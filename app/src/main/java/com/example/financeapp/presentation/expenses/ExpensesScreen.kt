package com.example.financeapp.presentation.expenses

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.presentation.common.components.TransactionsSectionScreen

@Composable
fun ExpensesScreen(
    state: ExpensesState,
    onIntent: (ExpensesIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    TransactionsSectionScreen(
        modifier = modifier,
        state = state,
        totalLabel = stringResource(R.string.expenses_total_label),
        emptyMessage = stringResource(R.string.empty_expenses),
        onRetry = { onIntent(ExpensesIntent.Retry) }
    )
}
