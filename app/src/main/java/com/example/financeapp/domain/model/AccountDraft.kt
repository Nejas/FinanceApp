package com.example.financeapp.domain.model

data class AccountDraft(
    val name: String,
    val emoji: String? = null,
    val balance: Money
)
