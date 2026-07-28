package com.example.financeapp.presentation.income

import androidx.compose.runtime.Immutable

@Immutable
sealed interface IncomeIntent {
    data class TransactionClicked(val transactionId: Long) : IncomeIntent
    data class TransactionDeleteRequested(val transactionId: Long) : IncomeIntent
    data object Retry : IncomeIntent
}
