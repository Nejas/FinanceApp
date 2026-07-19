package com.example.financeapp.domain.model

enum class Currency(val code: String) {
    RUB("RUB"),
    USD("USD"),
    EUR("EUR"),
    GBP("GBP"),
    CNY("CNY");

    companion object {
        fun fromCode(code: String): Currency? {
            return entries.firstOrNull { currency ->
                currency.code.equals(code, ignoreCase = true)
            }
        }
    }
}
