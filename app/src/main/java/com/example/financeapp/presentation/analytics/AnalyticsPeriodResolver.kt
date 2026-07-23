package com.example.financeapp.presentation.analytics

import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodFilterState
import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodType
import com.example.financeapp.presentation.analytics.bottomsheets.period.defaultAnalyticsPeriodFilterState
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class AnalyticsPeriodResolver @Inject constructor(
    private val clock: Clock
) {

    fun defaultPeriodFilter(): AnalyticsPeriodFilterState {
        return defaultAnalyticsPeriodFilterState(today = today())
    }

    fun resolvePeriod(
        periodType: AnalyticsPeriodType,
        currentPeriodFilter: AnalyticsPeriodFilterState
    ): AnalyticsPeriodFilterState {
        if (periodType == AnalyticsPeriodType.Custom) {
            return currentPeriodFilter.copy(selectedType = AnalyticsPeriodType.Custom)
        }

        val range = periodType.rangeEndingAt(today(), currentPeriodFilter)
        return AnalyticsPeriodFilterState(
            selectedType = periodType,
            startDate = range.startDate,
            endDate = range.endDate
        )
    }

    fun resolveCustomPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): AnalyticsPeriodFilterState {
        return AnalyticsPeriodFilterState(
            selectedType = AnalyticsPeriodType.Custom,
            startDate = minOf(startDate, endDate),
            endDate = maxOf(startDate, endDate)
        )
    }

    private fun AnalyticsPeriodType.rangeEndingAt(
        endDate: LocalDate,
        currentPeriodFilter: AnalyticsPeriodFilterState
    ): DateRange {
        val startDate = when (this) {
            AnalyticsPeriodType.Custom -> currentPeriodFilter.startDate
            AnalyticsPeriodType.Week -> endDate.minusDays(6)
            AnalyticsPeriodType.Month -> endDate.minusMonths(1).plusDays(1)
            AnalyticsPeriodType.Quarter -> endDate.minusMonths(3).plusDays(1)
            AnalyticsPeriodType.Year -> endDate.minusYears(1).plusDays(1)
        }
        return DateRange(startDate = startDate, endDate = endDate)
    }

    private fun today(): LocalDate {
        return LocalDate.now(clock)
    }

    private data class DateRange(
        val startDate: LocalDate,
        val endDate: LocalDate
    )
}
