package com.example.financeapp.presentation.bottomSheets.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.R
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.FinanceFieldIcon
import com.example.financeapp.presentation.common.components.base.FinanceModalBottomSheet
import com.example.financeapp.presentation.common.components.base.RoundFrame

@Composable
fun SettingsBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    isBiometricAvailable: Boolean = false,
    onItemClick: (SettingsListItem) -> Unit = {}
) {
    FinanceModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        SettingsSheetContent(
            isBiometricAvailable = isBiometricAvailable,
            onItemClick = onItemClick
        )
    }
}

@Composable
fun SettingsSheetContent(
    modifier: Modifier = Modifier,
    isBiometricAvailable: Boolean = false,
    onItemClick: (SettingsListItem) -> Unit = {}
) {
    val spacing = LocalSpacing.current
    val sections = SettingsSections.mapNotNull { section ->
        val visibleItems = section.items.filter { item ->
            item != SettingsListItem.Biometrics || isBiometricAvailable
        }
        section.copy(items = visibleItems).takeIf { visibleItems.isNotEmpty() }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = spacing.settingsTitleHorizontal,
                vertical = spacing.settingsTitleVertical
            ),
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        sections.forEach { section ->
            SettingsSectionList(
                titleResId = section.titleResId,
                items = section.items,
                onItemClick = onItemClick
            )
        }

        Spacer(modifier = Modifier.height(spacing.settingsBottomPadding))
    }
}

@Composable
private fun SettingsSectionList(
    @StringRes titleResId: Int,
    items: List<SettingsListItem>,
    onItemClick: (SettingsListItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = spacing.settingsSectionTop)
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = spacing.settingsSectionTitleHorizontal,
                vertical = spacing.settingsSectionTitleVertical
            ),
            text = stringResource(titleResId),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        items.forEach { item ->
            SettingsSectionRow(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun SettingsSectionRow(
    item: SettingsListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(sizing.settingsRowHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.settingsRowHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.settingsRowGap)
    ) {
        RoundFrame(
            size = sizing.settingsLeadingFrame,
            content = {
                FinanceFieldIcon(
                    icon = item.icon,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(sizing.settingsLeadingIcon)
                )
            }
        )
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(item.titleResId),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(sizing.settingsTrailingIcon)
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 622)
@Composable
private fun SettingsSheetContentPreview() {
    FinanceAppTheme(dynamicColor = false) {
        SettingsSheetContent(isBiometricAvailable = true)
    }
}
