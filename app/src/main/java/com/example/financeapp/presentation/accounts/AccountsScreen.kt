package com.example.financeapp.presentation.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.presentation.common.components.RouteScreenContent
import com.example.financeapp.presentation.common.model.FinanceListItemUiModel

@Composable
fun AccountsScreen(
    state: AccountsState,
    onIntent: (AccountsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    RouteScreenContent(
        modifier = modifier,
        totalLabel = stringResource(R.string.balance_total_label),
        total = state.totalBalance,
        items = state.accounts.map { account ->
            FinanceListItemUiModel(
                id = account.id.toString(),
                title = account.name,
                leadingEmoji = account.emoji,
                comment = account.description,
                money = account.balance
            )
        },
        emptyMessage = stringResource(R.string.empty_accounts),
        isLoading = state.isLoading,
        error = state.error,
        onRetryClick = { onIntent(AccountsIntent.Retry) },
        onRefresh = { onIntent(AccountsIntent.Retry) },
        onItemClick = { item ->
            item.id.toLongOrNull()?.let { accountId ->
                onIntent(AccountsIntent.AccountClick(accountId))
            }
        },
        onItemDeleteRequest = { item ->
            item.id.toLongOrNull()?.let { accountId ->
                onIntent(AccountsIntent.AccountDeleteRequested(accountId))
            }
        }
    )
}
