package com.example.financeapp.presentation.bottomSheets.settings

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.financeapp.R
import com.example.financeapp.presentation.common.model.FinanceFieldIconType
import com.example.financeapp.presentation.common.model.FinanceFieldUi

@Immutable
sealed interface SettingsListItem : FinanceFieldUi {
    val saveableKey: String

    data object Currency : SettingsListItem {
        override val saveableKey: String = "currency"
        override val titleResId: Int = R.string.settings_currency
        override val icon: FinanceFieldIconType = FinanceFieldIconType.Currency
    }

    data object Articles : SettingsListItem {
        override val saveableKey: String = "articles"
        override val titleResId: Int = R.string.settings_articles
        override val icon: FinanceFieldIconType = FinanceFieldIconType.Article
    }

    data object Theme : SettingsListItem {
        override val saveableKey: String = "theme"
        override val titleResId: Int = R.string.settings_theme_appearance
        override val icon: FinanceFieldIconType = FinanceFieldIconType.SettingsTheme
    }

    data object Language : SettingsListItem {
        override val saveableKey: String = "language"
        override val titleResId: Int = R.string.settings_language_short
        override val icon: FinanceFieldIconType = FinanceFieldIconType.SettingsLanguage
    }

    data object PinCode : SettingsListItem {
        override val saveableKey: String = "pin_code"
        override val titleResId: Int = R.string.settings_pin_code
        override val icon: FinanceFieldIconType = FinanceFieldIconType.SettingsPinCode
    }

    data object Biometrics : SettingsListItem {
        override val saveableKey: String = "biometrics"
        override val titleResId: Int = R.string.settings_biometrics
        override val icon: FinanceFieldIconType = FinanceFieldIconType.SettingsBiometrics
    }

    companion object {
        fun fromSaveableKey(saveableKey: String?): SettingsListItem? {
            return SettingsSections
                .flatMap(SettingsSection::items)
                .firstOrNull { item -> item.saveableKey == saveableKey }
        }
    }
}

@Immutable
data class SettingsSection(
    @StringRes val titleResId: Int,
    val items: List<SettingsListItem>
)

val SettingsSections: List<SettingsSection> = listOf(
    SettingsSection(
        titleResId = R.string.settings_wallet_section,
        items = listOf(
            SettingsListItem.Currency,
            SettingsListItem.Articles
        )
    ),
    SettingsSection(
        titleResId = R.string.settings_interface_section,
        items = listOf(
            SettingsListItem.Theme,
            SettingsListItem.Language
        )
    ),
    SettingsSection(
        titleResId = R.string.settings_security_section,
        items = listOf(
            SettingsListItem.PinCode,
            SettingsListItem.Biometrics
        )
    )
)
