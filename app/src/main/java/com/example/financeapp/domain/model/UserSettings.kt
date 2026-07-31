package com.example.financeapp.domain.model

import com.example.financeapp.core.theme.AppThemeMode

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val selectedCurrency: Currency = Currency.RUB,
    val isBiometricLoginEnabled: Boolean = false,
    val isBiometricLoginOfferShown: Boolean = false,
    val isPinCodeSetupOnboardingShown: Boolean = false
)
