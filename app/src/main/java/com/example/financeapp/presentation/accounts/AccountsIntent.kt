package com.example.financeapp.presentation.accounts

sealed interface AccountsIntent {
    data object Load : AccountsIntent
    data object Retry : AccountsIntent
    data object AnalyticsClicked : AccountsIntent
    data object SettingsClicked : AccountsIntent
    data class AccountClicked(val accountId: Long) : AccountsIntent
}
