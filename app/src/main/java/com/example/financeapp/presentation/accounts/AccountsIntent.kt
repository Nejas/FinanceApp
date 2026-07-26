package com.example.financeapp.presentation.accounts

sealed interface AccountsIntent {
    data class AccountClick(val accountId: Long) : AccountsIntent
    data class AccountDeleteRequested(val accountId: Long) : AccountsIntent
    data object Retry : AccountsIntent
}
