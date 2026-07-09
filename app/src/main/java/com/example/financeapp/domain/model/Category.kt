package com.example.financeapp.domain.model

data class Category(
    val id: Long,
    val name: String,
    val emoji: String,
    val type: TransactionType
)
