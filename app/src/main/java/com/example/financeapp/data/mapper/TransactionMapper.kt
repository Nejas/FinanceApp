package com.example.financeapp.data.mapper

import com.example.financeapp.data.network.model.request.TransactionRequestDto
import com.example.financeapp.data.network.model.response.TransactionPlainResponseDto
import com.example.financeapp.data.network.model.response.TransactionResponseDto
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionPayload
import java.time.Instant

fun TransactionResponseDto.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount.toMoney(account.currency),
        categoryId = category.id,
        accountId = account.id,
        transactionDate = Instant.parse(transactionDate),
        comment = comment
    )
}

fun TransactionPlainResponseDto.toDomain(
    currencyCode: String
): Transaction {
    return Transaction(
        id = id,
        amount = amount.toMoney(currencyCode),
        categoryId = categoryId,
        accountId = accountId,
        transactionDate = Instant.parse(transactionDate),
        comment = comment
    )
}

fun TransactionPayload.toRequestDto(): TransactionRequestDto {
    return TransactionRequestDto(
        accountId = accountId,
        categoryId = categoryId,
        amount = amount.amount.toPlainString(),
        transactionDate = transactionDate.toString(),
        comment = comment
    )
}
