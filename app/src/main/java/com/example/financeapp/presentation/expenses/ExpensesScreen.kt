package com.example.financeapp.presentation.expenses

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.presentation.common.components.MainFinanceScreen
import com.example.financeapp.presentation.common.model.FinanceListItemUiModel
import com.example.financeapp.presentation.common.utils.formatWithoutMinorUnits

@Composable
fun ExpensesScreen(
    state: ExpensesState,
    onIntent: (ExpensesIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    MainFinanceScreen(
        modifier = modifier,
        totalLabel = stringResource(R.string.expenses_total_label),
        total = state.total,
        items = state.transactions.map { transaction ->
            FinanceListItemUiModel(
                id = transaction.id.toString(),
                title = transaction.title,
                leadingEmoji = state.categoriesById[transaction.categoryId]?.emoji.orEmpty(),
                trailingText = transaction.amount.formatWithoutMinorUnits()
            )
        },
        emptyMessage = stringResource(R.string.empty_expenses),
        isLoading = state.isLoading,
        error = state.error,
        onRetryClick = { onIntent(ExpensesIntent.Retry) },
        onItemClick = { id -> onIntent(ExpensesIntent.TransactionClicked(id.toLong())) }
    )
}
