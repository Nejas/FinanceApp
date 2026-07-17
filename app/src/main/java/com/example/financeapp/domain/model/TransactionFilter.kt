package com.example.financeapp.domain.model

import java.time.LocalDate

data class TransactionFilter(
    val accountId: Long,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val type: TransactionType? = null
)
