package com.example.financeapp.data.network.model.common

interface TransactionBaseResponseDto {

    val id: Long
    val amount: String
    val transactionDate: String
    val comment: String?
    val createdAt: String
    val updatedAt: String
}
