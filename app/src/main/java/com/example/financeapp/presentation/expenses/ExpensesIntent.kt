package com.example.financeapp.presentation.expenses

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ExpensesIntent {
    data class TransactionClicked(val transactionId: Long) : ExpensesIntent
    data class TransactionDeleteRequested(val transactionId: Long) : ExpensesIntent
    data object Retry : ExpensesIntent
}
