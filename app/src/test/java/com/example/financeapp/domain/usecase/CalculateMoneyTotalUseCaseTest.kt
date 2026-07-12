package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculateMoneyTotalUseCaseTest {

    private val useCase = CalculateMoneyTotalUseCase()

    @Test
    fun invoke_sumsAmountsWithSameCurrency() {
        val result = useCase(
            amounts = listOf(
                Money(amountInMinorUnits = 1_200L * 100),
                Money(amountInMinorUnits = 750L * 100),
                Money(amountInMinorUnits = 2_300L * 100)
            )
        )

        assertEquals(Money(amountInMinorUnits = 4_250L * 100), result)
    }

    @Test
    fun invoke_returnsZeroInFallbackCurrencyForEmptyList() {
        val result = useCase(
            amounts = emptyList(),
            fallbackCurrency = Currency.EUR
        )

        assertEquals(
            Money(amountInMinorUnits = 0, currency = Currency.EUR),
            result
        )
    }

    @Test
    fun invoke_rejectsAmountsWithDifferentCurrencies() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase(
                amounts = listOf(
                    Money(amountInMinorUnits = 100, currency = Currency.RUB),
                    Money(amountInMinorUnits = 100, currency = Currency.USD)
                )
            )
        }
    }
}
