package com.example.financeapp.presentation.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AccountsRoute(
    state: AccountsState,
    onRetry: () -> Unit,
    onAccountClick: (Long) -> Unit,
    onAccountDeleteRequest: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    AccountsScreen(
        modifier = modifier,
        state = state,
        onIntent = { intent ->
            when (intent) {
                is AccountsIntent.AccountClick -> onAccountClick(intent.accountId)
                is AccountsIntent.AccountDeleteRequested -> {
                    onAccountDeleteRequest(intent.accountId)
                }
                AccountsIntent.Retry -> onRetry()
            }
        }
    )
}
