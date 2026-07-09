package com.example.financeapp.domain.model

data class AnalyticsFilter(
    val transactionType: TransactionType,
    val period: DateRange,
    val categoryIds: Set<Long> = emptySet(),
    val accountIds: Set<Long> = emptySet()
)
