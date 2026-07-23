package com.example.financeapp.presentation.common.model

import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Transaction

fun Transaction.toRouteScreenItem(categoriesById: Map<Long, Category>): RouteScreenItem {
    val category = categoriesById[categoryId]
    return RouteScreenItem(
        id = id.toString(),
        title = category?.name.orEmpty(),
        leadingEmoji = category?.emoji.orEmpty(),
        comment = comment,
        money = amount
    )
}
