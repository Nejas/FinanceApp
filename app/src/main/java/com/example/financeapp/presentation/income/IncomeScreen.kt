package com.example.financeapp.presentation.income

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.presentation.common.components.RouteScreenContent
import com.example.financeapp.presentation.common.model.RouteScreenItem

@Composable
fun IncomeScreen(
    state: IncomeState,
    onIntent: (IncomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    RouteScreenContent(
        modifier = modifier,
        totalLabel = stringResource(R.string.income_total_label),
        total = state.total,
        items = state.transactions.map { transaction ->
            RouteScreenItem(
                id = transaction.id.toString(),
                title = transaction.title,
                leadingEmoji = state.categoriesById[transaction.categoryId]?.emoji.orEmpty(),
                comment = transaction.comment,
                money = transaction.amount
            )
        },
        emptyMessage = stringResource(R.string.empty_income),
        isLoading = state.isLoading,
        error = state.error,
        onRetryClick = { onIntent(IncomeIntent.Retry) },
        onRefresh = { onIntent(IncomeIntent.Retry) }
    )
}
