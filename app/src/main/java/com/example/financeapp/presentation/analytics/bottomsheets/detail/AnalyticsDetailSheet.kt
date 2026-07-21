package com.example.financeapp.presentation.analytics.bottomsheets.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.R
import com.example.financeapp.core.theme.AnalyticsDetailPercentTextStyle
import com.example.financeapp.core.theme.AnalyticsDetailTitleTextStyle
import com.example.financeapp.core.theme.AnalyticsDetailTotalTextStyle
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.AnalyticsCategorySummary
import com.example.financeapp.domain.model.Money
import com.example.financeapp.presentation.analytics.AnalyticsCategoryColorMapper
import com.example.financeapp.presentation.analytics.AnalyticsDonutChart
import com.example.financeapp.presentation.common.utils.formatWithoutMinorUnits

@Composable
fun AnalyticsDetailSheet(
    total: Money,
    categories: List<AnalyticsCategorySummary>,
    categoryColors: Map<Long, Color>,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(sizing.analyticsDetailSheetMaxHeightFraction)
            .padding(
                bottom = spacing.analyticsDetailBottomPadding,
                start = spacing.actionGap,
                end = spacing.actionGap
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.analyticsDetailContentGap)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.analytics_detail_title),
            style = AnalyticsDetailTitleTextStyle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        AnalyticsDonutChart(
            categories = categories,
            total = total,
            categoryColors = categoryColors,
            chartSize = sizing.analyticsDetailChartSize,
            strokeWidth = sizing.analyticsDetailChartStroke,
            centerLabelStyle = MaterialTheme.typography.titleMedium,
            centerTotalStyle = AnalyticsDetailTotalTextStyle,
            centerTextGap = spacing.xxs
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.analyticsDetailCategoryGap)
        ) {
            items(
                items = categories,
                key = { category -> category.categoryId }
            ) { category ->
                AnalyticsDetailCategoryRow(
                    category = category,
                    color = categoryColors.getValue(category.categoryId)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsDetailCategoryRow(
    category: AnalyticsCategorySummary,
    color: Color,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.analyticsDetailProgressTop)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(spacing.analyticsDetailCategoryLabelGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(sizing.analyticsDetailCategoryDot)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(spacing.analyticsDetailCategoryLabelGap))
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.analyticsDetailCategoryLabelGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.amount.formatWithoutMinorUnits(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "(${category.percent}%)",
                    style = AnalyticsDetailPercentTextStyle,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
        LinearProgressIndicator(
            progress = { category.percent / PercentTotal },
            modifier = Modifier
                .fillMaxWidth()
                .height(sizing.analyticsDetailProgressHeight)
                .clip(RoundedCornerShape(sizing.analyticsDetailProgressHeight)),
            color = color,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            strokeCap = StrokeCap.Butt,
            gapSize = spacing.none,
            drawStopIndicator = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun AnalyticsDetailSheetPreview() {
    FinanceAppTheme(dynamicColor = false) {
        val categories = listOf(
            AnalyticsCategorySummary(1, "Ремонт", "🔧", Money(amountInMinorUnits = 80_200L * 100), 61),
            AnalyticsCategorySummary(2, "Авто", "🚗", Money(amountInMinorUnits = 31_500L * 100), 24),
            AnalyticsCategorySummary(3, "Другое", "📦", Money(amountInMinorUnits = 20_544L * 100), 15)
        )

        AnalyticsDetailSheet(
            total = Money(amountInMinorUnits = 132_244L * 100),
            categories = categories,
            categoryColors = AnalyticsCategoryColorMapper().map(categories)
        )
    }
}

private const val PercentTotal = 100f
