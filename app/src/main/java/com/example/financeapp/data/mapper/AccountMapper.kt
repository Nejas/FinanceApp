package com.example.financeapp.data.mapper

import com.example.financeapp.data.network.model.request.AccountCreateRequestDto
import com.example.financeapp.data.network.model.request.AccountUpdateRequestDto
import com.example.financeapp.data.network.model.response.AccountDetailsResponseDto
import com.example.financeapp.data.network.model.response.AccountResponseDto
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.AccountDraft
import java.time.Instant

fun AccountResponseDto.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        balance = balance.toMoney(currency),
        emoji = emoji,
        createdAt = Instant.parse(createdAt)
    )
}

fun AccountDetailsResponseDto.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        balance = balance.toMoney(currency),
        emoji = emoji,
        createdAt = Instant.parse(createdAt)
    )
}

fun AccountDraft.toCreateRequestDto(): AccountCreateRequestDto {
    return AccountCreateRequestDto(
        name = name,
        emoji = emoji,
        balance = balance.amount.toPlainString(),
        currency = balance.currency.code
    )
}

fun AccountDraft.toUpdateRequestDto(): AccountUpdateRequestDto {
    return AccountUpdateRequestDto(
        name = name,
        emoji = emoji,
        balance = balance.amount.toPlainString(),
        currency = balance.currency.code
    )
}
