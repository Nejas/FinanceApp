package com.example.financeapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.financeapp.core.localization.AppLanguage
import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.presentation.main.FinanceApp
import com.example.financeapp.presentation.settings.UserSettingsViewModel
import com.example.financeapp.presentation.splash.DotLottieSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.view
                .animate()
                .alpha(0f)
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(300L)
                .withEndAction {
                    splashScreenViewProvider.remove()
                }
                .start()
        }
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: UserSettingsViewModel = hiltViewModel()
            val userSettings by settingsViewModel.settings.collectAsState()
            val applicationLocales = AppCompatDelegate.getApplicationLocales()
            val currentLanguageTag = if (applicationLocales.isEmpty) {
                resources.configuration.locales[0].language
            } else {
                applicationLocales[0]?.language
            }
            val selectedLanguage = AppLanguage.fromLanguageTag(currentLanguageTag.orEmpty())
                ?: AppLanguage.RUSSIAN
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = userSettings.themeMode.resolveDarkTheme(systemDarkTheme)

            FinanceAppTheme(darkTheme = darkTheme) {
                var showSplash by remember { mutableStateOf(true) }
                FinanceApp(
                    userSettings = userSettings,
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { language ->
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(language.languageTag)
                        )
                    },
                    onThemeModeSelected = settingsViewModel::setThemeMode
                )

                if (showSplash) {
                    DotLottieSplashScreen(
                        onFinished = { showSplash = false }
                    )
                }
            }
        }
    }

}

private fun AppThemeMode.resolveDarkTheme(systemDarkTheme: Boolean): Boolean {
    return when (this) {
        AppThemeMode.SYSTEM -> systemDarkTheme
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
}
