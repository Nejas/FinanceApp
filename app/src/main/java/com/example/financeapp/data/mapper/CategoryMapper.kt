package com.example.financeapp.data.mapper

import com.example.financeapp.data.network.model.response.CategoryResponseDto
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.TransactionType

fun CategoryResponseDto.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        emoji = emoji,
        type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
    )
}
