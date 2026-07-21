package com.example.financeapp.presentation.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AccountsRoute(
    state: AccountsState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AccountsScreen(
        modifier = modifier,
        state = state,
        onIntent = { intent ->
            when (intent) {
                AccountsIntent.Retry -> onRetry()
            }
        }
    )
}
