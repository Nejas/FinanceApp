package com.example.financeapp.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.Money
import com.example.financeapp.presentation.common.model.FinanceListItemUiModel
import com.example.financeapp.presentation.common.mvi.ScreenError
import com.example.financeapp.presentation.common.utils.formatWithoutMinorUnits

@Composable
fun MainFinanceScreen(
    totalLabel: String,
    total: Money,
    items: List<FinanceListItemUiModel>,
    emptyMessage: String,
    isLoading: Boolean,
    error: ScreenError?,
    onRetryClick: () -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Column(modifier = modifier.fillMaxSize()) {
        TotalSumSurface(
            totalLabel = totalLabel,
            totalSum = total.formatWithoutMinorUnits()
        )
        when {
            isLoading -> LoadingContent(modifier = Modifier.weight(1f))
            error != null -> ErrorContent(
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
                    FinanceListItem(
                        item = item,
                        onClick = onItemClick,
                        modifier = Modifier.height(sizing.listItemHeight)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainFinanceScreenPreview() {
    FinanceAppTheme(dynamicColor = false) {
        MainFinanceScreen(
            totalLabel = "Расходы сегодня",
            total = Money(amountInMinorUnits = 125000),
            items = previewFinanceItems,
            emptyMessage = "Нет операций за выбранный день",
            isLoading = false,
            error = null,
            onRetryClick = {},
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainFinanceScreenEmptyPreview() {
    FinanceAppTheme(dynamicColor = false) {
        MainFinanceScreen(
            totalLabel = "Поступления сегодня",
            total = Money(amountInMinorUnits = 0),
            items = emptyList(),
            emptyMessage = "Нет поступлений за выбранный день",
            isLoading = false,
            error = null,
            onRetryClick = {},
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainFinanceScreenErrorPreview() {
    FinanceAppTheme(dynamicColor = false) {
        MainFinanceScreen(
            totalLabel = "Счета",
            total = Money(amountInMinorUnits = 0),
            items = emptyList(),
            emptyMessage = "Нет счетов",
            isLoading = false,
            error = ScreenError.LOAD_FAILED,
            onRetryClick = {},
            onItemClick = {}
        )
    }
}

private val previewFinanceItems = listOf(
    FinanceListItemUiModel(
        id = "food",
        title = "Продукты",
        leadingEmoji = "🛒",
        trailingText = "-1 250 ₽"
    ),
    FinanceListItemUiModel(
        id = "transport",
        title = "Транспорт",
        leadingEmoji = "🚇",
        trailingText = "-64 ₽"
    ),
    FinanceListItemUiModel(
        id = "salary",
        title = "Зарплата",
        leadingEmoji = "💼",
        trailingText = "+95 000 ₽"
    )
)
