package com.example.financeapp.presentation.bottomSheets.accountEditor

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.presentation.common.placeholders.ScreenError

@Immutable
data class AccountEditorHostState(
    val form: AccountEditorState? = null
)

@Immutable
data class AccountEditorState(
    val mode: AccountEditorMode,
    val name: String = "",
    val emoji: String = "",
    val balance: String = "",
    val selectedCurrency: Currency = Currency.RUB,
    val activeField: AccountEditorField? = null,
    @StringRes val formMessageResId: Int? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: ScreenError? = null
) {
    val canConfirm: Boolean
        get() = name.isNotBlank() &&
            isBalanceReady &&
            !isLoading &&
            !isSaving

    val isBalanceReady: Boolean
        get() = balance.isNotBlank() || mode is AccountEditorMode.Create
}

@Immutable
sealed interface AccountEditorMode {
    data object Create : AccountEditorMode
    data class Edit(val accountId: Long) : AccountEditorMode
}

@Immutable
enum class AccountEditorField {
    Name,
    Emoji,
    Currency
}

@Immutable
sealed interface AccountEditorIntent {
    data class Open(val mode: AccountEditorMode) : AccountEditorIntent
    data object NameClicked : AccountEditorIntent
    data class NameChanged(val name: String) : AccountEditorIntent
    data object EmojiClicked : AccountEditorIntent
    data class EmojiChanged(val emoji: String) : AccountEditorIntent
    data class BalanceChanged(val balance: String) : AccountEditorIntent
    data object CurrencyClicked : AccountEditorIntent
    data class CurrencySelected(val currency: Currency) : AccountEditorIntent
    data object FieldDismissed : AccountEditorIntent
    data object ConfirmClicked : AccountEditorIntent
    data object DismissRequested : AccountEditorIntent
    data object RetryClicked : AccountEditorIntent
}

@Immutable
sealed interface AccountEditorEffect {
    data class Saved(val accountId: Long) : AccountEditorEffect
    data object Close : AccountEditorEffect
}
