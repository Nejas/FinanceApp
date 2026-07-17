package com.example.financeapp.domain.model

import com.example.financeapp.domain.model.common.TransactionPayload
import java.time.Instant

data class TransactionPayloadData(
    override val accountId: Long,
    override val categoryId: Long,
    override val amount: Money,
    override val transactionDate: Instant,
    override val comment: String? = null
) : TransactionPayload
