package com.example.financeapp.presentation.accounts

sealed interface AccountsIntent {
    data object Retry : AccountsIntent
}
