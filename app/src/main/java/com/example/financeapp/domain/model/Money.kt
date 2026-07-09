package com.example.financeapp.domain.model

data class Money(
    val amountInMinorUnits: Long,
    val currency: Currency = Currency.RUB
) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot add amounts in different currencies"
        }
        return copy(amountInMinorUnits = Math.addExact(amountInMinorUnits, other.amountInMinorUnits))
    }
}
