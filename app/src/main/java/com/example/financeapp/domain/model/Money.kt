package com.example.financeapp.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

class Money(
    amount: BigDecimal,
    val currency: Currency = Currency.RUB
) {
    val amount: BigDecimal = amount.stripTrailingZeros()

    constructor(
        amountInMinorUnits: Long,
        currency: Currency = Currency.RUB
    ) : this(
        amount = BigDecimal.valueOf(amountInMinorUnits, MINOR_UNITS_SCALE),
        currency = currency
    )

    val amountInMinorUnits: Long
        get() = amount
            .movePointRight(MINOR_UNITS_SCALE)
            .setScale(0, RoundingMode.UNNECESSARY)
            .longValueExact()

    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot add amounts in different currencies"
        }
        return Money(
            amount = amount.add(other.amount),
            currency = currency
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Money) return false

        return currency == other.currency && amount.compareTo(other.amount) == 0
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + currency.hashCode()
        return result
    }

    override fun toString(): String {
        return "Money(amount=$amount, currency=$currency)"
    }

    companion object {
        fun sum(
            amounts: Iterable<Money>,
            fallbackCurrency: Currency = Currency.RUB
        ): Money {
            val iterator = amounts.iterator()
            if (!iterator.hasNext()) {
                return Money(
                    amount = BigDecimal.ZERO,
                    currency = fallbackCurrency
                )
            }

            var total = iterator.next()
            while (iterator.hasNext()) {
                total += iterator.next()
            }
            return total
        }

        private const val MINOR_UNITS_SCALE = 2
    }
}
