package com.example.financeapp.presentation.expenses

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExpensesRoute(
    state: ExpensesState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExpensesScreen(
        modifier = modifier,
        state = state,
        onIntent = { intent ->
            when (intent) {
                ExpensesIntent.Retry -> onRetry()
            }
        }
    )
}
