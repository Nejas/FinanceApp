package com.example.financeapp.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.financeapp.R
import com.example.financeapp.core.theme.FinanceDonuteLabelText
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.domain.model.AnalyticsCategorySummary
import com.example.financeapp.domain.model.Money
import com.example.financeapp.presentation.common.utils.formatWithoutMinorUnits
import java.math.BigDecimal
import java.math.RoundingMode

private const val FullCircleSweep = 360f

@Composable
fun AnalyticsDonutChart(
    categories: List<AnalyticsCategorySummary>,
    total: Money,
    categoryColors: Map<Long, Color>,
    modifier: Modifier = Modifier,
    chartSize: Dp = LocalSizing.current.analyticsChartSize,
    strokeWidth: Dp = LocalSizing.current.analyticsChartStroke,
    centerLabelStyle: TextStyle? = null,
    centerTotalStyle: TextStyle? = null,
    centerTextGap: Dp = 0.dp
) {
    Box(
        modifier = modifier.height(chartSize).width(chartSize),
        contentAlignment = Alignment.Center
    ) {
        DonutChart(
            categories = categories,
            categoryColors = categoryColors,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = strokeWidth
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(centerTextGap)
        ) {
            Text(
                text = stringResource(R.string.analytics_total_for_period),
                style = centerLabelStyle ?: MaterialTheme.typography.bodyMedium,
                color = FinanceDonuteLabelText,
                textAlign = TextAlign.Center
            )
            Text(
                text = total.formatWithoutMinorUnits(),
                style = centerTotalStyle ?: MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DonutChart(
    categories: List<AnalyticsCategorySummary>,
    categoryColors: Map<Long, Color>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = LocalSizing.current.analyticsChartStroke
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Canvas(modifier = modifier) {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            cap = StrokeCap.Butt
        )
        val inset = stroke.width / 2f
        val arcSize = Size(size.width - stroke.width, size.height - stroke.width)

        if (categories.isEmpty()) {
            drawArc(
                color = outlineColor,
                startAngle = 0f,
                sweepAngle = FullCircleSweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
            return@Canvas
        }

        val totalAmount = categories
            .map { category -> category.amount.amount }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            drawArc(
                color = outlineColor,
                startAngle = 0f,
                sweepAngle = FullCircleSweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
            return@Canvas
        }

        var startAngle = -90f
        categories.forEachIndexed { index, item ->
            val sweepAngle = if (index == categories.lastIndex) {
                FullCircleSweep - (startAngle + 90f)
            } else {
                item.amount.amount
                    .divide(totalAmount, ChartDivisionScale, ChartRoundingMode)
                    .toFloat() * FullCircleSweep
            }
            drawArc(
                color = categoryColors.getValue(item.categoryId),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
            startAngle += sweepAngle
        }
    }
}

private const val ChartDivisionScale = 8
private val ChartRoundingMode = RoundingMode.HALF_UP
