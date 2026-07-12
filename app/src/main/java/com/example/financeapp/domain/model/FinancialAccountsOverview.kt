package com.example.financeapp.domain.model

data class FinancialAccountsOverview(
    val accounts: List<FinancialAccount>,
    val totalBalance: Money
)
