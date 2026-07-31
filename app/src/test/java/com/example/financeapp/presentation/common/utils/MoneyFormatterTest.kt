package com.example.financeapp.presentation.common.utils

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatterTest {

    @Test
    fun `formatWithMinorUnits uses spaces for all locales`() {
        val originalLocale = Locale.getDefault()

        try {
            listOf(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH).forEach { locale ->
                Locale.setDefault(locale)

                assertEquals(
                    "123 322.45 ₽",
                    Money(amountInMinorUnits = 12_332_245L, currency = Currency.RUB)
                        .formatWithMinorUnits()
                )
            }
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun `formatWithMinorUnits keeps minus sign before grouped amount`() {
        assertEquals(
            "-9 500.75 $",
            Money(amountInMinorUnits = -950_075L, currency = Currency.USD)
                .formatWithMinorUnits()
        )
    }
}
