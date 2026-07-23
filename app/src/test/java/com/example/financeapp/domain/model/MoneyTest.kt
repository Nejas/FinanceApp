package com.example.financeapp.domain.model

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyTest {

    @Test
    fun plus_addsAmountsWithSameCurrency() {
        val first = Money(amountInMinorUnits = 12_345, currency = Currency.RUB)
        val second = Money(amountInMinorUnits = 655, currency = Currency.RUB)

        assertEquals(
            Money(amountInMinorUnits = 13_000, currency = Currency.RUB),
            first + second
        )
    }

    @Test
    fun plus_rejectsDifferentCurrencies() {
        val rubles = Money(amountInMinorUnits = 100, currency = Currency.RUB)
        val dollars = Money(amountInMinorUnits = 100, currency = Currency.USD)

        assertThrows(IllegalArgumentException::class.java) {
            rubles + dollars
        }
    }

    @Test
    fun currency_defaultsToRubles() {
        val money = Money(amountInMinorUnits = 0)

        assertEquals(Currency.RUB, money.currency)
    }

    @Test
    fun equals_ignoresBigDecimalScale() {
        assertEquals(
            Money(amount = BigDecimal("1.0"), currency = Currency.RUB),
            Money(amount = BigDecimal("1.00"), currency = Currency.RUB)
        )
    }

    @Test
    fun currencyFromCode_ignoresCase() {
        assertEquals(Currency.USD, Currency.fromCode("usd"))
    }

    @Test
    fun sum_addsAmountsWithSameCurrency() {
        val result = Money.sum(
            amounts = listOf(
                Money(amountInMinorUnits = 1_200L * 100),
                Money(amountInMinorUnits = 750L * 100),
                Money(amountInMinorUnits = 2_300L * 100)
            )
        )

        assertEquals(Money(amountInMinorUnits = 4_250L * 100), result)
    }

    @Test
    fun sum_returnsZeroInFallbackCurrencyForEmptyList() {
        val result = Money.sum(
            amounts = emptyList(),
            fallbackCurrency = Currency.EUR
        )

        assertEquals(
            Money(amountInMinorUnits = 0, currency = Currency.EUR),
            result
        )
    }

    @Test
    fun sum_rejectsAmountsWithDifferentCurrencies() {
        assertThrows(IllegalArgumentException::class.java) {
            Money.sum(
                amounts = listOf(
                    Money(amountInMinorUnits = 100, currency = Currency.RUB),
                    Money(amountInMinorUnits = 100, currency = Currency.USD)
                )
            )
        }
    }
}
