package com.example.financeapp.domain.model

import com.example.financeapp.domain.model.common.TransactionInfo
import java.time.Instant

data class Transaction(
    override val id: Long,
    val title: String,
    override val amount: Money,
    override val categoryId: Long,
    override val accountId: Long,
    override val transactionDate: Instant,
    override val comment: String? = null
) : TransactionInfo
