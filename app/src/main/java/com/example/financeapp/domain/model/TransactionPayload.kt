package com.example.financeapp.domain.model

import java.time.Instant

data class TransactionPayload(
    val accountId: Long,
    val categoryId: Long,
    val amount: Money,
    val transactionDate: Instant,
    val comment: String? = null
)
