package com.example.financeapp.presentation.common.utils

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatterTest {

    @Test
    fun `formatWithoutMinorUnits uses spaces for all locales`() {
        val originalLocale = Locale.getDefault()

        try {
            listOf(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH).forEach { locale ->
                Locale.setDefault(locale)

                assertEquals(
                    "123 322 ₽",
                    Money(amountInMinorUnits = 123_322L * 100, currency = Currency.RUB)
                        .formatWithoutMinorUnits()
                )
            }
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun `formatWithoutMinorUnits keeps minus sign before grouped amount`() {
        assertEquals(
            "-9 500 $",
            Money(amountInMinorUnits = -9_500L * 100, currency = Currency.USD)
                .formatWithoutMinorUnits()
        )
    }
}
