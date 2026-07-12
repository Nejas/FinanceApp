package com.example.financeapp.domain.model

data class TransactionsOverview(
    val transactions: List<Transaction>,
    val total: Money
)
