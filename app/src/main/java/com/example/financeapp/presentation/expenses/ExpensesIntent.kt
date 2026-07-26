package com.example.financeapp.presentation.expenses

sealed interface ExpensesIntent {
    data class TransactionClicked(val transactionId: Long) : ExpensesIntent
    data class TransactionDeleteRequested(val transactionId: Long) : ExpensesIntent
    data object Retry : ExpensesIntent
}
