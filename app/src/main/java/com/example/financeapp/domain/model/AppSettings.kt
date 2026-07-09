package com.example.financeapp.domain.model

data class AppSettings(
    val currency: Currency,
    val themeMode: ThemeMode,
    val language: AppLanguage,
    val isPinCodeEnabled: Boolean,
    val isBiometricEnabled: Boolean
)
