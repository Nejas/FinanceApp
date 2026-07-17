package com.example.financeapp.domain.model.common

import com.example.financeapp.domain.model.Money
import java.time.Instant

interface TransactionInfo : Identifiable {

    val amount: Money
    val categoryId: Long
    val accountId: Long
    val transactionDate: Instant
    val comment: String?
}
