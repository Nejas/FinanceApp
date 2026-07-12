package com.example.financeapp.presentation.income

import java.time.LocalDate

sealed interface IncomeIntent {
    data object Load : IncomeIntent
    data object Retry : IncomeIntent
    data object AnalyticsClicked : IncomeIntent
    data object SettingsClicked : IncomeIntent
    data class DateSelected(val date: LocalDate) : IncomeIntent
    data class TransactionClicked(val transactionId: Long) : IncomeIntent
}
