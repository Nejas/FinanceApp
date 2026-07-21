package com.example.financeapp.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionFilter
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.model.TransactionsOverview
import com.example.financeapp.domain.usecase.CalculateMoneyTotalUseCase
import com.example.financeapp.domain.usecase.GetAllFinancialAccountsOverviewUseCase
import com.example.financeapp.domain.usecase.GetCategoriesUseCase
import com.example.financeapp.domain.usecase.GetTransactionsUseCase
import com.example.financeapp.presentation.accounts.AccountsState
import com.example.financeapp.presentation.common.network.NetworkRefreshable
import com.example.financeapp.presentation.common.placeholders.ScreenError
import com.example.financeapp.presentation.common.placeholders.toScreenError
import com.example.financeapp.presentation.expenses.ExpensesState
import com.example.financeapp.presentation.income.IncomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val getAllFinancialAccountsOverview: GetAllFinancialAccountsOverviewUseCase,
    private val calculateMoneyTotal: CalculateMoneyTotalUseCase,
    private val networkMonitor: NetworkMonitor,
    private val clock: Clock,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
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
        loadJob = viewModelScope.launch(defaultDispatcher) {
            try {
                if (useRefreshLock) {
                    val isCompleted = withTimeoutOrNull(RefreshTimeoutMillis) {
                        loadMainDataInternal(
                            isSilent = isSilent
                        )
                        true
                    } == true

                    if (!isCompleted) {
                        showRefreshTimeout(isSilent = isSilent)
                    }
                } else {
                    loadMainDataInternal(
                        isSilent = isSilent
                    )
                }
            } finally {
                if (useRefreshLock) {
                    refreshMutex.unlock()
                }
            }
        }
    }

    private suspend fun loadMainDataInternal(
        isSilent: Boolean
    ) {
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

        coroutineScope {
            val categoriesDeferred = async { getCategories() }
            val accountsOverviewDeferred = async { getAllFinancialAccountsOverview(Currency.RUB) }
            val transactionsDeferred = async {
                suspendRunCatching {
                    val accounts = accountsOverviewDeferred.await()
                        .getOrThrow()
                        .accounts
                        .filterByCurrency(Currency.RUB)

                    loadTransactionsForAccounts(accounts).getOrThrow()
                }
            }

            val categoriesResult = categoriesDeferred.await()
            val accountsOverviewResult = accountsOverviewDeferred.await()
            val transactionsResult = transactionsDeferred.await()
            val transactionStatesResult = buildTransactionStates(
                categoriesResult = categoriesResult,
                accounts = accountsOverviewResult
                    .getOrNull()
                    ?.accounts
                    .orEmpty()
                    .filterByCurrency(Currency.RUB),
                transactionsResult = transactionsResult
            )
            val accountsResult = accountsOverviewResult.map { overview ->
                AccountsState(
                    accounts = overview.accounts,
                    totalBalance = overview.totalBalance,
                    isLoading = false,
                    error = null
                )
            }

            _state.update { state ->
                state.copy(
                    expensesState = transactionStatesResult
                        .map { states -> states.expensesState }
                        .getOrElse { error ->
                            logLoadError(
                                message = "Failed to load main expenses",
                                error = error,
                                isSilent = isSilent
                            )
                            state.expensesState.toLoadFailure(
                                isSilent = isSilent,
                                error = error.toScreenError(networkMonitor.isOnline.value)
                            )
                        },
                    incomeState = transactionStatesResult
                        .map { states -> states.incomeState }
                        .getOrElse { error ->
                            logLoadError(
                                message = "Failed to load main income",
                                error = error,
                                isSilent = isSilent
                            )
                            state.incomeState.toLoadFailure(
                                isSilent = isSilent,
                                error = error.toScreenError(networkMonitor.isOnline.value)
                            )
                        },
                    accountsState = accountsResult.getOrElse { error ->
                        logLoadError(
                            message = "Failed to load main accounts",
                            error = error,
                            isSilent = isSilent
                        )
                        state.accountsState.toLoadFailure(
                            isSilent = isSilent,
                            error = error.toScreenError(networkMonitor.isOnline.value)
                        )
                    }
                )
            }
        }
    }

    private suspend fun loadTransactionsForAccounts(
        accounts: List<FinancialAccount>
    ): Result<List<Transaction>> {
        return suspendRunCatching {
            coroutineScope {
                accounts.map { account ->
                    async {
                        getTransactions(
                            TransactionFilter(accountId = account.id)
                        ).getOrThrow()
                    }
                }
                    .awaitAll()
                    .flatten()
            }
        }
    }

    private suspend fun buildTransactionStates(
        categoriesResult: Result<List<Category>>,
        accounts: List<FinancialAccount>,
        transactionsResult: Result<List<Transaction>>
    ): Result<MainTransactionsStateData> {
        return suspendRunCatching {
            coroutineScope {
                val categories = categoriesResult.getOrThrow()
                val transactions = transactionsResult.getOrThrow()
                val expensesDeferred = async(defaultDispatcher) {
                    buildTransactionsStateData(
                        type = TransactionType.EXPENSE,
                        categories = categories,
                        accounts = accounts,
                        transactions = transactions
                    ).toExpensesState()
                }
                val incomeDeferred = async(defaultDispatcher) {
                    buildTransactionsStateData(
                        type = TransactionType.INCOME,
                        categories = categories,
                        accounts = accounts,
                        transactions = transactions
                    ).toIncomeState()
                }

                MainTransactionsStateData(
                    expensesState = expensesDeferred.await(),
                    incomeState = incomeDeferred.await()
                )
            }
        }
    }

    private fun buildTransactionsStateData(
        type: TransactionType,
        categories: List<Category>,
        accounts: List<FinancialAccount>,
        transactions: List<Transaction>
    ): TransactionsStateData {
        val categoriesById = categories.associateBy { category -> category.id }
        val typeCategoriesById = categories
            .filter { category -> category.type == type }
            .associateBy { category -> category.id }
        val typedTransactions = transactions
            .filter { transaction ->
                categoriesById[transaction.categoryId]?.type == type
            }
            .sortedByDescending { transaction -> transaction.transactionDate }

        return TransactionsStateData(
            categoriesById = typeCategoriesById,
            overview = TransactionsOverview(
                transactions = typedTransactions,
                accounts = accounts,
                total = calculateMoneyTotal(
                    amounts = typedTransactions.map { transaction -> transaction.amount },
                    fallbackCurrency = Currency.RUB
                )
            )
        )
    }

    private fun List<FinancialAccount>.filterByCurrency(
        currency: Currency
    ): List<FinancialAccount> {
        return filter { account -> account.balance.currency == currency }
    }

    private fun TransactionsStateData.toExpensesState(): ExpensesState {
        return ExpensesState(
            transactions = overview.transactions,
            categoriesById = categoriesById,
            total = overview.total,
            isLoading = false,
            hasLoaded = true,
            error = null
        )
    }

    private fun TransactionsStateData.toIncomeState(): IncomeState {
        return IncomeState(
            transactions = overview.transactions,
            categoriesById = categoriesById,
            total = overview.total,
            isLoading = false,
            hasLoaded = true,
            error = null
        )
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

    private fun ExpensesState.toLoadFailure(
        isSilent: Boolean,
        error: ScreenError
    ): ExpensesState {
        if (isSilent) return this
        return copy(
            isLoading = false,
            error = error
        )
    }

    private fun IncomeState.toLoadFailure(
        isSilent: Boolean,
        error: ScreenError
    ): IncomeState {
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

    private data class TransactionsStateData(
        val categoriesById: Map<Long, Category>,
        val overview: TransactionsOverview
    )

    private data class MainTransactionsStateData(
        val expensesState: ExpensesState,
        val incomeState: IncomeState
    )

    private companion object {
        const val TAG = "MainViewModel"
        const val RefreshTimeoutMillis = 30_000L
    }
}
