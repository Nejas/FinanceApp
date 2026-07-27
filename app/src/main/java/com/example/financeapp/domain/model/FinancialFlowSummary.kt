package com.example.financeapp.domain.model

data class FinancialFlowSummary(
    val expenses: TransactionCategorySummary,
    val income: TransactionCategorySummary
)
