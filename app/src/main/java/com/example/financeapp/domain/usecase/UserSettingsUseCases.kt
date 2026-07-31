package com.example.financeapp.domain.usecase

import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.repository.UserSettingsRepository
import javax.inject.Inject

class UserSettingsUseCases @Inject constructor(
    private val repository: UserSettingsRepository
) {
    val settings = repository.settings

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        repository.setThemeMode(themeMode)
    }

    suspend fun setCurrency(currency: Currency) {
        repository.setCurrency(currency)
    }

    suspend fun setBiometricLoginEnabled(isEnabled: Boolean) {
        repository.setBiometricLoginEnabled(isEnabled)
    }

    suspend fun setBiometricLoginOfferShown(isShown: Boolean) {
        repository.setBiometricLoginOfferShown(isShown)
    }

    suspend fun setPinCodeSetupOnboardingShown(isShown: Boolean) {
        repository.setPinCodeSetupOnboardingShown(isShown)
    }
}
