package com.example.financeapp.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialSummaryCriteria
import com.example.financeapp.domain.model.SyncEvent
import com.example.financeapp.domain.model.SyncStatus
import com.example.financeapp.domain.usecase.AccountUseCases
import com.example.financeapp.domain.usecase.TransactionUseCases
import com.example.financeapp.domain.usecase.GetFinancialSummaryUseCase
import com.example.financeapp.domain.usecase.SynchronizationUseCases
import com.example.financeapp.presentation.accounts.AccountsState
import com.example.financeapp.presentation.common.model.TransactionsSectionState
import com.example.financeapp.presentation.common.placeholders.ScreenError
import com.example.financeapp.presentation.common.placeholders.toScreenError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class MainViewModel @Inject constructor(
    private val financialSummaryUseCase: GetFinancialSummaryUseCase,
    private val transactionUseCases: TransactionUseCases,
    private val accountUseCases: AccountUseCases,
    private val synchronizationUseCases: SynchronizationUseCases,
    private val networkMonitor: NetworkMonitor,
    private val clock: Clock
) : ViewModel() {

    private val _state = MutableStateFlow(
        MainState(selectedDate = LocalDate.now(clock))
    )
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val effectChannel = Channel<MainEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var loadJob: Job? = null
    private var deleteJob: Job? = null
    private val refreshMutex = Mutex()

    init {
        loadMainData()
        startSyncEventObservation()
    }

    fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.DateSelected -> {
                _state.update { state -> state.copy(selectedDate = intent.date) }
            }
            MainIntent.Retry -> {
                refreshFromNetwork()
            }
            MainIntent.DataChanged -> {
                refreshFromNetwork(isSilent = true)
            }
            is MainIntent.DeleteTransaction -> {
                deleteMainItem(
                    logMessage = "Failed to delete transaction: id=${intent.transactionId}",
                    delete = { transactionUseCases.delete(intent.transactionId) }
                )
            }
            is MainIntent.DeleteFinancialAccount -> {
                deleteMainItem(
                    logMessage = "Failed to delete financial account: id=${intent.accountId}",
                    delete = { accountUseCases.delete(intent.accountId) }
                )
            }
            MainIntent.RetryFailedSyncOperations -> controlFailedSyncOperations(
                operation = { synchronizationUseCases.retryFailedOperations() },
                refreshAfterSuccess = false
            )
            MainIntent.DiscardFailedSyncOperations -> controlFailedSyncOperations(
                operation = { synchronizationUseCases.discardFailedOperations() },
                refreshAfterSuccess = true
            )
        }
    }

    fun refreshFromNetwork(isSilent: Boolean = false) {
        loadMainData(
            isSilent = isSilent,
            useRefreshLock = true
        )
    }

    private fun startSyncEventObservation() {
        viewModelScope.launch {
            synchronizationUseCases.observeEvents().collect { event ->
                when (event) {
                    SyncEvent.DataRefreshed -> refreshFromNetwork(isSilent = true)
                    is SyncEvent.OperationsFailed -> {
                        effectChannel.send(MainEffect.SyncFailed(event.count))
                    }
                }
            }
        }
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

        val result = financialSummaryUseCase(
            FinancialSummaryCriteria(currency = Currency.RUB)
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
            val hasLoadedSyncState = transactionsOverview != null || accountsOverview != null
            val hasPendingSync = if (hasLoadedSyncState) {
                (transactionsOverview?.hasPendingSync() == true) ||
                    (accountsOverview?.hasPendingSync() == true)
            } else {
                state.hasPendingSync
            }

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
                    ),
                hasPendingSync = hasPendingSync
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

    private fun deleteMainItem(
        logMessage: String,
        delete: suspend () -> Result<Unit>
    ) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            delete().fold(
                onSuccess = {
                    refreshFromNetwork(isSilent = true)
                },
                onFailure = { error ->
                    Log.e(TAG, logMessage, error)
                    effectChannel.send(MainEffect.DeleteFailed)
                }
            )
        }
    }

    private fun controlFailedSyncOperations(
        operation: suspend () -> Result<Unit>,
        refreshAfterSuccess: Boolean
    ) {
        viewModelScope.launch {
            operation().onSuccess {
                if (refreshAfterSuccess) {
                    refreshFromNetwork(isSilent = true)
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to control failed sync operations", error)
            }
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

private fun com.example.financeapp.domain.model.FinancialFlowSummary.hasPendingSync(): Boolean {
    return expenses.overview.transactions.any { transaction ->
        transaction.syncStatus == SyncStatus.PENDING
    } || income.overview.transactions.any { transaction ->
        transaction.syncStatus == SyncStatus.PENDING
    }
}

private fun com.example.financeapp.domain.model.AccountSummary.hasPendingSync(): Boolean {
    return accounts.any { account -> account.syncStatus == SyncStatus.PENDING }
}
