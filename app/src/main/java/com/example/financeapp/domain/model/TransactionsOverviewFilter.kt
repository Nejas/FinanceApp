package com.example.financeapp.domain.model

import java.time.LocalDate

data class TransactionsOverviewFilter(
    val accountId: Long? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate,
    val type: TransactionType? = null,
    val currency: Currency,
    val categoryIds: Set<Long> = emptySet()
)
