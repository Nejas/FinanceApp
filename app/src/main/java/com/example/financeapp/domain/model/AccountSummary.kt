package com.example.financeapp.domain.model

data class AccountSummary(
    val accounts: List<Account>,
    val totalBalance: Money
)
