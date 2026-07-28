package com.example.financeapp.presentation.common.model

import androidx.compose.runtime.Immutable
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.SyncStatus

@Immutable
data class FinanceListItemUiModel(
    val id: String,
    val title: String,
    val leadingEmoji: String,
    val comment: String?,
    val money: Money,
    val isPendingSync: Boolean = false
)

fun Transaction.toFinanceListItemUiModel(categoriesById: Map<Long, Category>): FinanceListItemUiModel {
    val category = categoriesById[categoryId]
    return FinanceListItemUiModel(
        id = id.toString(),
        title = category?.name.orEmpty(),
        leadingEmoji = category?.emoji.orEmpty(),
        comment = comment,
        money = amount,
        isPendingSync = syncStatus == SyncStatus.PENDING
    )
}
