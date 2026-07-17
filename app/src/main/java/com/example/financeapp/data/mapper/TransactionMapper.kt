package com.example.financeapp.data.mapper

import com.example.financeapp.data.network.model.request.TransactionRequestDto
import com.example.financeapp.data.network.model.response.TransactionPlainResponseDto
import com.example.financeapp.data.network.model.response.TransactionResponseDto
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.common.TransactionPayload
import java.time.Instant

fun TransactionResponseDto.toDomain(): Transaction {
    return Transaction(
        id = id,
        title = category.name,
        amount = amount.toMoney(account.currency),
        categoryId = category.id,
        accountId = account.id,
        transactionDate = Instant.parse(transactionDate),
        comment = comment
    )
}

fun TransactionPlainResponseDto.toDomain(
    title: String = "",
    currencyCode: String
): Transaction {
    return Transaction(
        id = id,
        title = title,
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
