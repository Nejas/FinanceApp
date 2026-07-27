package com.example.financeapp.domain.model

data class TransactionAnalysis(
    val total: Money,
    val categories: List<CategoryBreakdown>,
    val availableCategories: List<Category>,
    val transactions: List<AnalyzedTransaction>,
    val filter: TransactionAnalysisCriteria
)

data class CategoryBreakdown(
    val categoryId: Long,
    val category: Category?,
    val amount: Money,
    val sharePercent: Int
)

data class AnalyzedTransaction(
    val transaction: Transaction,
    val category: Category?,
    val account: Account?
)
