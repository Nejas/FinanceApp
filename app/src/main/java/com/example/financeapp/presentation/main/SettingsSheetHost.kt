package com.example.financeapp.presentation.main

import androidx.compose.runtime.Composable
import com.example.financeapp.core.localization.AppLanguage
import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.domain.model.AuthProtectionState
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.UserSettings
import com.example.financeapp.presentation.bottomSheets.components.categories.FinanceCategorySelectionSheetContent
import com.example.financeapp.presentation.bottomSheets.components.currency.FinanceCurrencySelectionSheetContent
import com.example.financeapp.presentation.bottomSheets.settings.BiometricSettingsSheet
import com.example.financeapp.presentation.bottomSheets.settings.LanguageSettingsSheet
import com.example.financeapp.presentation.bottomSheets.settings.PinCodeSettingsSheet
import com.example.financeapp.presentation.bottomSheets.settings.SettingsBottomSheet
import com.example.financeapp.presentation.bottomSheets.settings.SettingsListItem
import com.example.financeapp.presentation.bottomSheets.settings.ThemeSettingsSheet
import com.example.financeapp.presentation.common.components.base.FinanceModalBottomSheet
import com.example.financeapp.presentation.common.components.base.FinanceSelectionIndicatorType

@Composable
internal fun SettingsSheetHost(
    isVisible: Boolean,
    activeItem: SettingsListItem?,
    mainState: MainState,
    userSettings: UserSettings,
    hasPinCode: Boolean,
    authProtectionState: AuthProtectionState,
    isBiometricAvailable: Boolean,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
    onBiometricLoginEnabledChange: (Boolean) -> Unit,
    onBiometricAuthenticationRequest: (onAuthenticated: () -> Unit, onFailure: (Boolean) -> Unit) -> Unit,
    onVerifyPinCode: suspend (String) -> Boolean,
    onSetPinCode: suspend (String) -> Unit,
    onClearPinCode: suspend () -> Unit,
    onCanAttemptPin: suspend () -> Boolean,
    onPinFailure: suspend () -> AuthProtectionState,
    onAuthSuccess: suspend () -> Unit,
    onDismissSettings: () -> Unit,
    onDismissActiveItem: () -> Unit,
    onItemSelected: (SettingsListItem) -> Unit,
    onBiometricFailure: () -> Unit
) {
    if (!isVisible) return
    when (activeItem) {
        SettingsListItem.Articles -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            FinanceCategorySelectionSheetContent(
                categories = mainState.settingsCategories(),
                selectedCategoryIds = emptySet(),
                indicatorType = FinanceSelectionIndicatorType.CheckMark,
                onCategoryClick = null
            )
        }
        SettingsListItem.Language -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            LanguageSettingsSheet(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { language ->
                    onLanguageSelected(language)
                    onDismissActiveItem()
                }
            )
        }
        SettingsListItem.Theme -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            ThemeSettingsSheet(
                selectedThemeMode = userSettings.themeMode,
                onThemeModeSelected = onThemeModeSelected
            )
        }
        SettingsListItem.PinCode -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            PinCodeSettingsSheet(
                hasPinCode = hasPinCode,
                authProtectionState = authProtectionState,
                onVerifyPinCode = onVerifyPinCode,
                onSetPinCode = onSetPinCode,
                onClearPinCode = onClearPinCode,
                onCanAttemptPin = onCanAttemptPin,
                onPinFailure = onPinFailure,
                onAuthSuccess = onAuthSuccess,
                onPinCodeChanged = onDismissActiveItem
            )
        }
        SettingsListItem.Biometrics -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            BiometricSettingsSheet(
                isEnabled = userSettings.isBiometricLoginEnabled,
                onEnabledChange = { isEnabled ->
                    if (isEnabled) {
                        onBiometricAuthenticationRequest(
                            { onBiometricLoginEnabledChange(true) },
                            { isFailedAttempt -> if (isFailedAttempt) onBiometricFailure() }
                        )
                    } else {
                        onBiometricLoginEnabledChange(false)
                    }
                }
            )
        }
        SettingsListItem.Currency -> FinanceModalBottomSheet(onDismissRequest = onDismissActiveItem) {
            FinanceCurrencySelectionSheetContent(
                selectedCurrency = userSettings.selectedCurrency,
                onCurrencyClick = { currency ->
                    onCurrencySelected(currency)
                    onDismissActiveItem()
                }
            )
        }
        null -> SettingsBottomSheet(
            onDismissRequest = onDismissSettings,
            isBiometricAvailable = isBiometricAvailable,
            onItemClick = onItemSelected
        )
    }
}

private fun MainState.settingsCategories(): List<Category> =
    (expensesState.categoriesById.values + incomeState.categoriesById.values)
        .distinctBy(Category::id)
        .sortedWith(compareBy<Category> { it.type }.thenBy { it.name })
