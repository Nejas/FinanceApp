package com.example.financeapp.presentation.bottomSheets.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.presentation.common.testing.FinanceTestTags
import org.junit.Rule
import org.junit.Test

class ThemeSettingsSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clickingDarkThemeOptionSelectsDarkMode() {
        composeRule.setContent {
            FinanceAppTheme(dynamicColor = false) {
                var selectedThemeMode by remember { mutableStateOf(AppThemeMode.LIGHT) }

                ThemeSettingsSheet(
                    selectedThemeMode = selectedThemeMode,
                    onThemeModeSelected = { themeMode -> selectedThemeMode = themeMode }
                )
            }
        }

        composeRule
            .onNodeWithTag(FinanceTestTags.themeOption(AppThemeMode.DARK))
            .performClick()

        composeRule
            .onNodeWithTag(FinanceTestTags.themeOption(AppThemeMode.DARK))
            .assertIsSelected()
    }
}
