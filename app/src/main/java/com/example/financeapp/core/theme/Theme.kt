package com.example.financeapp.core.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat


private val DarkColorScheme = darkColorScheme(
    primary = ChartPurple,
    onPrimary = ColorDarkBackground,
    primaryContainer = ColorDarkSurfaceVariant,
    onPrimaryContainer = ColorDarkOnBackground,
    secondary = ColorDarkSelectedContainer,
    onSecondary = ColorDarkOnBackground,
    secondaryContainer = ColorDarkSelectedContainer,
    onSecondaryContainer = ColorDarkOnBackground,
    background = ColorDarkBackground,
    onBackground = ColorDarkOnBackground,
    surface = ColorDarkBackground,
    onSurface = ColorDarkOnBackground,
    surfaceVariant = ColorDarkSurfaceVariant,
    onSurfaceVariant = ColorDarkOnBackground,
    outline = ColorDarkOutline,
    outlineVariant = ColorDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = FinancePrimary,
    onPrimary = FinanceBackground,
    primaryContainer = FinanceSelectedContainer,
    onPrimaryContainer = FinancePrimary,
    secondary = FinanceSelectedContainer,
    onSecondary = FinancePrimary,
    secondaryContainer = FinanceSelectedContainer,
    onSecondaryContainer = FinancePrimary,
    background = FinanceBackground,
    onBackground = FinanceOnBackground,
    surface = FinanceBackground,
    onSurface = FinanceOnBackground,
    surfaceVariant = FinanceNavigationBackground,
    onSurfaceVariant = FinanceOnBackground,
    outline = FinanceOutline,
    outlineVariant = FinanceOutlineVariant,
    error = FinanceWarning,
    tertiary = FinancePositive
)

@Composable
fun FinanceAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme

        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surfaceVariant.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSizing provides Sizing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
