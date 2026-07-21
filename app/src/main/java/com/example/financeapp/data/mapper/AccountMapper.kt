package com.example.financeapp.data.mapper

import com.example.financeapp.data.network.model.request.AccountCreateRequestDto
import com.example.financeapp.data.network.model.request.AccountUpdateRequestDto
import com.example.financeapp.data.network.model.response.AccountDetailsResponseDto
import com.example.financeapp.data.network.model.response.AccountResponseDto
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.common.FinancialAccountPayload
import java.time.Instant

fun AccountResponseDto.toDomain(): FinancialAccount {
    return FinancialAccount(
        id = id,
        name = name,
        balance = balance.toMoney(currency),
        emoji = emoji,
        createdAt = Instant.parse(createdAt)
    )
}

fun AccountDetailsResponseDto.toDomain(): FinancialAccount {
    return FinancialAccount(
        id = id,
        name = name,
        balance = balance.toMoney(currency),
        emoji = emoji,
        createdAt = Instant.parse(createdAt)
    )
}

fun FinancialAccountPayload.toCreateRequestDto(): AccountCreateRequestDto {
    return AccountCreateRequestDto(
        name = name,
        emoji = emoji,
        balance = balance.amount.toPlainString(),
        currency = currency.code
    )
}

fun FinancialAccountPayload.toUpdateRequestDto(): AccountUpdateRequestDto {
    return AccountUpdateRequestDto(
        name = name,
        emoji = emoji,
        balance = balance.amount.toPlainString(),
        currency = currency.code
    )
}
