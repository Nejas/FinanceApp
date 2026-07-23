package com.example.financeapp.domain.model

data class FinancialAccountPayload(
    val name: String,
    val emoji: String? = null,
    val balance: Money
)
