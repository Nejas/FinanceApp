package com.example.financeapp.presentation.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financeapp.presentation.common.model.TransactionsSectionState
import com.example.financeapp.presentation.common.model.toFinanceListItemUiModel

@Composable
fun TransactionsSectionScreen(
    state: TransactionsSectionState,
    totalLabel: String,
    emptyMessage: String,
    onRetry: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onTransactionDeleteRequest: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    RouteScreenContent(
        modifier = modifier,
        totalLabel = totalLabel,
        total = state.total,
        items = state.transactions.map { transaction ->
            transaction.toFinanceListItemUiModel(state.categoriesById)
        },
        emptyMessage = emptyMessage,
        isLoading = state.isLoading,
        error = state.error,
        onRetryClick = onRetry,
        onRefresh = onRetry,
        onItemClick = { item ->
            item.id.toLongOrNull()?.let(onTransactionClick)
        },
        onItemDeleteRequest = { item ->
            item.id.toLongOrNull()?.let(onTransactionDeleteRequest)
        }
    )
}
