package com.example.financeapp.presentation.common.utils

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import java.math.BigDecimal
import java.math.RoundingMode

fun Money.formatWithMinorUnits(): String {
    val displayAmount = amount.abs().setScale(MINOR_UNITS_SCALE, RoundingMode.HALF_UP)
    val formattedAmount = displayAmount.toPlainString().groupThousands()
    val sign = if (amount.signum() < 0) "-" else ""
    return "$sign$formattedAmount ${currency.symbol}"
}

fun BigDecimal.toEditableAmount(): String {
    return setScale(MINOR_UNITS_SCALE, RoundingMode.HALF_UP).toPlainString()
}

fun String.normalizedDecimalAmountInput(): String {
    val integerPart = StringBuilder()
    val fractionPart = StringBuilder()
    var hasSeparator = false

    forEach { char ->
        when {
            char.isDigit() && hasSeparator && fractionPart.length < MINOR_UNITS_SCALE -> {
                fractionPart.append(char)
            }
            char.isDigit() && !hasSeparator -> integerPart.append(char)
            char.isDecimalSeparator() && !hasSeparator -> hasSeparator = true
        }
    }

    if (integerPart.isEmpty() && !hasSeparator) return ""

    val normalizedInteger = integerPart
        .toString()
        .trimStart(LEADING_ZERO)
        .ifEmpty { ZERO_AMOUNT_VALUE }

    return if (hasSeparator) {
        "$normalizedInteger.$fractionPart"
    } else {
        normalizedInteger
    }
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
    val parts = split(DECIMAL_SEPARATOR, limit = 2)
    val groupedInteger = parts.first()
        .reversed()
        .chunked(ThousandsGroupSize)
        .joinToString(separator = AmountGroupSeparator)
        .reversed()
    val fraction = parts.getOrNull(1)

    return if (fraction == null) {
        groupedInteger
    } else {
        "$groupedInteger$DECIMAL_SEPARATOR$fraction"
    }
}

private fun Char.isDecimalSeparator(): Boolean {
    return this == DECIMAL_SEPARATOR || this == ALTERNATIVE_DECIMAL_SEPARATOR
}

private const val MINOR_UNITS_SCALE = 2
private const val LEADING_ZERO = '0'
private const val ZERO_AMOUNT_VALUE = "0"
private const val DECIMAL_SEPARATOR = '.'
private const val ALTERNATIVE_DECIMAL_SEPARATOR = ','
private const val ThousandsGroupSize = 3
private const val AmountGroupSeparator = " "
