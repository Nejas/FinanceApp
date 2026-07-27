package com.example.financeapp.domain.model

data class TransactionSummary(
    val transactions: List<Transaction>,
    val accounts: List<Account>,
    val total: Money
)
