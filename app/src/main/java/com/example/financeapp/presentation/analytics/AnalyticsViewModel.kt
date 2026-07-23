package com.example.financeapp.presentation.analytics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.FinancialAccountsFilter
import com.example.financeapp.domain.usecase.GetAnalyticsOverviewUseCase
import com.example.financeapp.domain.usecase.GetFinancialAccountsOverviewUseCase
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.common.network.NetworkRefreshable
import com.example.financeapp.presentation.common.placeholders.ScreenError
import com.example.financeapp.presentation.common.placeholders.toScreenError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsOverview: GetAnalyticsOverviewUseCase,
    private val getFinancialAccountsOverview: GetFinancialAccountsOverviewUseCase,
    private val categoryColorMapper: AnalyticsCategoryColorMapper,
    private val periodResolver: AnalyticsPeriodResolver,
    private val filterUiMapper: AnalyticsFilterUiMapper,
    private val stateMapper: AnalyticsStateMapper,
    private val filterReducer: AnalyticsFilterReducer,
    private val networkMonitor: NetworkMonitor,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel(), NetworkRefreshable {

    private val initialPeriodFilter = periodResolver.defaultPeriodFilter()
    private val initialFilter = defaultAnalyticsFilter(periodFilter = initialPeriodFilter)
    private val _state = MutableStateFlow(
        AnalyticsState(
            filter = initialFilter,
            periodFilter = initialPeriodFilter
        )
    )
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    private val effectChannel = Channel<AnalyticsEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var loadJob: Job? = null
    private var referenceDataJob: Job? = null
    private val refreshMutex = Mutex()
    private var currentFilter = initialFilter
    private var currentPeriodFilter = initialPeriodFilter

    init {
        loadReferenceData()
        loadAnalytics()
    }

    fun onIntent(intent: AnalyticsIntent) {
        when (intent) {
            AnalyticsIntent.BackClicked -> sendEffect(AnalyticsEffect.NavigateBack)
            AnalyticsIntent.Retry -> {
                refreshFromNetwork()
            }
            AnalyticsIntent.ChartClicked -> {
                _state.update { state -> filterReducer.showDetail(state) }
            }
            AnalyticsIntent.DetailDismissed -> {
                _state.update { state -> filterReducer.hideDetail(state) }
            }
            is AnalyticsIntent.FilterClicked -> {
                _state.update { state -> filterReducer.openFilterSheet(state, intent.type) }
            }
            AnalyticsIntent.FilterDismissed -> {
                _state.update { state -> filterReducer.dismissFilterSheet(state) }
            }
            is AnalyticsIntent.TypeApplied -> {
                applyFilterChange(
                    filterReducer.applyType(
                        state = _state.value,
                        currentFilter = currentFilter,
                        currentPeriodFilter = currentPeriodFilter,
                        type = intent.type
                    )
                )
            }
            is AnalyticsIntent.PeriodSelected -> {
                applyFilterChange(
                    filterReducer.selectPeriod(
                        state = _state.value,
                        currentFilter = currentFilter,
                        currentPeriodFilter = currentPeriodFilter,
                        periodType = intent.periodType
                    )
                )
            }
            is AnalyticsIntent.CustomPeriodApplied -> {
                applyFilterChange(
                    filterReducer.applyCustomPeriod(
                        state = _state.value,
                        currentFilter = currentFilter,
                        startDate = intent.startDate,
                        endDate = intent.endDate
                    )
                )
            }
            AnalyticsIntent.CustomPeriodCancelClicked -> {
                _state.update { state -> filterReducer.returnToPeriodSheet(state) }
            }
            is AnalyticsIntent.CategorySelectionApplied -> {
                applyFilterChange(
                    filterReducer.applyCategories(
                        state = _state.value,
                        currentFilter = currentFilter,
                        currentPeriodFilter = currentPeriodFilter,
                        categoryIds = intent.categoryIds
                    )
                )
            }
            is AnalyticsIntent.AccountSelected -> {
                applyFilterChange(
                    filterReducer.applyAccount(
                        state = _state.value,
                        currentFilter = currentFilter,
                        currentPeriodFilter = currentPeriodFilter,
                        accountId = intent.accountId
                    )
                )
            }
        }
    }

    private fun applyFilterChange(change: AnalyticsFilterChange) {
        currentFilter = change.filter
        currentPeriodFilter = change.periodFilter
        _state.value = change.state
        if (change.shouldReload) {
            loadAnalytics()
        }
    }

    override fun refreshFromNetwork(isSilent: Boolean) {
        if (!refreshMutex.tryLock()) return

        referenceDataJob?.cancel()
        loadJob?.cancel()

        val requestedFilter = currentFilter
        val requestedPeriodFilter = currentPeriodFilter
        loadJob = viewModelScope.launch {
            try {
                val isCompleted = withTimeoutOrNull(RefreshTimeoutMillis) {
                    coroutineScope {
                        val referenceDataDeferred = async {
                            loadReferenceDataInternal(isSilent = isSilent)
                        }
                        val analyticsDeferred = async {
                            loadAnalyticsInternal(
                                requestedFilter = requestedFilter,
                                requestedPeriodFilter = requestedPeriodFilter,
                                isSilent = isSilent
                            )
                        }

                        referenceDataDeferred.await()
                        analyticsDeferred.await()
                    }
                    true
                } == true

                if (!isCompleted) {
                    showRefreshTimeout(isSilent = isSilent)
                }
            } finally {
                refreshMutex.unlock()
            }
        }
    }

    private fun loadReferenceData(
        isSilent: Boolean = false
    ) {
        referenceDataJob?.cancel()
        referenceDataJob = viewModelScope.launch {
            loadReferenceDataInternal(isSilent = isSilent)
        }
    }

    private fun loadAnalytics(isSilent: Boolean = false) {
        loadJob?.cancel()
        val requestedFilter = currentFilter
        val requestedPeriodFilter = currentPeriodFilter
        loadJob = viewModelScope.launch {
            loadAnalyticsInternal(
                requestedFilter = requestedFilter,
                requestedPeriodFilter = requestedPeriodFilter,
                isSilent = isSilent
            )
        }
    }

    private suspend fun loadReferenceDataInternal(isSilent: Boolean = false) {
        getFinancialAccountsOverview(
            filter = FinancialAccountsFilter(currency = currentFilter.currency),
            totalCurrency = currentFilter.currency
        )
            .onSuccess { overview ->
                _state.update { state ->
                    state.copy(
                        availableAccounts = overview.accounts,
                        filters = filterUiMapper.map(
                            filter = currentFilter,
                            periodFilter = currentPeriodFilter,
                            categories = state.availableCategories,
                            accounts = overview.accounts
                        )
                    )
                }
            }
            .onFailure { error ->
                logReferenceLoadError(
                    message = "Failed to load analytics accounts",
                    error = error,
                    isSilent = isSilent
                )
            }
    }

    private suspend fun loadAnalyticsInternal(
        requestedFilter: AnalyticsFilter,
        requestedPeriodFilter: AnalyticsPeriodFilterState,
        isSilent: Boolean = false
    ) {
        Log.d(TAG, "Loading analytics: filter=$requestedFilter")
        if (!isSilent) {
            _state.update { state ->
                state.copy(
                    filter = requestedFilter,
                    periodFilter = requestedPeriodFilter,
                    isLoading = true,
                    isEmpty = false,
                    error = null
                )
            }
        }

        getAnalyticsOverview(requestedFilter).fold(
            onSuccess = { overview ->
                Log.d(TAG, "Loaded ${overview.transactions.size} analytics transactions")
                val categoryItems = stateMapper.mapCategories(overview.categories)
                val categoryColors = withContext(defaultDispatcher) {
                    categoryColorMapper.map(categoryItems)
                }
                _state.update { state ->
                    stateMapper.mapOverview(
                        currentState = state,
                        overview = overview,
                        categoryItems = categoryItems,
                        categoryColors = categoryColors,
                        periodFilter = requestedPeriodFilter
                    )
                }
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to load analytics", error)
                if (!isSilent) {
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            isEmpty = false,
                            error = error.toScreenError(networkMonitor.isOnline.value)
                        )
                    }
                }
            }
        )
    }

    private fun logReferenceLoadError(
        message: String,
        error: Throwable,
        isSilent: Boolean
    ) {
        if (isSilent) {
            Log.d(TAG, message, error)
        } else {
            Log.e(TAG, message, error)
        }
    }

    private fun showRefreshTimeout(isSilent: Boolean) {
        Log.e(TAG, "Analytics refresh timed out")
        if (isSilent) return
        _state.update { state ->
            state.copy(
                isLoading = false,
                isEmpty = false,
                error = ScreenError.TIMEOUT
            )
        }
    }

    private fun sendEffect(effect: AnalyticsEffect) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    private companion object {
        const val TAG = "AnalyticsViewModel"
        const val RefreshTimeoutMillis = 30_000L
    }
}
