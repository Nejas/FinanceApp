package com.example.financeapp.presentation.bottomSheets.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.R
import com.example.financeapp.core.theme.ColorDarkBackground
import com.example.financeapp.core.theme.ColorDarkSecurityControlContainer
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.FinanceBiometricIconContainer
import com.example.financeapp.core.theme.FinanceSecurityControlContainer
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsBiometryIcon

@Composable
fun BiometricSettingsSheet(
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val controlContainerColor = if (MaterialTheme.colorScheme.surface == ColorDarkBackground) {
        ColorDarkSecurityControlContainer
    } else {
        FinanceSecurityControlContainer
    }
    Column(
        modifier = modifier.fillMaxWidth().padding(
            start = spacing.securitySheetHorizontal,
            end = spacing.securitySheetHorizontal,
            bottom = spacing.securitySheetBottom
        )
    ) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(bottom = spacing.biometricTitleBottom),
            text = stringResource(R.string.settings_face_id),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = spacing.biometricContentVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.biometricContentGap)
        ) {
            Box(
                modifier = Modifier.size(sizing.biometricIconContainer)
                    .background(FinanceBiometricIconContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FinanceSettingsBiometryIcon(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(sizing.biometricIcon)
                )
            }
            Text(
                modifier = Modifier.padding(horizontal = spacing.biometricDescriptionHorizontal),
                text = stringResource(R.string.settings_biometry_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(sizing.biometricToggleRowHeight)
                .background(controlContainerColor, RoundedCornerShape(sizing.biometricToggleRowCorner))
                .clickable { onEnabledChange(!isEnabled) }
                .padding(horizontal = spacing.biometricToggleRowPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.settings_face_touch_id),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Switch(checked = isEnabled, onCheckedChange = onEnabledChange)
        }
        Text(
            modifier = Modifier.padding(top = spacing.xs),
            text = stringResource(R.string.settings_biometry_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 416)
@Composable
private fun BiometricSettingsSheetPreview() {
    FinanceAppTheme(dynamicColor = false) {
        BiometricSettingsSheet(isEnabled = true, onEnabledChange = {})
    }
}
