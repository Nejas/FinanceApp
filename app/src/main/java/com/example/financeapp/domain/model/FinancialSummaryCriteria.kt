package com.example.financeapp.domain.model

import java.time.LocalDate

data class FinancialSummaryCriteria(
    val currency: Currency,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
