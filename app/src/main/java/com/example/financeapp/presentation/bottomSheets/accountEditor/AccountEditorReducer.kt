package com.example.financeapp.presentation.bottomSheets.accountEditor

import androidx.annotation.StringRes
import com.example.financeapp.R
import javax.inject.Inject

class AccountEditorReducer @Inject constructor() {

    fun reduce(
        state: AccountEditorState,
        intent: AccountEditorIntent
    ): AccountEditorState {
        return when (intent) {
            AccountEditorIntent.NameClicked -> state.copy(
                activeField = AccountEditorField.Name,
                formMessageResId = null
            )
            is AccountEditorIntent.NameChanged -> state.copy(
                name = intent.name,
                formMessageResId = null
            )
            AccountEditorIntent.EmojiClicked -> state.copy(
                activeField = AccountEditorField.Emoji,
                formMessageResId = null
            )
            is AccountEditorIntent.EmojiChanged -> state.copy(
                emoji = intent.emoji.normalizedSingleSymbol(),
                formMessageResId = null
            )
            is AccountEditorIntent.BalanceChanged -> state.copy(
                balance = intent.balance.normalizedIntegerAmount(),
                formMessageResId = null
            )
            AccountEditorIntent.CurrencyClicked -> state.copy(
                activeField = AccountEditorField.Currency,
                formMessageResId = null
            )
            is AccountEditorIntent.CurrencySelected -> state.copy(
                selectedCurrency = intent.currency,
                activeField = null,
                formMessageResId = null
            )
            AccountEditorIntent.FieldDismissed -> state.copy(activeField = null)
            is AccountEditorIntent.Open,
            AccountEditorIntent.ConfirmClicked,
            AccountEditorIntent.DismissRequested,
            AccountEditorIntent.RetryClicked -> state
        }
    }

    @StringRes
    fun validationMessage(state: AccountEditorState): Int? {
        return when {
            state.name.isBlank() -> R.string.account_name_required
            state.balance.isBlank() && state.mode is AccountEditorMode.Create -> null
            state.balance.isBlank() && state.mode is AccountEditorMode.Edit -> {
                R.string.account_balance_required
            }
            state.balance.toBigDecimalOrNull() == null -> R.string.account_balance_invalid
            state.balance.toBigDecimal().signum() < 0 -> R.string.account_balance_invalid
            else -> null
        }
    }
}

private fun String.normalizedIntegerAmount(): String {
    val digits = filter(Char::isDigit)
    val normalizedDigits = digits.trimStart(LEADING_ZERO)

    return normalizedDigits.ifEmpty {
        if (digits.isEmpty()) "" else ZERO_AMOUNT_VALUE
    }
}

private const val LEADING_ZERO = '0'
private const val ZERO_AMOUNT_VALUE = "0"

private fun String.normalizedSingleSymbol(): String {
    val value = trim()
    if (value.isEmpty()) return ""

    val codePoints = value.codePoints().toArray()
    var endIndex = 1

    if (codePoints.first().isRegionalIndicator()) {
        if (codePoints.getOrNull(endIndex)?.isRegionalIndicator() == true) {
            endIndex++
        }
        return codePoints.take(endIndex).toCodePointString()
    }

    endIndex = includeSymbolMarks(codePoints, endIndex)
    while (codePoints.getOrNull(endIndex) == ZeroWidthJoiner && endIndex + 1 < codePoints.size) {
        endIndex += 2
        endIndex = includeSymbolMarks(codePoints, endIndex)
    }

    return codePoints.take(endIndex).toCodePointString()
}

private fun includeSymbolMarks(
    codePoints: IntArray,
    startIndex: Int
): Int {
    var index = startIndex
    while (
        codePoints.getOrNull(index)?.isVariationSelector() == true ||
        codePoints.getOrNull(index)?.isEmojiModifier() == true ||
        codePoints.getOrNull(index)?.isCombiningMark() == true
    ) {
        index++
    }
    return index
}

private fun List<Int>.toCodePointString(): String {
    val builder = StringBuilder()
    forEach { codePoint -> builder.appendCodePoint(codePoint) }
    return builder.toString()
}

private fun Int.isRegionalIndicator(): Boolean {
    return this in RegionalIndicatorStart..RegionalIndicatorEnd
}

private fun Int.isVariationSelector(): Boolean {
    return this in VariationSelectorStart..VariationSelectorEnd ||
        this in VariationSelectorSupplementStart..VariationSelectorSupplementEnd
}

private fun Int.isEmojiModifier(): Boolean {
    return this in EmojiModifierStart..EmojiModifierEnd
}

private fun Int.isCombiningMark(): Boolean {
    return Character.getType(this) in CombiningMarkTypes
}

private const val ZeroWidthJoiner = 0x200D
private const val RegionalIndicatorStart = 0x1F1E6
private const val RegionalIndicatorEnd = 0x1F1FF
private const val VariationSelectorStart = 0xFE00
private const val VariationSelectorEnd = 0xFE0F
private const val VariationSelectorSupplementStart = 0xE0100
private const val VariationSelectorSupplementEnd = 0xE01EF
private const val EmojiModifierStart = 0x1F3FB
private const val EmojiModifierEnd = 0x1F3FF
private val CombiningMarkTypes = setOf(
    Character.NON_SPACING_MARK.toInt(),
    Character.COMBINING_SPACING_MARK.toInt(),
    Character.ENCLOSING_MARK.toInt()
)
