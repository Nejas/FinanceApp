package com.example.financeapp.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dotlottie.dlplayer.Mode
import com.example.financeapp.core.theme.LocalSpacing
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.delay

@Composable
fun DotLottieSplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MILLIS)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        DotLottieAnimation(
            source = DotLottieSource.Asset(SPLASH_ASSET_NAME),
            autoplay = true,
            loop = false,
            speed = 1f,
            useFrameInterpolation = true,
            playMode = Mode.FORWARD,
            modifier = Modifier.size(spacing.screenBottomContent * 2)
        )
    }
}

private const val SPLASH_ASSET_NAME = "wallet_animation.lottie"
private const val SPLASH_DURATION_MILLIS = 1_600L
