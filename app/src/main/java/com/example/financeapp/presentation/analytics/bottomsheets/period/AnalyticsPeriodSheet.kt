package com.example.financeapp.presentation.analytics.bottomsheets.period

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType
import com.example.financeapp.presentation.common.components.base.FinanceSelectionRow
import com.example.financeapp.presentation.common.components.base.FinanceSelectionSheetScaffold
import java.time.format.DateTimeFormatter

@Composable
internal fun AnalyticsPeriodSheet(
    periodFilter: AnalyticsPeriodFilterState,
    onPeriodClick: (AnalyticsPeriodType) -> Unit
) {
    FinanceSelectionSheetScaffold(title = stringResource(R.string.analytics_filter_period)) {
        AnalyticsPeriodOptions.forEach { periodType ->
            FinanceSelectionRow(
                title = stringResource(periodType.titleResId),
                subtitle = if (periodType == AnalyticsPeriodType.Custom) {
                    periodFilter.formattedFilterPeriod()
                } else {
                    null
                },
                isSelected = periodFilter.selectedType == periodType,
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onClick = { onPeriodClick(periodType) }
            )
        }
        Spacer(modifier = Modifier.height(LocalSpacing.current.xl))
    }
}

private fun AnalyticsPeriodFilterState.formattedFilterPeriod(): String {
    return "${startDate.format(FilterDateFormatter)} – ${endDate.format(FilterDateFormatter)}"
}

private val AnalyticsPeriodOptions = listOf(
    AnalyticsPeriodType.Custom,
    AnalyticsPeriodType.Week,
    AnalyticsPeriodType.Month,
    AnalyticsPeriodType.Quarter,
    AnalyticsPeriodType.Year
)

private val FilterDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
