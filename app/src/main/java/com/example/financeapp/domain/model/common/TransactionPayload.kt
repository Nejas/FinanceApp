package com.example.financeapp.domain.model.common

import com.example.financeapp.domain.model.Money
import java.time.Instant

interface TransactionPayload {

    val accountId: Long
    val categoryId: Long
    val amount: Money
    val transactionDate: Instant
    val comment: String?
}
