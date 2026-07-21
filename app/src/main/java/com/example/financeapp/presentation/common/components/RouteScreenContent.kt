package com.example.financeapp.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.Money
import com.example.financeapp.presentation.common.components.base.FinancePullToRefreshBox
import com.example.financeapp.presentation.common.components.base.ListItemColumn
import com.example.financeapp.presentation.common.components.base.TotalSumSurface
import com.example.financeapp.presentation.common.model.RouteScreenItem
import com.example.financeapp.presentation.common.placeholders.EmptyContent
import com.example.financeapp.presentation.common.placeholders.ErrorContent
import com.example.financeapp.presentation.common.placeholders.LoadingContent
import com.example.financeapp.presentation.common.placeholders.ScreenError
import com.example.financeapp.presentation.common.utils.formatWithoutMinorUnits

@Composable
fun RouteScreenContent(
    totalLabel: String,
    total: Money,
    items: List<RouteScreenItem>,
    emptyMessage: String,
    isLoading: Boolean,
    error: ScreenError?,
    onRetryClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    FinancePullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = isLoading,
        onRefresh = onRefresh
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TotalSumSurface(
                totalLabel = totalLabel,
                totalSum = total.formatWithoutMinorUnits()
            )
            when {
                isLoading -> LoadingContent(modifier = Modifier.weight(1f))
                error != null -> ErrorContent(
                    error = error,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(1f)
                )
                items.isEmpty() -> EmptyContent(
                    message = emptyMessage,
                    modifier = Modifier.weight(1f)
                )
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = spacing.listBottomPadding),
                    verticalArrangement = Arrangement.spacedBy(spacing.none)
                ) {
                    items(items = items, key = { it.id }) { item ->
                        ListItemColumn(
                            item = item,
                            modifier = Modifier.height(sizing.listItemHeight)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainFinanceScreenPreview() {
    FinanceAppTheme(dynamicColor = false) {
        RouteScreenContent(
            totalLabel = "Расходы сегодня",
            total = Money(amountInMinorUnits = 125000),
            items = previewFinanceItems,
            emptyMessage = "Нет операций за выбранный день",
            isLoading = false,
            error = null,
            onRetryClick = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainFinanceScreenEmptyPreview() {
    FinanceAppTheme(dynamicColor = false) {
        RouteScreenContent(
            totalLabel = "Поступления сегодня",
            total = Money(amountInMinorUnits = 0),
            items = emptyList(),
            emptyMessage = "Нет поступлений за выбранный день",
            isLoading = false,
            error = null,
            onRetryClick = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainFinanceScreenErrorPreview() {
    FinanceAppTheme(dynamicColor = false) {
        RouteScreenContent(
            totalLabel = "Счета",
            total = Money(amountInMinorUnits = 0),
            items = emptyList(),
            emptyMessage = "Нет счетов",
            isLoading = false,
            error = ScreenError.LOAD_FAILED,
            onRetryClick = {},
            onRefresh = {}
        )
    }
}

private val previewFinanceItems = listOf(
    RouteScreenItem(
        id = "food",
        title = "Продукты",
        leadingEmoji = "🛒",
        comment = "Сбербанк",
        money = Money(amountInMinorUnits = 125000)
    ),
    RouteScreenItem(
        id = "transport",
        title = "Транспорт",
        leadingEmoji = "🚇",
        comment = "Наличные",
        money = Money(amountInMinorUnits = 6400)
    ),
    RouteScreenItem(
        id = "salary",
        title = "Зарплата",
        leadingEmoji = "💼",
        comment = "Основной счет",
        money = Money(amountInMinorUnits = 9500000)
    )
)
