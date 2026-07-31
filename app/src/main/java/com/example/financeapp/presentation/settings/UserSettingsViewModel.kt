package com.example.financeapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.domain.model.AuthProtectionState
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.UserSettings
import com.example.financeapp.domain.usecase.SecurityUseCases
import com.example.financeapp.domain.usecase.UserSettingsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val userSettingsUseCases: UserSettingsUseCases,
    private val securityUseCases: SecurityUseCases
) : ViewModel() {

    val settings: StateFlow<UserSettings> = userSettingsUseCases.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
        initialValue = UserSettings()
    )

    val hasPinCode: StateFlow<Boolean?> = securityUseCases.hasPinCode
        .map<Boolean, Boolean?> { hasPinCode -> hasPinCode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
            initialValue = null
        )

    val isPinCodeSetupOnboardingShown: StateFlow<Boolean?> = userSettingsUseCases.settings
        .map { settings -> settings.isPinCodeSetupOnboardingShown }
        .map<Boolean, Boolean?> { isShown -> isShown }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
            initialValue = null
        )

    val authProtectionState: StateFlow<AuthProtectionState> =
        securityUseCases.authProtectionState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
            initialValue = AuthProtectionState()
        )

    fun setThemeMode(themeMode: AppThemeMode) {
        viewModelScope.launch {
            userSettingsUseCases.setThemeMode(themeMode)
        }
    }

    fun setCurrency(currency: Currency) {
        viewModelScope.launch {
            userSettingsUseCases.setCurrency(currency)
        }
    }

    fun setBiometricLoginEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            userSettingsUseCases.setBiometricLoginEnabled(isEnabled)
        }
    }

    suspend fun setBiometricLoginEnabledSuspend(isEnabled: Boolean) {
        userSettingsUseCases.setBiometricLoginEnabled(isEnabled)
    }

    suspend fun setBiometricLoginOfferShown(isShown: Boolean) {
        userSettingsUseCases.setBiometricLoginOfferShown(isShown)
    }

    suspend fun setPinCodeSetupOnboardingShown(isShown: Boolean) {
        userSettingsUseCases.setPinCodeSetupOnboardingShown(isShown)
    }

    suspend fun verifyPinCode(pinCode: String): Boolean {
        return securityUseCases.verifyPinCode(pinCode)
    }

    suspend fun setPinCode(pinCode: String) {
        securityUseCases.setPinCode(pinCode)
        userSettingsUseCases.setPinCodeSetupOnboardingShown(true)
    }

    suspend fun clearPinProtection() {
        securityUseCases.clearPinCode()
        userSettingsUseCases.setBiometricLoginEnabled(false)
    }

    suspend fun canAttemptPin(): Boolean {
        return securityUseCases.canAttemptPin()
    }

    suspend fun registerPinFailure(): AuthProtectionState {
        return securityUseCases.registerPinFailure()
    }

    suspend fun registerBiometricFailure(): AuthProtectionState {
        return securityUseCases.registerBiometricFailure()
    }

    suspend fun registerAuthSuccess() {
        securityUseCases.registerAuthSuccess()
    }

    private companion object {
        const val StopTimeoutMillis = 5_000L
    }
}
