package com.example.financeapp.presentation.bottomSheets.transactionEditor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.R
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.FinancialAccountsFilter
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionPayload
import com.example.financeapp.domain.usecase.CreateTransactionUseCase
import com.example.financeapp.domain.usecase.GetCategoriesUseCase
import com.example.financeapp.domain.usecase.GetFinancialAccountsOverviewUseCase
import com.example.financeapp.domain.usecase.GetTransactionUseCase
import com.example.financeapp.domain.usecase.UpdateTransactionUseCase
import com.example.financeapp.presentation.common.placeholders.toScreenError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
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

@HiltViewModel
class TransactionEditorViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getFinancialAccountsOverview: GetFinancialAccountsOverviewUseCase,
    private val getTransaction: GetTransactionUseCase,
    private val createTransaction: CreateTransactionUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
    private val reducer: TransactionEditorReducer,
    private val networkMonitor: NetworkMonitor,
    private val clock: Clock
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionEditorHostState())
    val state: StateFlow<TransactionEditorHostState> = _state.asStateFlow()

    private val effectChannel = Channel<TransactionEditorEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var loadJob: Job? = null
    private var saveJob: Job? = null

    fun onIntent(intent: TransactionEditorIntent) {
        when (intent) {
            is TransactionEditorIntent.Open -> open(intent.mode)
            TransactionEditorIntent.ConfirmClicked -> save()
            TransactionEditorIntent.DismissRequested -> dismiss()
            TransactionEditorIntent.RetryClicked -> retry()
            else -> reduce(intent)
        }
    }

    private fun open(mode: TransactionEditorMode) {
        loadJob?.cancel()
        saveJob?.cancel()

        val now = ZonedDateTime.now(clock)
        _state.value = TransactionEditorHostState(
            form = TransactionEditorState(
                mode = mode,
                date = now.toLocalDate(),
                time = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES),
                isLoading = true
            )
        )
        loadJob = viewModelScope.launch {
            load(mode)
        }
    }

    private suspend fun load(mode: TransactionEditorMode) {
        coroutineScope {
            val categoriesDeferred = async {
                getCategories(mode.transactionType)
            }
            val accountsDeferred = async {
                getFinancialAccountsOverview(
                    filter = FinancialAccountsFilter(),
                    totalCurrency = Currency.RUB
                )
            }
            val transactionDeferred = (mode as? TransactionEditorMode.Edit)?.let { editMode ->
                async {
                    getTransaction(editMode.transactionId)
                }
            }

            val categoriesResult = categoriesDeferred.await()
            val accountsResult = accountsDeferred.await()
            val transactionResult = transactionDeferred?.await()
            val error = categoriesResult.exceptionOrNull()
                ?: accountsResult.exceptionOrNull()
                ?: transactionResult?.exceptionOrNull()

            if (error != null) {
                Log.e(TAG, "Failed to load transaction editor", error)
                updateCurrentForm(mode) { form ->
                    form.copy(
                        isLoading = false,
                        error = error.toScreenError(networkMonitor.isOnline.value)
                    )
                }
                return@coroutineScope
            }

            val categories = categoriesResult.getOrThrow()
            val accounts = accountsResult.getOrThrow().accounts
            val transaction = transactionResult?.getOrNull()

            updateCurrentForm(mode) { form ->
                form.withLoadedData(
                    categories = categories,
                    accounts = accounts,
                    transaction = transaction
                )
            }
        }
    }

    private fun TransactionEditorState.withLoadedData(
        categories: List<Category>,
        accounts: List<FinancialAccount>,
        transaction: Transaction?
    ): TransactionEditorState {
        if (transaction == null) {
            return copy(
                availableCategories = categories,
                availableAccounts = accounts,
                selectedAccount = accounts.firstOrNull(),
                isLoading = false,
                error = null
            )
        }

        val transactionDate = transaction.transactionDate.atZone(clock.zone)
        return copy(
            amount = transaction.amount.amount
                .abs()
                .setScale(0, RoundingMode.DOWN)
                .toPlainString(),
            selectedCategory = categories.firstOrNull { category ->
                category.id == transaction.categoryId
            },
            selectedAccount = accounts.firstOrNull { account ->
                account.id == transaction.accountId
            },
            date = transactionDate.toLocalDate(),
            time = transactionDate.toLocalTime().truncatedTo(ChronoUnit.MINUTES),
            availableCategories = categories,
            availableAccounts = accounts,
            comment = transaction.comment,
            isLoading = false,
            error = null
        )
    }

    private fun reduce(intent: TransactionEditorIntent) {
        _state.update { hostState ->
            val form = hostState.form ?: return@update hostState
            hostState.copy(form = reducer.reduce(form, intent))
        }
    }

    private fun save() {
        val form = _state.value.form ?: return
        if (form.isLoading || form.isSaving) return

        val validationMessage = reducer.validationMessage(form)
        if (validationMessage != null) {
            _state.update { hostState ->
                hostState.copy(
                    form = hostState.form?.copy(formMessageResId = validationMessage)
                )
            }
            return
        }

        val account = requireNotNull(form.effectiveAccount)
        val category = requireNotNull(form.selectedCategory)
        val payload = TransactionPayload(
            accountId = account.id,
            categoryId = category.id,
            amount = Money(
                amount = form.amount.toBigDecimal(),
                currency = account.balance.currency
            ),
            transactionDate = LocalDateTime.of(form.date, form.time)
                .atZone(clock.zone)
                .toInstant(),
            comment = form.comment
                ?.trim()
                ?.takeIf { comment -> comment.isNotBlank() }
        )
        Log.d(
            TAG,
            "Saving transaction: mode=${form.mode.logName()}, " +
                "accountId=${payload.accountId}, " +
                "categoryId=${payload.categoryId}, " +
                "amount=${payload.amount.amount.toPlainString()} ${payload.amount.currency.code}, " +
                "transactionDate=${payload.transactionDate}, " +
                "rawComment=${form.comment.logValue()}, " +
                "payloadComment=${payload.comment.logValue()}"
        )

        _state.update { hostState ->
            hostState.copy(
                form = hostState.form?.copy(
                    isSaving = true,
                    formMessageResId = null
                )
            )
        }

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val result = when (val mode = form.mode) {
                is TransactionEditorMode.Create -> createTransaction(payload)
                is TransactionEditorMode.Edit -> updateTransaction(
                    id = mode.transactionId,
                    payload = payload
                )
            }

            result.fold(
                onSuccess = { transaction ->
                    _state.value = TransactionEditorHostState()
                    effectChannel.send(
                        TransactionEditorEffect.Saved(transaction.id)
                    )
                },
                onFailure = { error ->
                    Log.e(
                        TAG,
                        "Failed to save transaction: mode=${form.mode.logName()}, " +
                            "accountId=${payload.accountId}, " +
                            "categoryId=${payload.categoryId}, " +
                            "amount=${payload.amount.amount.toPlainString()} ${payload.amount.currency.code}, " +
                            "transactionDate=${payload.transactionDate}, " +
                            "payloadComment=${payload.comment.logValue()}",
                        error
                    )
                    updateCurrentForm(form.mode) { currentForm ->
                        currentForm.copy(
                            isSaving = false,
                            formMessageResId = R.string.transaction_save_failed
                        )
                    }
                }
            )
        }
    }

    private fun retry() {
        _state.value.form?.mode?.let(::open)
    }

    private fun dismiss() {
        val form = _state.value.form ?: return
        if (form.isSaving) return

        loadJob?.cancel()
        _state.value = TransactionEditorHostState()
        effectChannel.trySend(TransactionEditorEffect.Close)
    }

    private inline fun updateCurrentForm(
        mode: TransactionEditorMode,
        transform: (TransactionEditorState) -> TransactionEditorState
    ) {
        _state.update { hostState ->
            val form = hostState.form
            if (form == null || form.mode != mode) {
                hostState
            } else {
                hostState.copy(form = transform(form))
            }
        }
    }

    private companion object {
        const val TAG = "TransactionEditorVM"
    }
}

private fun TransactionEditorMode.logName(): String {
    return when (this) {
        is TransactionEditorMode.Create -> "Create(type=$transactionType)"
        is TransactionEditorMode.Edit -> "Edit(id=$transactionId, type=$transactionType)"
    }
}

private fun String?.logValue(): String {
    return when {
        this == null -> "null"
        isEmpty() -> "empty"
        isBlank() -> "blank(length=$length)"
        else -> "value(length=$length)"
    }
}
