package com.example.financeapp.presentation.expenses

sealed interface ExpensesEffect {
    data object OpenAnalytics : ExpensesEffect
    data object OpenSettings : ExpensesEffect
    data class OpenTransaction(val transactionId: Long) : ExpensesEffect
}
