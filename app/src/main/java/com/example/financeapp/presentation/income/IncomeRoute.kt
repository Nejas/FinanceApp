package com.example.financeapp.presentation.income

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun IncomeRoute(
    state: IncomeState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IncomeScreen(
        modifier = modifier,
        state = state,
        onIntent = { intent ->
            when (intent) {
                IncomeIntent.Retry -> onRetry()
            }
        }
    )
}
