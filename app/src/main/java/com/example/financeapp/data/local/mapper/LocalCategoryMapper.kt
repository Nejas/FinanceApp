package com.example.financeapp.data.local.mapper

import com.example.financeapp.data.local.db.entity.CategoryEntity
import com.example.financeapp.data.network.model.response.CategoryResponseDto
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.TransactionType

fun CategoryResponseDto.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        emoji = emoji,
        isIncome = isIncome
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        emoji = emoji,
        isIncome = type == TransactionType.INCOME
    )
}

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        emoji = emoji,
        type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
    )
}
