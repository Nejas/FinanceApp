package com.example.financeapp.data.network.model.request

import com.example.financeapp.data.network.model.common.AccountBaseRequestDto
import kotlinx.serialization.Serializable

@Serializable
data class AccountCreateRequestDto(
    override val name: String,
    override val emoji: String? = null,
    override val balance: String,
    override val currency: String
) : AccountBaseRequestDto

@Serializable
data class AccountUpdateRequestDto(
    override val name: String,
    override val emoji: String? = null,
    override val balance: String,
    override val currency: String
) : AccountBaseRequestDto
