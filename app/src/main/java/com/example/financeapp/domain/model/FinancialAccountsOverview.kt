package com.example.financeapp.domain.model

data class FinancialAccountsOverview(
    val accounts: List<Account>,
    val totalBalance: Money
)
