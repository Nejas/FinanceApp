package com.example.financeapp.domain.model

data class AnalyticsOverview(
    val total: Money,
    val categories: List<AnalyticsCategorySummary>,
    val availableCategories: List<Category>,
    val transactions: List<AnalyticsTransactionSummary>,
    val filter: AnalyticsFilter
)

data class AnalyticsCategorySummary(
    val categoryId: Long,
    val title: String,
    val emoji: String,
    val amount: Money,
    val percent: Int
)

data class AnalyticsTransactionSummary(
    val id: Long,
    val title: String,
    val leadingEmoji: String,
    val accountName: String,
    val amount: Money
)
