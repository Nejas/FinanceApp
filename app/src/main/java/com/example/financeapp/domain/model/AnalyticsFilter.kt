package com.example.financeapp.domain.model

import java.time.LocalDate

data class AnalyticsFilter(
    val accountId: Long? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: TransactionType? = TransactionType.EXPENSE,
    val currency: Currency,
    val categoryIds: Set<Long> = emptySet()
)
