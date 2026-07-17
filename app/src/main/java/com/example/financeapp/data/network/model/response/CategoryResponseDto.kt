package com.example.financeapp.data.network.model.response

import com.example.financeapp.data.network.model.common.CategoryBaseResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponseDto(
    override val id: Long,
    override val name: String,
    override val emoji: String,
    val isIncome: Boolean
) : CategoryBaseResponseDto
