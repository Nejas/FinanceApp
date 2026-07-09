package com.example.financeapp.domain.model

import java.time.LocalDate

data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    init {
        require(!endDate.isBefore(startDate)) {
            "End date must not be before start date"
        }
    }
}
