package com.example.financeapp.domain.model

import java.time.LocalDate

data class TransactionsQuery(
    val accountIds: Set<Long>,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val type: TransactionType? = null
)
