package com.example.financeapp.presentation.common.model

import com.example.financeapp.domain.model.Money

data class RouteScreenItem(
    val id: String,
    val title: String,
    val leadingEmoji: String,
    val comment: String?,
    val money: Money
)
