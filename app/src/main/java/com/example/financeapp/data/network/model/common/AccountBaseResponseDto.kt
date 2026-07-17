package com.example.financeapp.data.network.model.common

interface AccountBaseResponseDto {

    val id: Long
    val name: String
    val emoji: String
    val balance: String
    val currency: String
}
