package com.example.financeapp.presentation.common.model

import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction

data class FinanceListItemUiModel(
    val id: String,
    val title: String,
    val leadingEmoji: String,
    val comment: String?,
    val money: Money
)

fun Transaction.toFinanceListItemUiModel(categoriesById: Map<Long, Category>): FinanceListItemUiModel {
    val category = categoriesById[categoryId]
    return FinanceListItemUiModel(
        id = id.toString(),
        title = category?.name.orEmpty(),
        leadingEmoji = category?.emoji.orEmpty(),
        comment = comment,
        money = amount
    )
}
