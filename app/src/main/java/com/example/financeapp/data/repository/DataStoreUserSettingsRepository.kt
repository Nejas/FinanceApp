package com.example.financeapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.UserSettings
import com.example.financeapp.domain.repository.UserSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings"
)

class DataStoreUserSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : UserSettingsRepository {

    override val settings: Flow<UserSettings> = context.userSettingsDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            UserSettings(
                themeMode = preferences[UserSettingsKeys.ThemeMode]
                    ?.let(AppThemeMode::fromName)
                    ?: AppThemeMode.SYSTEM,
                selectedCurrency = preferences[UserSettingsKeys.SelectedCurrency]
                    ?.let(Currency::fromCode)
                    ?: Currency.RUB,
                isBiometricLoginEnabled = preferences[UserSettingsKeys.IsBiometricLoginEnabled]
                    ?: false,
                isBiometricLoginOfferShown = preferences[UserSettingsKeys.IsBiometricLoginOfferShown]
                    ?: false,
                isPinCodeSetupOnboardingShown = preferences[UserSettingsKeys.IsPinCodeSetupOnboardingShown]
                    ?: false
            )
        }

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[UserSettingsKeys.ThemeMode] = themeMode.name
        }
    }

    override suspend fun setCurrency(currency: Currency) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[UserSettingsKeys.SelectedCurrency] = currency.code
        }
    }

    override suspend fun setBiometricLoginEnabled(isEnabled: Boolean) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[UserSettingsKeys.IsBiometricLoginEnabled] = isEnabled
        }
    }

    override suspend fun setBiometricLoginOfferShown(isShown: Boolean) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[UserSettingsKeys.IsBiometricLoginOfferShown] = isShown
        }
    }

    override suspend fun setPinCodeSetupOnboardingShown(isShown: Boolean) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[UserSettingsKeys.IsPinCodeSetupOnboardingShown] = isShown
        }
    }

    private object UserSettingsKeys {
        val ThemeMode = stringPreferencesKey("theme_mode")
        val SelectedCurrency = stringPreferencesKey("selected_currency")
        val IsBiometricLoginEnabled = booleanPreferencesKey("is_biometric_login_enabled")
        val IsBiometricLoginOfferShown = booleanPreferencesKey("is_biometric_login_offer_shown")
        val IsPinCodeSetupOnboardingShown = booleanPreferencesKey("is_pin_code_setup_onboarding_shown")
    }
}
