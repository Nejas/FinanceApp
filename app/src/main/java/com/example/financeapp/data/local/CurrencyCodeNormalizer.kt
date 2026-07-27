package com.example.financeapp.data.local

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.data.mapper.DataMappingException

fun String.normalizeCurrencyCode(): String {
    val normalizedValue = trim()
    val code = Currency.fromCode(normalizedValue)?.code ?: when (normalizedValue) {
        "₽" -> Currency.RUB.code
        "$" -> Currency.USD.code
        "€" -> Currency.EUR.code
        "£" -> Currency.GBP.code
        "¥", "￥" -> Currency.CNY.code
        else -> null
    }
    return code ?: throw DataMappingException("Unknown currency value: $normalizedValue")
}
