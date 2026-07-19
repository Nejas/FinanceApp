package com.example.financeapp.presentation.common.model

data class FinanceListItemUiModel(
    val id: String,
    val title: String,
    val leadingEmoji: String,
    val trailingText: String,
    val trailingDescription: String? = null
)
