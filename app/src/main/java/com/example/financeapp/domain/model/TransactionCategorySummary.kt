package com.example.financeapp.domain.model

data class TransactionCategorySummary(
    val overview: TransactionSummary,
    val categories: List<Category>
)
