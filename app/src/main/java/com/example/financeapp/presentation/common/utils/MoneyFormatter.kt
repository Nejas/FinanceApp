package com.example.financeapp.presentation.common.utils

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import java.math.RoundingMode

/**
 * Presentation-only formatter: domain keeps money as precise decimal amount, while UI hides
 * kopecks because current Figma mockups show rounded amounts.
 */
fun Money.formatWithoutMinorUnits(): String {
    val majorUnits = amount.abs().setScale(0, RoundingMode.DOWN)
    val formattedAmount = majorUnits.toPlainString().groupThousands()
    val sign = if (amount.signum() < 0) "-" else ""
    return "$sign$formattedAmount ${currency.symbol}"
}

val Currency.symbol: String
    get() = when (this) {
        Currency.RUB -> "₽"
        Currency.USD -> "$"
        Currency.EUR -> "€"
        Currency.GBP -> "£"
        Currency.CNY -> "¥"
    }

private fun String.groupThousands(): String {
    return reversed()
        .chunked(ThousandsGroupSize)
        .joinToString(separator = AmountGroupSeparator)
        .reversed()
}

private const val ThousandsGroupSize = 3
private const val AmountGroupSeparator = " "
