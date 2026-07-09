package com.example.financeapp.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DateRangeTest {

    @Test
    fun dateRange_acceptsSingleDayPeriod() {
        val date = LocalDate.of(2026, 7, 12)

        val range = DateRange(startDate = date, endDate = date)

        assertEquals(date, range.startDate)
        assertEquals(date, range.endDate)
    }

    @Test
    fun dateRange_rejectsEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException::class.java) {
            DateRange(
                startDate = LocalDate.of(2026, 7, 12),
                endDate = LocalDate.of(2026, 7, 11)
            )
        }
    }
}
