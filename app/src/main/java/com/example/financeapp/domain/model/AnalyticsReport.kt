package com.example.financeapp.domain.model

data class AnalyticsReport(
    val total: Money,
    val categories: List<CategorySummary>,
    val transactions: List<Transaction>
)
