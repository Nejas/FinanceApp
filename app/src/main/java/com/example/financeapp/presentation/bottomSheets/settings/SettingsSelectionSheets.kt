package com.example.financeapp.presentation.bottomSheets.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.financeapp.R
import com.example.financeapp.core.localization.AppLanguage
import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.core.theme.ColorDarkBackground
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.icons.FinanceCheckIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsMoonIcon
import com.example.financeapp.presentation.common.components.icons.FinanceThemeLightIcon
import com.example.financeapp.presentation.common.components.icons.FinanceThemeSystemIcon

@Composable
fun LanguageSettingsSheet(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.settingsBottomPadding)
    ) {
        SettingsPickerTitle(title = stringResource(R.string.settings_language_short))

        AppLanguage.entries.forEach { language ->
            LanguageOptionRow(
                language = language,
                isSelected = language == selectedLanguage,
                onClick = { onLanguageSelected(language) }
            )
        }
    }
}

@Composable
fun ThemeSettingsSheet(
    selectedThemeMode: AppThemeMode,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = spacing.settingsBottomPadding)
    ) {
        SettingsPickerTitle(title = stringResource(R.string.settings_theme_appearance))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = spacing.settingsTitleHorizontal,
                    vertical = spacing.settingsPickerOptionsVertical
                ),
            horizontalArrangement = Arrangement.spacedBy(spacing.settingsPickerOptionGap)
        ) {
            AppThemeMode.SelectionOrder.forEach { themeMode ->
                ThemeOptionCard(
                    themeMode = themeMode,
                    isSelected = themeMode == selectedThemeMode,
                    onClick = { onThemeModeSelected(themeMode) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SettingsPickerTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Text(
        modifier = modifier.padding(
            horizontal = spacing.settingsTitleHorizontal,
            vertical = spacing.settingsTitleVertical
        ),
        text = title,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun LanguageOptionRow(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.settingsTitleHorizontal)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(sizing.settingsPickerRowHeight)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.settingsPickerOptionGap)
        ) {
            Text(
                text = language.flag,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.size(sizing.icon)
            )
            Text(
                modifier = Modifier.weight(1f),
                text = language.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isSelected) {
                FinanceCheckIcon(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(sizing.smallIcon)
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = spacing.hairline
        )
    }
}

@Composable
private fun ThemeOptionCard(
    themeMode: AppThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = modifier
            .height(sizing.settingsThemeCardHeight)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(sizing.settingsThemeCardCorner),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(spacing.hairline * 2, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            ThemePreview(
                themeMode = themeMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sizing.settingsThemePreviewHeight)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
            ) {
                ThemeModeIcon(
                    themeMode = themeMode,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(sizing.selectionIndicatorInner)
                )
                Text(
                    text = stringResource(themeMode.titleResId),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ThemePreview(
    themeMode: AppThemeMode,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val shape = RoundedCornerShape(LocalSizing.current.settingsThemePreviewCorner)
    val previewModifier = modifier
        .background(
            brush = themeMode.previewBrush(),
            shape = shape
        )

    Box(
        modifier = previewModifier
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            color = Color.Transparent,
            border = BorderStroke(spacing.hairline, MaterialTheme.colorScheme.outlineVariant),
            content = {}
        )
    }
}

@Composable
private fun ThemeModeIcon(
    themeMode: AppThemeMode,
    color: Color,
    modifier: Modifier = Modifier
) {
    when (themeMode) {
        AppThemeMode.SYSTEM -> FinanceThemeSystemIcon(color = color, modifier = modifier)
        AppThemeMode.LIGHT -> FinanceThemeLightIcon(color = color, modifier = modifier)
        AppThemeMode.DARK -> FinanceSettingsMoonIcon(color = color, modifier = modifier)
    }
}

private val AppLanguage.flag: String
    get() = when (this) {
        AppLanguage.RUSSIAN -> "🇷🇺"
        AppLanguage.ENGLISH -> "🇬🇧"
        AppLanguage.GERMAN -> "🇩🇪"
        AppLanguage.FRENCH -> "🇫🇷"
        AppLanguage.SPANISH -> "🇪🇸"
    }

private val AppLanguage.displayName: String
    get() = when (this) {
        AppLanguage.RUSSIAN -> "Русский"
        AppLanguage.ENGLISH -> "English"
        AppLanguage.GERMAN -> "Deutsch"
        AppLanguage.FRENCH -> "Français"
        AppLanguage.SPANISH -> "Español"
    }

private val AppThemeMode.titleResId: Int
    get() = when (this) {
        AppThemeMode.SYSTEM -> R.string.theme_system
        AppThemeMode.LIGHT -> R.string.theme_light
        AppThemeMode.DARK -> R.string.theme_dark
    }

private fun AppThemeMode.previewBrush(): Brush {
    return when (this) {
        AppThemeMode.SYSTEM -> Brush.verticalGradient(
            colors = listOf(Color.White, Color.Black)
        )
        AppThemeMode.LIGHT -> Brush.verticalGradient(
            colors = listOf(Color.White, Color.White)
        )
        AppThemeMode.DARK -> Brush.verticalGradient(
            colors = listOf(ColorDarkBackground, ColorDarkBackground)
        )
    }
}
