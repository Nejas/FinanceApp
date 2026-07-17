package com.example.financeapp.data.network.model.response

import com.example.financeapp.data.network.model.common.AccountBaseResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class AccountResponseDto(
    override val id: Long,
    val userId: Long,
    override val name: String,
    override val emoji: String,
    override val balance: String,
    override val currency: String,
    val createdAt: String,
    val updatedAt: String
) : AccountBaseResponseDto

@Serializable
data class AccountDetailsResponseDto(
    override val id: Long,
    override val name: String,
    override val emoji: String,
    override val balance: String,
    override val currency: String,
    val incomeStats: List<StatItemResponseDto>,
    val expenseStats: List<StatItemResponseDto>,
    val createdAt: String,
    val updatedAt: String
) : AccountBaseResponseDto

@Serializable
data class AccountBriefResponseDto(
    override val id: Long,
    override val name: String,
    override val emoji: String,
    override val balance: String,
    override val currency: String
) : AccountBaseResponseDto

@Serializable
data class AccountHistoryResponseDto(
    val accountId: Long,
    val accountName: String,
    val accountEmoji: String,
    val currency: String,
    val currentBalance: String,
    val history: List<AccountHistoryDto>
)

@Serializable
data class AccountHistoryDto(
    val id: Long,
    val accountId: Long,
    val changeType: String,
    val previousState: AccountStateResponseDto? = null,
    val newState: AccountStateResponseDto,
    val changeTimestamp: String,
    val createdAt: String
)

@Serializable
data class AccountStateResponseDto(
    override val id: Long,
    override val name: String,
    override val emoji: String,
    override val balance: String,
    override val currency: String
) : AccountBaseResponseDto

@Serializable
data class StatItemResponseDto(
    val categoryId: Long,
    val categoryName: String,
    val emoji: String,
    val amount: String
)
