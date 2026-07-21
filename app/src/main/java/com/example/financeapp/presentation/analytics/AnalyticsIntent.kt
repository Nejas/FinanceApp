package com.example.financeapp.presentation.analytics

import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.presentation.analytics.bottomsheets.AnalyticsFilterType
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodType
import java.time.LocalDate

sealed interface AnalyticsIntent {
    data object BackClicked : AnalyticsIntent
    data object Retry : AnalyticsIntent
    data object ChartClicked : AnalyticsIntent
    data object DetailDismissed : AnalyticsIntent
    data class FilterClicked(val type: AnalyticsFilterType) : AnalyticsIntent
    data object FilterDismissed : AnalyticsIntent
    data class TypeApplied(val type: TransactionType?) : AnalyticsIntent
    data class PeriodSelected(val periodType: AnalyticsPeriodType) : AnalyticsIntent
    data class CustomPeriodApplied(
        val startDate: LocalDate,
        val endDate: LocalDate
    ) : AnalyticsIntent
    data object CustomPeriodCancelClicked : AnalyticsIntent
    data class CategorySelectionApplied(val categoryIds: Set<Long>) : AnalyticsIntent
    data class AccountSelected(val accountId: Long?) : AnalyticsIntent
}
