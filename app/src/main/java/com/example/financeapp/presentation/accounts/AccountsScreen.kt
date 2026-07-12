package com.example.financeapp.presentation.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.presentation.common.components.MainFinanceScreen
import com.example.financeapp.presentation.common.model.FinanceListItemUiModel
import com.example.financeapp.presentation.common.utils.formatWithoutMinorUnits

@Composable
fun AccountsScreen(
    state: AccountsState,
    onIntent: (AccountsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    MainFinanceScreen(
        modifier = modifier,
        totalLabel = stringResource(R.string.balance_total_label),
        total = state.totalBalance,
        items = state.accounts.map { account ->
            FinanceListItemUiModel(
                id = account.id.toString(),
                title = account.name,
                leadingEmoji = account.emoji,
                trailingText = account.balance.formatWithoutMinorUnits()
            )
        },
        emptyMessage = stringResource(R.string.empty_accounts),
        isLoading = state.isLoading,
        error = state.error,
        onRetryClick = { onIntent(AccountsIntent.Retry) },
        onItemClick = { id -> onIntent(AccountsIntent.AccountClicked(id.toLong())) }
    )
}
