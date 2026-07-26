package com.example.financeapp.presentation.bottomSheets.transactionEditor

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.common.model.FinanceFieldType
import com.example.financeapp.presentation.common.placeholders.ScreenError
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class TransactionEditorHostState(
    val form: TransactionEditorState? = null
)

@Immutable
data class TransactionEditorState(
    val mode: TransactionEditorMode,
    val amount: String = "",
    val selectedCategory: Category? = null,
    val selectedAccount: FinancialAccount? = null,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val availableCategories: List<Category> = emptyList(),
    val availableAccounts: List<FinancialAccount> = emptyList(),
    val activeField: FinanceFieldType? = null,
    val comment: String? = null,
    @StringRes val formMessageResId: Int? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: ScreenError? = null
) {
    val transactionType: TransactionType
        get() = mode.transactionType

    val effectiveAccount: FinancialAccount?
        get() = selectedAccount ?: if (mode is TransactionEditorMode.Create) {
            availableAccounts.firstOrNull()
        } else {
            null
        }

    val currency: Currency?
        get() = effectiveAccount?.balance?.currency

    val canConfirm: Boolean
        get() = amount.toBigDecimalOrNull()?.signum() == 1 &&
            selectedCategory != null &&
            effectiveAccount != null &&
            !isLoading &&
            !isSaving
}

@Immutable
sealed interface TransactionEditorMode {
    val transactionType: TransactionType

    data class Create(
        override val transactionType: TransactionType
    ) : TransactionEditorMode

    data class Edit(
        val transactionId: Long,
        override val transactionType: TransactionType
    ) : TransactionEditorMode
}

@Immutable
sealed interface TransactionEditorIntent {
    data class Open(val mode: TransactionEditorMode) : TransactionEditorIntent
    data class AmountChanged(val amount: String) : TransactionEditorIntent
    data class FieldClicked(val type: FinanceFieldType) : TransactionEditorIntent
    data object FieldDismissed : TransactionEditorIntent
    data class CategorySelected(val categoryId: Long) : TransactionEditorIntent
    data class DateChanged(val date: LocalDate) : TransactionEditorIntent
    data class TimeChanged(val time: LocalTime) : TransactionEditorIntent
    data class AccountSelected(val accountId: Long) : TransactionEditorIntent
    data class CommentChanged(val comment: String) : TransactionEditorIntent
    data object ConfirmClicked : TransactionEditorIntent
    data object DismissRequested : TransactionEditorIntent
    data object RetryClicked : TransactionEditorIntent
}

@Immutable
sealed interface TransactionEditorEffect {
    data class Saved(val transactionId: Long) : TransactionEditorEffect
    data object Close : TransactionEditorEffect
}

val TransactionEditorFieldTypes: List<FinanceFieldType> = listOf(
    FinanceFieldType.Category,
    FinanceFieldType.Date,
    FinanceFieldType.Time,
    FinanceFieldType.Account,
    FinanceFieldType.Description
)
