package com.example.financeapp.presentation.common.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
interface FinanceFieldUi {
    @get:StringRes
    val titleResId: Int
    val icon: FinanceFieldIconType
}

@Immutable
sealed interface FinanceFieldIconType {
    data object Account : FinanceFieldIconType
    data object Article : FinanceFieldIconType
    data object Calendar : FinanceFieldIconType
    data object Clock : FinanceFieldIconType
    data object Currency : FinanceFieldIconType
    data object Emoji : FinanceFieldIconType
    data object ListType : FinanceFieldIconType
    data object SettingsBiometrics : FinanceFieldIconType
    data object SettingsLanguage : FinanceFieldIconType
    data object SettingsPinCode : FinanceFieldIconType
    data object SettingsTheme : FinanceFieldIconType
    data object Tag : FinanceFieldIconType
}
