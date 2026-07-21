package com.example.financeapp.presentation.analytics

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.R
import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.AnalyticsOverview
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.usecase.GetAnalyticsOverviewUseCase
import com.example.financeapp.domain.usecase.GetFinancialAccountsUseCase
import com.example.financeapp.presentation.analytics.bottomsheets.AnalyticsFilterType
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodType
import com.example.financeapp.presentation.analytics.bottomsheets.period.defaultAnalyticsPeriodFilterState
import com.example.financeapp.presentation.common.model.RouteScreenItem
import com.example.financeapp.presentation.common.network.NetworkRefreshable
import com.example.financeapp.presentation.common.placeholders.ScreenError
import com.example.financeapp.presentation.common.placeholders.toScreenError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    private val getFinancialAccounts: GetFinancialAccountsUseCase,
    private val categoryColorMapper: AnalyticsCategoryColorMapper,
    private val clock: Clock,
    private val networkMonitor: NetworkMonitor,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel(), NetworkRefreshable {

    private val initialPeriodFilter = defaultAnalyticsPeriodFilterState(today = LocalDate.now(clock))
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
                _state.update { state ->
                    state.copy(
                        isDetailVisible = true,
                        activeFilterSheet = null
                    )
                }
            }
            AnalyticsIntent.DetailDismissed -> {
                _state.update { state ->
                    state.copy(isDetailVisible = false)
                }
            }
            is AnalyticsIntent.FilterClicked -> {
                _state.update { state ->
                    state.copy(
                        activeFilterSheet = intent.type.toFilterSheet(),
                        isDetailVisible = false
                    )
                }
            }
            AnalyticsIntent.FilterDismissed -> {
                _state.update { state -> state.copy(activeFilterSheet = null) }
            }
            is AnalyticsIntent.TypeApplied -> {
                applyFilter(
                    currentFilter.copy(
                        type = intent.type,
                        categoryIds = emptySet()
                    )
                )
            }
            is AnalyticsIntent.PeriodSelected -> {
                if (intent.periodType == AnalyticsPeriodType.Custom) {
                    _state.update { state ->
                        state.copy(activeFilterSheet = AnalyticsFilterSheet.CustomPeriod)
                    }
                } else {
                    val range = intent.periodType.rangeEndingAt(LocalDate.now(clock))
                    val periodFilter = AnalyticsPeriodFilterState(
                        selectedType = intent.periodType,
                        startDate = range.startDate,
                        endDate = range.endDate
                    )
                    applyFilter(
                        filter = currentFilter.copy(
                            startDate = range.startDate,
                            endDate = range.endDate
                        ),
                        periodFilter = periodFilter
                    )
                }
            }
            is AnalyticsIntent.CustomPeriodApplied -> {
                val startDate = minOf(intent.startDate, intent.endDate)
                val endDate = maxOf(intent.startDate, intent.endDate)
                val periodFilter = AnalyticsPeriodFilterState(
                    selectedType = AnalyticsPeriodType.Custom,
                    startDate = startDate,
                    endDate = endDate
                )
                applyFilter(
                    filter = currentFilter.copy(
                        startDate = startDate,
                        endDate = endDate
                    ),
                    periodFilter = periodFilter
                )
            }
            AnalyticsIntent.CustomPeriodCancelClicked -> {
                _state.update { state ->
                    state.copy(activeFilterSheet = AnalyticsFilterSheet.Period)
                }
            }
            is AnalyticsIntent.CategorySelectionApplied -> {
                applyFilter(currentFilter.copy(categoryIds = intent.categoryIds))
            }
            is AnalyticsIntent.AccountSelected -> {
                applyFilter(currentFilter.copy(accountId = intent.accountId))
            }
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

    private fun applyFilter(
        filter: AnalyticsFilter,
        periodFilter: AnalyticsPeriodFilterState = currentPeriodFilter
    ) {
        currentFilter = filter
        currentPeriodFilter = periodFilter
        _state.update { state ->
            state.copy(
                filter = filter,
                periodFilter = periodFilter,
                activeFilterSheet = null,
                filters = filter.analyticsFilters(
                    periodFilter = periodFilter,
                    categories = state.availableCategories,
                    accounts = state.availableAccounts
                )
            )
        }
        loadAnalytics()
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
        getFinancialAccounts(currentFilter.currency)
            .onSuccess { overview ->
                _state.update { state ->
                    state.copy(
                        availableAccounts = overview.accounts,
                        filters = currentFilter.analyticsFilters(
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
                val categoryColors = withContext(defaultDispatcher) {
                    categoryColorMapper.map(overview.categories)
                }
                _state.update { state ->
                    overview.toState(
                        currentState = state,
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

    private fun AnalyticsOverview.toState(
        currentState: AnalyticsState,
        categoryColors: Map<Long, Color>,
        periodFilter: AnalyticsPeriodFilterState
    ): AnalyticsState {
        return currentState.copy(
            filter = filter,
            periodFilter = periodFilter,
            total = total,
            categories = categories,
            availableCategories = availableCategories,
            categoryColors = categoryColors,
            filters = filter.analyticsFilters(
                periodFilter = periodFilter,
                categories = availableCategories,
                accounts = currentState.availableAccounts
            ),
            transactions = transactions.map { transaction ->
                RouteScreenItem(
                    id = transaction.id.toString(),
                    title = transaction.title,
                    leadingEmoji = transaction.leadingEmoji,
                    comment = transaction.accountName,
                    money = transaction.amount
                )
            },
            isLoading = false,
            isEmpty = categories.isEmpty() && transactions.isEmpty(),
            error = null
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

    private fun AnalyticsFilter.analyticsFilters(
        periodFilter: AnalyticsPeriodFilterState,
        categories: List<Category>,
        accounts: List<FinancialAccount>
    ): List<AnalyticsFilterUi> {
        return listOf(
            AnalyticsFilterUi(
                type = AnalyticsFilterType.Type,
                titleResId = R.string.analytics_filter_type,
                valueResId = type.analyticsTitleResId()
            ),
            AnalyticsFilterUi(
                type = AnalyticsFilterType.Period,
                titleResId = R.string.analytics_filter_period,
                valueResId = periodFilter.selectedType
                    .takeUnless { periodType -> periodType == AnalyticsPeriodType.Custom }
                    ?.titleResId,
                value = if (periodFilter.selectedType == AnalyticsPeriodType.Custom) {
                    formattedPeriod()
                } else {
                    ""
                }
            ),
            AnalyticsFilterUi(
                type = AnalyticsFilterType.Category,
                titleResId = R.string.analytics_filter_articles,
                valueResId = if (categoryIds.isEmpty()) {
                    R.string.analytics_filter_all_categories
                } else {
                    null
                },
                value = categories
                    .filter { category -> category.id in categoryIds }
                    .joinToString(separator = ", ") { category -> category.name }
            ),
            AnalyticsFilterUi(
                type = AnalyticsFilterType.Account,
                titleResId = R.string.analytics_filter_account,
                valueResId = if (accountId == null) {
                    R.string.analytics_filter_all_accounts
                } else {
                    null
                },
                value = accounts.firstOrNull { account -> account.id == accountId }?.name.orEmpty()
            )
        )
    }

    private fun AnalyticsFilter.formattedPeriod(): String {
        return "${startDate.format(FilterDateFormatter)} – ${endDate.format(FilterDateFormatter)}"
    }

    private fun TransactionType?.analyticsTitleResId(): Int {
        return when (this) {
            TransactionType.EXPENSE -> R.string.analytics_filter_expenses
            TransactionType.INCOME -> R.string.analytics_filter_income
            null -> R.string.analytics_filter_all
        }
    }

    private fun AnalyticsFilterType.toFilterSheet(): AnalyticsFilterSheet {
        return when (this) {
            AnalyticsFilterType.Type -> AnalyticsFilterSheet.Type
            AnalyticsFilterType.Period -> AnalyticsFilterSheet.Period
            AnalyticsFilterType.Category -> AnalyticsFilterSheet.Category
            AnalyticsFilterType.Account -> AnalyticsFilterSheet.Account
        }
    }

    private fun AnalyticsPeriodType.rangeEndingAt(endDate: LocalDate): DateRange {
        val startDate = when (this) {
            AnalyticsPeriodType.Custom -> currentPeriodFilter.startDate
            AnalyticsPeriodType.Week -> endDate.minusDays(6)
            AnalyticsPeriodType.Month -> endDate.minusMonths(1).plusDays(1)
            AnalyticsPeriodType.Quarter -> endDate.minusMonths(3).plusDays(1)
            AnalyticsPeriodType.Year -> endDate.minusYears(1).plusDays(1)
        }
        return DateRange(startDate = startDate, endDate = endDate)
    }

    private fun sendEffect(effect: AnalyticsEffect) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    private data class DateRange(
        val startDate: LocalDate,
        val endDate: LocalDate
    )

    private companion object {
        const val TAG = "AnalyticsViewModel"
        const val RefreshTimeoutMillis = 30_000L
        val FilterDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
}
