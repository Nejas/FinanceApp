package com.example.financeapp.domain.model

import java.time.Instant

data class Transaction(
    val id: Long,
    val amount: Money,
    val categoryId: Long,
    val accountId: Long,
    val transactionDate: Instant,
    val comment: String? = null
)
