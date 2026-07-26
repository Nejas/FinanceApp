package com.example.financeapp.data.mapper

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import java.math.BigDecimal
import java.math.RoundingMode

fun String.toMoney(currencyCode: String): Money {
    val currency = Currency.fromCode(currencyCode)
        ?: throw DataMappingException("Unknown currency code: $currencyCode")
    return Money(
        amount = BigDecimal(this),
        currency = currency
    )
}

fun Money.toApiAmountString(): String {
    return amount
        .setScale(API_AMOUNT_SCALE, RoundingMode.UNNECESSARY)
        .toPlainString()
}

private const val API_AMOUNT_SCALE = 2
