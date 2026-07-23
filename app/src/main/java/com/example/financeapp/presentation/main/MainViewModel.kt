package com.example.financeapp.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.MainOverviewFilter
import com.example.financeapp.domain.usecase.GetMainOverviewUseCase
import com.example.financeapp.presentation.accounts.AccountsState
import com.example.financeapp.presentation.common.model.TransactionsSectionState
import com.example.financeapp.presentation.common.network.NetworkRefreshable
import com.example.financeapp.presentation.common.placeholders.ScreenError
import com.example.financeapp.presentation.common.placeholders.toScreenError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMainOverview: GetMainOverviewUseCase,
    private val networkMonitor: NetworkMonitor,
    private val clock: Clock
) : ViewModel(), NetworkRefreshable {

    private val _state = MutableStateFlow(
        MainState(selectedDate = LocalDate.now(clock))
    )
    val state: StateFlow<MainState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private val refreshMutex = Mutex()

    init {
        loadMainData()
    }

    fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.DateSelected -> {
                _state.update { state -> state.copy(selectedDate = intent.date) }
            }
            MainIntent.Retry -> {
                refreshFromNetwork()
            }
        }
    }

    override fun refreshFromNetwork(isSilent: Boolean) {
        loadMainData(
            isSilent = isSilent,
            useRefreshLock = true
        )
    }

    private fun loadMainData(
        isSilent: Boolean = false,
        useRefreshLock: Boolean = false
    ) {
        if (useRefreshLock && !refreshMutex.tryLock()) return

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                if (useRefreshLock) {
                    val isCompleted = withTimeoutOrNull(RefreshTimeoutMillis) {
                        loadMainDataInternal(isSilent = isSilent)
                        true
                    } == true

                    if (!isCompleted) {
                        showRefreshTimeout(isSilent = isSilent)
                    }
                } else {
                    loadMainDataInternal(isSilent = isSilent)
                }
            } finally {
                if (useRefreshLock) {
                    refreshMutex.unlock()
                }
            }
        }
    }

    private suspend fun loadMainDataInternal(isSilent: Boolean) {
        Log.d(TAG, "Loading main route data")
        if (!isSilent) {
            _state.update { state ->
                state.copy(
                    expensesState = state.expensesState.copy(isLoading = true, error = null),
                    incomeState = state.incomeState.copy(isLoading = true, error = null),
                    accountsState = state.accountsState.copy(isLoading = true, error = null)
                )
            }
        }

        val result = getMainOverview(
            MainOverviewFilter(currency = Currency.RUB)
        )
        val transactionsError = result.transactions.exceptionOrNull()
        val accountsError = result.accounts.exceptionOrNull()

        transactionsError?.let { error ->
            logLoadError(
                message = "Failed to load main transactions",
                error = error,
                isSilent = isSilent
            )
        }
        accountsError?.let { error ->
            logLoadError(
                message = "Failed to load main accounts",
                error = error,
                isSilent = isSilent
            )
        }

        val isOnline = networkMonitor.isOnline.value
        val transactionsScreenError = transactionsError?.toScreenError(isOnline)
        val accountsScreenError = accountsError?.toScreenError(isOnline)

        _state.update { state ->
            val transactionsOverview = result.transactions.getOrNull()
            val accountsOverview = result.accounts.getOrNull()

            state.copy(
                expensesState = transactionsOverview
                    ?.expenses
                    ?.toExpensesState()
                    ?: state.expensesState.toLoadFailure(
                        isSilent = isSilent,
                        error = requireNotNull(transactionsScreenError)
                    ),
                incomeState = transactionsOverview
                    ?.income
                    ?.toIncomeState()
                    ?: state.incomeState.toLoadFailure(
                        isSilent = isSilent,
                        error = requireNotNull(transactionsScreenError)
                    ),
                accountsState = accountsOverview
                    ?.toAccountsState()
                    ?: state.accountsState.toLoadFailure(
                        isSilent = isSilent,
                        error = requireNotNull(accountsScreenError)
                    )
            )
        }
    }

    private fun showRefreshTimeout(isSilent: Boolean) {
        Log.e(TAG, "Main route refresh timed out")
        _state.update { state ->
            state.copy(
                expensesState = state.expensesState.toLoadFailure(
                    isSilent = isSilent,
                    error = ScreenError.TIMEOUT
                ),
                incomeState = state.incomeState.toLoadFailure(
                    isSilent = isSilent,
                    error = ScreenError.TIMEOUT
                ),
                accountsState = state.accountsState.toLoadFailure(
                    isSilent = isSilent,
                    error = ScreenError.TIMEOUT
                )
            )
        }
    }

    private fun TransactionsSectionState.toLoadFailure(
        isSilent: Boolean,
        error: ScreenError
    ): TransactionsSectionState {
        if (isSilent) return this
        return copy(
            isLoading = false,
            error = error
        )
    }

    private fun AccountsState.toLoadFailure(
        isSilent: Boolean,
        error: ScreenError
    ): AccountsState {
        if (isSilent) return this
        return copy(
            isLoading = false,
            error = error
        )
    }

    private fun logLoadError(
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

    private companion object {
        const val TAG = "MainViewModel"
        const val RefreshTimeoutMillis = 30_000L
    }
}
