package com.example.financeapp.presentation.common.utils

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Presentation-only formatter: domain keeps money as precise decimal amount, while UI hides
 * kopecks because current Figma mockups show rounded amounts.
 */
fun Money.formatWithoutMinorUnits(locale: Locale = Locale.getDefault()): String {
    val majorUnits = amount.abs().setScale(0, RoundingMode.DOWN)
    val formattedAmount = NumberFormat.getIntegerInstance(locale).format(majorUnits)
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
