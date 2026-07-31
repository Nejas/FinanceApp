package com.example.financeapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.financeapp.core.localization.AppLanguage
import com.example.financeapp.core.theme.AppThemeMode.Companion.resolveDarkTheme
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.presentation.auth.AuthGateScreen
import com.example.financeapp.presentation.auth.AuthSetupScreen
import com.example.financeapp.presentation.auth.BiometricPromptController
import com.example.financeapp.presentation.main.FinanceApp
import com.example.financeapp.presentation.settings.UserSettingsViewModel
import com.example.financeapp.presentation.splash.DotLottieSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var biometricPromptController: BiometricPromptController

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
        biometricPromptController = BiometricPromptController(this)
        setContent {
            val settingsViewModel: UserSettingsViewModel = hiltViewModel()
            val userSettings by settingsViewModel.settings.collectAsState()
            val hasPinCode by settingsViewModel.hasPinCode.collectAsState()
            val isPinCodeSetupOnboardingShown by settingsViewModel
                .isPinCodeSetupOnboardingShown
                .collectAsState()
            val authProtectionState by settingsViewModel.authProtectionState.collectAsState()
            val isBiometricAvailable = remember {
                BiometricManager.from(this)
                    .canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
            }
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
                var isAuthenticated by rememberSaveable { mutableStateOf(false) }
                var isAuthSetupInProgress by rememberSaveable { mutableStateOf(false) }
                val shouldStartAuthSetup = !isAuthenticated &&
                    hasPinCode == false &&
                    isPinCodeSetupOnboardingShown == false
                val shouldShowAuthSetup = !isAuthenticated &&
                    (isAuthSetupInProgress || shouldStartAuthSetup)

                LaunchedEffect(shouldStartAuthSetup) {
                    if (shouldStartAuthSetup) {
                        isAuthSetupInProgress = true
                    }
                }

                when {
                    showSplash ||
                        hasPinCode == null ||
                        isPinCodeSetupOnboardingShown == null -> {
                        DotLottieSplashScreen(
                            onFinished = { showSplash = false }
                        )
                    }
                    shouldShowAuthSetup -> {
                        AuthSetupScreen(
                            isBiometricAvailable = isBiometricAvailable,
                            isBiometricOfferShown = userSettings.isBiometricLoginOfferShown,
                            onSetPinCode = settingsViewModel::setPinCode,
                            onBiometricLoginEnabledChange = settingsViewModel::setBiometricLoginEnabledSuspend,
                            onBiometricOfferShownChange = settingsViewModel::setBiometricLoginOfferShown,
                            onPinCodeSetupOnboardingShownChange = settingsViewModel::setPinCodeSetupOnboardingShown,
                            onAuthSuccess = settingsViewModel::registerAuthSuccess,
                            onBiometricAuthenticationRequest = biometricPromptController::authenticate,
                            onCompleted = {
                                isAuthSetupInProgress = false
                                isAuthenticated = true
                            }
                        )
                    }
                    hasPinCode == true && !isAuthenticated -> {
                        AuthGateScreen(
                            authProtectionState = authProtectionState,
                            isBiometricLoginEnabled = userSettings.isBiometricLoginEnabled,
                            isBiometricAvailable = isBiometricAvailable,
                            onVerifyPinCode = settingsViewModel::verifyPinCode,
                            onCanAttemptPin = settingsViewModel::canAttemptPin,
                            onPinFailure = settingsViewModel::registerPinFailure,
                            onBiometricFailure = settingsViewModel::registerBiometricFailure,
                            onAuthSuccess = settingsViewModel::registerAuthSuccess,
                            onBiometricAuthenticationRequest = biometricPromptController::authenticate,
                            onBiometricAuthenticationCancel = biometricPromptController::cancel,
                            onAuthenticated = {
                                isAuthenticated = true
                            }
                        )
                    }
                    else -> FinanceApp(
                        userSettings = userSettings,
                        hasPinCode = hasPinCode == true,
                        authProtectionState = authProtectionState,
                        isBiometricAvailable = isBiometricAvailable,
                        selectedLanguage = selectedLanguage,
                        onLanguageSelected = { language ->
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(language.languageTag)
                            )
                        },
                        onThemeModeSelected = settingsViewModel::setThemeMode,
                        onCurrencySelected = settingsViewModel::setCurrency,
                        onBiometricLoginEnabledChange = settingsViewModel::setBiometricLoginEnabled,
                        onBiometricAuthenticationRequest = biometricPromptController::authenticate,
                        onVerifyPinCode = settingsViewModel::verifyPinCode,
                        onSetPinCode = settingsViewModel::setPinCode,
                        onClearPinCode = settingsViewModel::clearPinProtection,
                        onCanAttemptPin = settingsViewModel::canAttemptPin,
                        onPinFailure = settingsViewModel::registerPinFailure,
                        onAuthSuccess = settingsViewModel::registerAuthSuccess
                    )
                }
            }
        }
    }

}
