package com.example.financeapp.presentation.common.testing

import com.example.financeapp.core.theme.AppThemeMode

/** Stable semantics tags used by Compose UI tests. */
object FinanceTestTags {
    const val PinCodeInput = "pin_code_input"
    const val PinCodeChangeAction = "pin_code_change_action"
    const val PinCodeResetAction = "pin_code_reset_action"

    fun themeOption(themeMode: AppThemeMode): String {
        return "theme_option_${themeMode.name.lowercase()}"
    }
}
