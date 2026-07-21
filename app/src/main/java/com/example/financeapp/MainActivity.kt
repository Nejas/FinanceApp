package com.example.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.presentation.main.FinanceApp
import com.example.financeapp.presentation.splash.DotLottieSplashScreen
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
            FinanceAppTheme {
                var showSplash by remember { mutableStateOf(true) }
                FinanceApp()

                if (showSplash) {
                    DotLottieSplashScreen(
                        onFinished = { showSplash = false }
                    )
                }
            }
        }
    }
}
