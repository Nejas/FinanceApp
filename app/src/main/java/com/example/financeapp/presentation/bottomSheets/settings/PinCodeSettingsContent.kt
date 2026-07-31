package com.example.financeapp.presentation.bottomSheets.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.financeapp.R
import com.example.financeapp.core.theme.FinanceBiometricIconContainer
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.base.RoundFrame
import com.example.financeapp.presentation.common.components.icons.FinanceCheckIcon
import com.example.financeapp.presentation.common.components.icons.FinanceDeleteIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsLockIcon
import com.example.financeapp.presentation.common.testing.FinanceTestTags

@Composable
internal fun PinCodeActionSelectionContent(
    onChangeClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(vertical = spacing.settingsTitleVertical),
            text = stringResource(R.string.settings_pin_code),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        PinCodeActionRow(
            titleResId = R.string.settings_pin_change_action,
            onClick = onChangeClick,
            isDestructive = false,
            testTag = FinanceTestTags.PinCodeChangeAction
        )
        PinCodeActionRow(
            titleResId = R.string.settings_pin_reset_action,
            onClick = onResetClick,
            isDestructive = true,
            testTag = FinanceTestTags.PinCodeResetAction
        )
    }
}

@Composable
private fun PinCodeActionRow(
    titleResId: Int,
    onClick: () -> Unit,
    isDestructive: Boolean,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(sizing.settingsRowHeight)
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.settingsRowHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.settingsRowGap)
    ) {
        RoundFrame(
            size = sizing.settingsLeadingFrame,
            content = {
                if (isDestructive) {
                    FinanceDeleteIcon(contentColor, Modifier.size(sizing.settingsLeadingIcon))
                } else {
                    FinanceSettingsLockIcon(
                        MaterialTheme.colorScheme.onSecondary,
                        Modifier.size(sizing.settingsLeadingIcon)
                    )
                }
            }
        )
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(titleResId),
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun PinCodeResetConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.settings_pin_reset_confirm_title)) },
        text = { Text(stringResource(R.string.settings_pin_reset_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirmClick) { Text(stringResource(R.string.settings_pin_reset_action)) }
        },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
internal fun PinCodeSuccessContent(messageResId: Int, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = spacing.pinCodeContentVertical),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        RoundFrame(
            size = sizing.biometricIconContainer,
            backgroundColor = FinanceBiometricIconContainer,
            content = {
                FinanceCheckIcon(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(sizing.biometricIcon)
                )
            }
        )
        Text(
            text = stringResource(messageResId),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}
