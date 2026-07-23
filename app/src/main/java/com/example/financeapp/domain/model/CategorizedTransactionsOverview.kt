package com.example.financeapp.domain.model

data class CategorizedTransactionsOverview(
    val overview: TransactionsOverview,
    val categories: List<Category>
)
