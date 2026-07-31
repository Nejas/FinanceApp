package com.example.financeapp.domain.repository

import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setThemeMode(themeMode: AppThemeMode)

    suspend fun setCurrency(currency: Currency)

    suspend fun setBiometricLoginEnabled(isEnabled: Boolean)

    suspend fun setBiometricLoginOfferShown(isShown: Boolean)

    suspend fun setPinCodeSetupOnboardingShown(isShown: Boolean)
}
