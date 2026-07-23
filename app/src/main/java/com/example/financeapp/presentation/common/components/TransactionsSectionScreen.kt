package com.example.financeapp.presentation.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financeapp.presentation.common.model.TransactionsSectionState
import com.example.financeapp.presentation.common.model.toRouteScreenItem

@Composable
fun TransactionsSectionScreen(
    state: TransactionsSectionState,
    totalLabel: String,
    emptyMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    RouteScreenContent(
        modifier = modifier,
        totalLabel = totalLabel,
        total = state.total,
        items = state.transactions.map { transaction ->
            transaction.toRouteScreenItem(state.categoriesById)
        },
        emptyMessage = emptyMessage,
        isLoading = state.isLoading,
        error = state.error,
        onRetryClick = onRetry,
        onRefresh = onRetry
    )
}
