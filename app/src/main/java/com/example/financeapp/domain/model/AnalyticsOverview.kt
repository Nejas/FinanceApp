package com.example.financeapp.domain.model

data class AnalyticsOverview(
    val total: Money,
    val categories: List<AnalyticsCategoryBreakdown>,
    val availableCategories: List<Category>,
    val transactions: List<AnalyticsTransactionEntry>,
    val filter: AnalyticsFilter
)

data class AnalyticsCategoryBreakdown(
    val categoryId: Long,
    val category: Category?,
    val amount: Money,
    val sharePercent: Int
)

data class AnalyticsTransactionEntry(
    val transaction: Transaction,
    val category: Category?,
    val account: FinancialAccount?
)
