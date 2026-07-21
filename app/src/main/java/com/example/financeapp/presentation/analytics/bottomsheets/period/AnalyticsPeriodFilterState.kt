package com.example.financeapp.presentation.analytics.bottomsheets.period

import androidx.annotation.StringRes
import com.example.financeapp.R
import java.time.LocalDate

data class AnalyticsPeriodFilterState(
    val selectedType: AnalyticsPeriodType,
    val startDate: LocalDate,
    val endDate: LocalDate
)

enum class AnalyticsPeriodType(@StringRes val titleResId: Int) {
    Custom(R.string.analytics_period_custom),
    Week(R.string.analytics_period_week),
    Month(R.string.analytics_period_month),
    Quarter(R.string.analytics_period_quarter),
    Year(R.string.analytics_period_year)
}

fun defaultAnalyticsPeriodFilterState(today: LocalDate): AnalyticsPeriodFilterState {
    return AnalyticsPeriodFilterState(
        selectedType = AnalyticsPeriodType.Month,
        startDate = today.withDayOfMonth(1),
        endDate = today
    )
}
