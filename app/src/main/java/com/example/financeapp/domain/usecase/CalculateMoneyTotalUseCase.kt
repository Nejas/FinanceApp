package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import java.math.BigDecimal
import javax.inject.Inject

class CalculateMoneyTotalUseCase @Inject constructor() {

    operator fun invoke(
        amounts: List<Money>,
        fallbackCurrency: Currency = Currency.RUB
    ): Money {
        if (amounts.isEmpty()) {
            return Money(
                amount = BigDecimal.ZERO,
                currency = fallbackCurrency
            )
        }

        return amounts.drop(1).fold(amounts.first()) { total, amount ->
            total + amount
        }
    }
}
