package com.example.financeapp.data.network.model.common

interface AccountBaseRequestDto {

    val name: String
    val emoji: String?
    val balance: String
    val currency: String
}
