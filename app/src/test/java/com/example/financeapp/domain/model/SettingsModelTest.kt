package com.example.financeapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsModelTest {

    @Test
    fun languageFromTag_ignoresCase() {
        assertEquals(AppLanguage.GERMAN, AppLanguage.fromLanguageTag("DE"))
    }

    @Test
    fun settings_keepSecurityFlagsWithoutContainingCredentials() {
        val settings = AppSettings(
            currency = Currency.RUB,
            themeMode = ThemeMode.SYSTEM,
            language = AppLanguage.RUSSIAN,
            isPinCodeEnabled = true,
            isBiometricEnabled = true
        )

        assertEquals(true, settings.isPinCodeEnabled)
        assertEquals(true, settings.isBiometricEnabled)
    }
}
