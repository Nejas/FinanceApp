package com.example.financeapp.domain.model

data class TransactionsOverview(
    val transactions: List<Transaction>,
    val accounts: List<FinancialAccount>,
    val total: Money
)
