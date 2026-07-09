package com.example.financeapp.domain.model

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
    fun currencyFromCode_ignoresCase() {
        assertEquals(Currency.USD, Currency.fromCode("usd"))
    }
}
