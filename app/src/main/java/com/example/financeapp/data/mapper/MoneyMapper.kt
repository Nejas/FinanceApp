package com.example.financeapp.data.mapper

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import java.math.BigDecimal

fun String.toMoney(currencyCode: String): Money {
    val currency = Currency.fromCode(currencyCode)
        ?: throw DataMappingException("Unknown currency code: $currencyCode")
    return Money(
        amount = BigDecimal(this),
        currency = currency
    )
}
