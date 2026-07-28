package com.example.financeapp.presentation.accounts

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AccountsIntent {
    data class AccountClick(val accountId: Long) : AccountsIntent
    data class AccountDeleteRequested(val accountId: Long) : AccountsIntent
    data object Retry : AccountsIntent
}
