package com.example.financeapp.data.network.model.response

import com.example.financeapp.data.network.model.common.TransactionBaseResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponseDto(
    override val id: Long,
    val account: AccountBriefResponseDto,
    val category: CategoryResponseDto,
    override val amount: String,
    override val transactionDate: String,
    override val comment: String? = null,
    override val createdAt: String,
    override val updatedAt: String
) : TransactionBaseResponseDto

@Serializable
data class TransactionPlainResponseDto(
    override val id: Long,
    val accountId: Long,
    val categoryId: Long,
    override val amount: String,
    override val transactionDate: String,
    override val comment: String? = null,
    override val createdAt: String,
    override val updatedAt: String
) : TransactionBaseResponseDto
