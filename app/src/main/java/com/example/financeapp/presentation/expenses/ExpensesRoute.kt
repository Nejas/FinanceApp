package com.example.financeapp.presentation.expenses

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExpensesRoute(
    state: ExpensesState,
    onRetry: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onTransactionDeleteRequest: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExpensesScreen(
        modifier = modifier,
        state = state,
        onIntent = { intent ->
            when (intent) {
                is ExpensesIntent.TransactionClicked -> {
                    onTransactionClick(intent.transactionId)
                }
                is ExpensesIntent.TransactionDeleteRequested -> {
                    onTransactionDeleteRequest(intent.transactionId)
                }
                ExpensesIntent.Retry -> onRetry()
            }
        }
    )
}
