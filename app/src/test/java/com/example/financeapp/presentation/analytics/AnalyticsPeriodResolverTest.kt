package com.example.financeapp.presentation.analytics

import com.example.financeapp.presentation.analytics.bottomsheets.period.AnalyticsPeriodType
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsPeriodResolverTest {

    private val resolver = AnalyticsPeriodResolver(
        clock = Clock.fixed(
            Instant.parse("2026-07-23T10:00:00Z"),
            ZoneOffset.UTC
        )
    )

    @Test
    fun defaultPeriodFilter_returnsCurrentMonthToToday() {
        val periodFilter = resolver.defaultPeriodFilter()

        assertEquals(AnalyticsPeriodType.Month, periodFilter.selectedType)
        assertEquals(LocalDate.of(2026, 7, 1), periodFilter.startDate)
        assertEquals(LocalDate.of(2026, 7, 23), periodFilter.endDate)
    }

    @Test
    fun resolvePeriod_returnsRangeEndingToday() {
        val current = resolver.defaultPeriodFilter()

        val periodFilter = resolver.resolvePeriod(
            periodType = AnalyticsPeriodType.Week,
            currentPeriodFilter = current
        )

        assertEquals(AnalyticsPeriodType.Week, periodFilter.selectedType)
        assertEquals(LocalDate.of(2026, 7, 17), periodFilter.startDate)
        assertEquals(LocalDate.of(2026, 7, 23), periodFilter.endDate)
    }

    @Test
    fun resolveCustomPeriod_ordersDates() {
        val periodFilter = resolver.resolveCustomPeriod(
            startDate = LocalDate.of(2026, 7, 20),
            endDate = LocalDate.of(2026, 7, 10)
        )

        assertEquals(AnalyticsPeriodType.Custom, periodFilter.selectedType)
        assertEquals(LocalDate.of(2026, 7, 10), periodFilter.startDate)
        assertEquals(LocalDate.of(2026, 7, 20), periodFilter.endDate)
    }
}
