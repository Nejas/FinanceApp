package com.example.financeapp.presentation.income

sealed interface IncomeEffect {
    data object OpenAnalytics : IncomeEffect
    data object OpenSettings : IncomeEffect
    data class OpenTransaction(val transactionId: Long) : IncomeEffect
}
