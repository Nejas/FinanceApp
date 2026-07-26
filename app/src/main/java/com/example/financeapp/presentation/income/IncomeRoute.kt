package com.example.financeapp.presentation.income

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun IncomeRoute(
    state: IncomeState,
    onRetry: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onTransactionDeleteRequest: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    IncomeScreen(
        modifier = modifier,
        state = state,
        onIntent = { intent ->
            when (intent) {
                is IncomeIntent.TransactionClicked -> {
                    onTransactionClick(intent.transactionId)
                }
                is IncomeIntent.TransactionDeleteRequested -> {
                    onTransactionDeleteRequest(intent.transactionId)
                }
                IncomeIntent.Retry -> onRetry()
            }
        }
    )
}
