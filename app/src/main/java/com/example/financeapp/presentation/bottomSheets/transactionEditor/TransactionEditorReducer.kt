package com.example.financeapp.presentation.bottomSheets.transactionEditor

import androidx.annotation.StringRes
import com.example.financeapp.R
import javax.inject.Inject

class TransactionEditorReducer @Inject constructor() {

    fun reduce(
        state: TransactionEditorState,
        intent: TransactionEditorIntent
    ): TransactionEditorState {
        return when (intent) {
            is TransactionEditorIntent.AmountChanged -> state.copy(
                amount = intent.amount.normalizedIntegerAmount(),
                formMessageResId = null
            )
            is TransactionEditorIntent.FieldClicked -> state.copy(
                activeField = intent.field,
                formMessageResId = null
            )
            TransactionEditorIntent.FieldDismissed -> state.copy(activeField = null)
            is TransactionEditorIntent.CategorySelected -> state.copy(
                selectedCategory = state.availableCategories.firstOrNull { category ->
                    category.id == intent.categoryId
                },
                activeField = null,
                formMessageResId = null
            )
            is TransactionEditorIntent.AccountSelected -> state.copy(
                selectedAccount = state.availableAccounts.firstOrNull { account ->
                    account.id == intent.accountId
                },
                activeField = null,
                formMessageResId = null
            )
            is TransactionEditorIntent.DateChanged -> state.copy(
                date = intent.date,
                activeField = null,
                formMessageResId = null
            )
            is TransactionEditorIntent.TimeChanged -> state.copy(
                time = intent.time.withSecond(0).withNano(0),
                activeField = null,
                formMessageResId = null
            )
            is TransactionEditorIntent.CommentChanged -> state.copy(
                comment = intent.comment,
                formMessageResId = null
            )
            is TransactionEditorIntent.Open,
            TransactionEditorIntent.ConfirmClicked,
            TransactionEditorIntent.DismissRequested,
            TransactionEditorIntent.RetryClicked -> state
        }
    }

    @StringRes
    fun validationMessage(state: TransactionEditorState): Int? {
        return when {
            state.amount.isBlank() -> R.string.transaction_amount_required
            state.amount.toBigDecimalOrNull()?.signum() != 1 -> {
                R.string.transaction_amount_positive
            }
            state.selectedCategory == null -> R.string.transaction_category_required
            state.effectiveAccount == null -> R.string.transaction_account_required
            else -> null
        }
    }
}

private fun String.normalizedIntegerAmount(): String {
    val digits = filter(Char::isDigit)
    val normalizedDigits = digits.trimStart(LEADING_ZERO)

    return normalizedDigits.ifEmpty {
        if (digits.isEmpty()) "" else ZERO_AMOUNT_VALUE
    }
}

private const val LEADING_ZERO = '0'
private const val ZERO_AMOUNT_VALUE = "0"
