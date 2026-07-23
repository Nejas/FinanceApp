package com.example.financeapp.domain.model

import java.time.LocalDate

data class MainOverviewFilter(
    val currency: Currency,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
