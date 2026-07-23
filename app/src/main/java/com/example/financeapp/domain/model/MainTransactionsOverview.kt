package com.example.financeapp.domain.model

data class MainTransactionsOverview(
    val expenses: CategorizedTransactionsOverview,
    val income: CategorizedTransactionsOverview
)
