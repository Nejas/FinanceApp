package com.example.financeapp.presentation.expenses

import java.time.LocalDate

sealed interface ExpensesIntent {
    data object Load : ExpensesIntent
    data object Retry : ExpensesIntent
    data object AnalyticsClicked : ExpensesIntent
    data object SettingsClicked : ExpensesIntent
    data class DateSelected(val date: LocalDate) : ExpensesIntent
    data class TransactionClicked(val transactionId: Long) : ExpensesIntent
}
