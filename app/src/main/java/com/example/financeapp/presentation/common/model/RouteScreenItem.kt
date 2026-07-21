package com.example.financeapp.presentation.common.model

import com.example.financeapp.domain.model.Money

data class RouteScreenItem(
    override val id: String,
    override val title: String,
    override val leadingEmoji: String,
    override val comment: String?,
    val money: Money
): FinanceListItem
