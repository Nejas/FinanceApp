package com.example.financeapp.data.network.model.request

import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequestDto(
    val accountId: Long,
    val categoryId: Long,
    val amount: String,
    val transactionDate: String,
    val comment: String? = null
)
