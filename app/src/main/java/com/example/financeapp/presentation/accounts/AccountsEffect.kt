package com.example.financeapp.presentation.accounts

sealed interface AccountsEffect {
    data object OpenAnalytics : AccountsEffect
    data object OpenSettings : AccountsEffect
    data class OpenAccount(val accountId: Long) : AccountsEffect
}
