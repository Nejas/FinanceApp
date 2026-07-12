package com.example.financeapp.presentation.income

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.presentation.common.components.MainFinanceScreen
import com.example.financeapp.presentation.common.model.FinanceListItemUiModel
import com.example.financeapp.presentation.common.utils.formatWithoutMinorUnits

@Composable
fun IncomeScreen(
    state: IncomeState,
    onIntent: (IncomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    MainFinanceScreen(
        modifier = modifier,
        totalLabel = stringResource(R.string.income_total_label),
        total = state.total,
        items = state.transactions.map { transaction ->
            FinanceListItemUiModel(
                id = transaction.id.toString(),
                title = transaction.title,
                leadingEmoji = state.categoriesById[transaction.categoryId]?.emoji.orEmpty(),
                trailingText = transaction.amount.formatWithoutMinorUnits()
            )
        },
        emptyMessage = stringResource(R.string.empty_income),
        isLoading = state.isLoading,
        error = state.error,
        onRetryClick = { onIntent(IncomeIntent.Retry) },
        onItemClick = { id -> onIntent(IncomeIntent.TransactionClicked(id.toLong())) }
    )
}
