package com.example.financeapp.domain.model

data class FinancialAccount(
    val id: Long,
    val name: String,
    val balance: Money,
    val emoji: String
)
