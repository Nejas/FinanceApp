package com.example.financeapp.presentation.analytics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.R
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import com.example.financeapp.presentation.analytics.mappers.AnalyticsCategoryColorMapper
import com.example.financeapp.presentation.analytics.AnalyticsCategoryUi
import com.example.financeapp.presentation.analytics.TransactionAnalysisCriteriaUi
import com.example.financeapp.presentation.analytics.AnalyticsIntent
import com.example.financeapp.presentation.analytics.AnalyticsFilterField
import com.example.financeapp.presentation.analytics.AnalyticsState
import com.example.financeapp.presentation.bottomSheets.analytics.AnalyticsBottomSheet
import com.example.financeapp.presentation.bottomSheets.components.period.AnalyticsPeriodType
import com.example.financeapp.presentation.bottomSheets.components.period.defaultAnalyticsPeriodFilterState
import com.example.financeapp.presentation.analytics.defaultTransactionAnalysisCriteria
import com.example.financeapp.presentation.common.components.FinanceFieldIcon
import com.example.financeapp.presentation.common.components.base.FinancePullToRefreshBox
import com.example.financeapp.presentation.common.components.base.ListItemColumn
import com.example.financeapp.presentation.common.components.base.RoundFrame
import com.example.financeapp.presentation.common.components.base.TextOvalFrame
import com.example.financeapp.presentation.common.model.FinanceListItemUiModel
import com.example.financeapp.presentation.common.placeholders.EmptyContent
import com.example.financeapp.presentation.common.placeholders.ErrorContent
import com.example.financeapp.presentation.common.placeholders.LoadingContent
import java.time.LocalDate

@Composable
fun AnalyticsScreen(
    state: AnalyticsState,
    onIntent: (AnalyticsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FinancePullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                isRefreshing = state.isLoading,
                onRefresh = { onIntent(AnalyticsIntent.Retry) }
            ) {
                when {
                    state.isLoading -> LoadingContent(modifier = Modifier.fillMaxSize())
                    state.error != null -> ErrorContent(
                        error = state.error,
                        onRetryClick = { onIntent(AnalyticsIntent.Retry) },
                        modifier = Modifier.fillMaxSize()
                    )
                else -> AnalyticsContent(
                        state = state,
                        onFilterClick = { type -> onIntent(AnalyticsIntent.FilterClicked(type)) },
                        onChartClick = { onIntent(AnalyticsIntent.ChartClicked) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    AnalyticsBottomSheet(
        state = state,
        onIntent = onIntent
    )
}
@Composable
private fun AnalyticsChartSurface(
    state: AnalyticsState,
    onChartClick: () -> Unit,
    modifier: Modifier = Modifier)
{
    val spacing = LocalSpacing.current

    Column(modifier = modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onChartClick)){
            AnalyticsDonutChart(
                categories = state.categories,
                total = state.total,
                categoryColors = state.categoryColors,
                modifier = Modifier
                    .padding(
                        top = spacing.analyticsChartTop,
                        bottom = spacing.analyticsLegendTop
                    ),

                )
        }
            AnalyticsLegend(
                categories = state.categories,
                categoryColors = state.categoryColors,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = spacing.analyticsLegendBottom)
            )

    }
}
@Composable
private fun AnalyticsContent(
    state: AnalyticsState,
    onFilterClick: (AnalyticsFilterField) -> Unit,
    onChartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.analyticsBottomPadding)
    ) {
        item {
            AnalyticsChartSurface(
                state = state,
                onChartClick = onChartClick
            )
        }

        itemsIndexed(
            items = state.filters,
            key = { _, filter -> filter.field.saveableKey }
        ) { index, filter ->
            TransactionAnalysisCriteriaRow(
                filter = filter,
                onClick = { onFilterClick(filter.field) },
                showDivider = index != state.filters.lastIndex
            )
        }

        item {
            Text(
                modifier = Modifier.padding(
                    start = spacing.analyticsContentHorizontal,
                    top = spacing.analyticsLegendTop,
                    bottom = spacing.contentGap
                ),
                text = stringResource(R.string.analytics_transactions_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (state.isEmpty) {
            item {
                EmptyContent(
                    message = stringResource(R.string.analytics_empty),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.sm)
                )
            }
        } else {
            items(
                items = state.transactions,
                key = { transaction -> transaction.id }
            ) { transaction ->
                ListItemColumn(
                    item = transaction,
                    modifier = Modifier.height(sizing.listItemHeight)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsLegend(
    categories: List<AnalyticsCategoryUi>,
    categoryColors: Map<Long, Color>,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Row(
        modifier = modifier.padding(horizontal = spacing.md),
        horizontalArrangement = Arrangement.spacedBy(
            space = spacing.lg,
            alignment = Alignment.CenterHorizontally)
    ){
        categories.forEach { category ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(sizing.analyticsLegendDot)
                        .clip(CircleShape)
                        .background(categoryColors.getValue(category.categoryId))
                )
                Spacer(modifier = Modifier.width(spacing.s))
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TransactionAnalysisCriteriaRow(
    filter: TransactionAnalysisCriteriaUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .height(sizing.analyticsFilterRowHeight)
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = spacing.analyticsContentHorizontal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundFrame(
                size = sizing.analyticsFilterIcon,
                content = {
                    FinanceFieldIcon(
                        icon = filter.field.icon,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(sizing.analyticsFilterInnerIcon)
                    )
                }
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = spacing.md, end = spacing.xxs),
                text = stringResource(filter.field.titleResId),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            TextOvalFrame(
                text = filter.resolveValue(),
                backgroundColor = MaterialTheme.colorScheme.background,
                maxWidth = sizing.analyticsFilterValueMaxWidth
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                thickness = spacing.hairline
            )
        }
    }
}

@Composable
private fun TransactionAnalysisCriteriaUi.resolveValue(): String {
    return valueResId?.let { resId -> stringResource(resId) } ?: value
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun AnalyticsScreenPreview() {
    FinanceAppTheme(dynamicColor = false) {
        val previewPeriodFilter = defaultAnalyticsPeriodFilterState(today = LocalDate.of(2026, 2, 5))
            .copy(selectedType = AnalyticsPeriodType.Month)
        val previewFilter = defaultTransactionAnalysisCriteria(
            periodFilter = previewPeriodFilter,
            currency = Currency.RUB
        )
        val categories = listOf(
            AnalyticsCategoryUi(1, "Ремонт", "🔧", Money(amountInMinorUnits = 80_200L * 100), 61),
            AnalyticsCategoryUi(2, "Авто", "🚗", Money(amountInMinorUnits = 31_500L * 100), 24),
            AnalyticsCategoryUi(3, "Другое", "📦", Money(amountInMinorUnits = 20_544L * 100), 15)
        )
        AnalyticsScreen(
            state = AnalyticsState(
                filter = previewFilter,
                periodFilter = previewPeriodFilter,
                total = Money(amountInMinorUnits = 132_244L * 100),
                categories = categories,
                categoryColors = AnalyticsCategoryColorMapper().map(categories),
                filters = listOf(
                    TransactionAnalysisCriteriaUi(
                        field = AnalyticsFilterField.TransactionType,
                        valueResId = R.string.analytics_filter_expenses
                    ),
                    TransactionAnalysisCriteriaUi(
                        field = AnalyticsFilterField.Period,
                        valueResId = previewPeriodFilter.selectedType.titleResId
                    ),
                    TransactionAnalysisCriteriaUi(
                        field = AnalyticsFilterField.Category,
                        value = "Ремонт, Авто"
                    ),
                    TransactionAnalysisCriteriaUi(
                        field = AnalyticsFilterField.Account,
                        valueResId = R.string.analytics_filter_all_accounts
                    )
                ),
                transactions = listOf(
                    FinanceListItemUiModel(
                        "1",
                        "На собачку",
                        "🐶",
                        "Джек",
                        Money(amountInMinorUnits = 123_322L * 100)
                    ),
                    FinanceListItemUiModel(
                        "2",
                        "Покупки в магазине",
                        "🛒",
                        "Сбербанк",
                        Money(amountInMinorUnits = 45_100L * 100)
                    )
                )
            ),
            onIntent = {}
        )
    }
}
