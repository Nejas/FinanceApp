package com.example.financeapp.presentation.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.financeapp.presentation.common.components.icons.FinanceAccountCardIcon
import com.example.financeapp.presentation.common.components.icons.FinanceArticleIcon
import com.example.financeapp.presentation.common.components.icons.FinanceCalendarIcon
import com.example.financeapp.presentation.common.components.icons.FinanceClockIcon
import com.example.financeapp.presentation.common.components.icons.FinanceCurrencyIcon
import com.example.financeapp.presentation.common.components.icons.FinanceEmojiIcon
import com.example.financeapp.presentation.common.components.icons.FinanceListTypeIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsBiometryIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsArticleIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsGlobeIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsLockIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsMoonIcon
import com.example.financeapp.presentation.common.components.icons.FinanceTagIcon
import com.example.financeapp.presentation.common.model.FinanceFieldIconType

@Composable
fun FinanceFieldIcon(
    icon: FinanceFieldIconType,
    color: Color,
    modifier: Modifier = Modifier
) {
    when (icon) {
        FinanceFieldIconType.Account -> FinanceAccountCardIcon(color = color, modifier = modifier)
        FinanceFieldIconType.Article -> FinanceArticleIcon(color = color, modifier = modifier)
        FinanceFieldIconType.Calendar -> FinanceCalendarIcon(color = color, modifier = modifier)
        FinanceFieldIconType.Clock -> FinanceClockIcon(color = color, modifier = modifier)
        FinanceFieldIconType.Currency -> FinanceCurrencyIcon(color = color, modifier = modifier)
        FinanceFieldIconType.Emoji -> FinanceEmojiIcon(color = color, modifier = modifier)
        FinanceFieldIconType.ListType -> FinanceListTypeIcon(color = color, modifier = modifier)
        FinanceFieldIconType.SettingsBiometrics -> FinanceSettingsBiometryIcon(color = color, modifier = modifier)
        FinanceFieldIconType.SettingsLanguage -> FinanceSettingsGlobeIcon(color = color, modifier = modifier)
        FinanceFieldIconType.SettingsPinCode -> FinanceSettingsLockIcon(color = color, modifier = modifier)
        FinanceFieldIconType.SettingsTheme -> FinanceSettingsMoonIcon(color = color, modifier = modifier)
        FinanceFieldIconType.Tag -> FinanceTagIcon(color = color, modifier = modifier)
    }
}
