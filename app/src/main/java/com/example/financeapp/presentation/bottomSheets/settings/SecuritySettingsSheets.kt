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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.financeapp.domain.model.AuthProtectionState
import com.example.financeapp.presentation.common.components.PinCodeEntryContent
import com.example.financeapp.presentation.common.components.PinCodeLength
import com.example.financeapp.presentation.common.components.base.RoundFrame
import com.example.financeapp.presentation.common.components.icons.FinanceCheckIcon
import com.example.financeapp.presentation.common.components.icons.FinanceDeleteIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsBiometryIcon
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsLockIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinCodeSettingsSheet(
    hasPinCode: Boolean,
    authProtectionState: AuthProtectionState,
    onVerifyPinCode: suspend (String) -> Boolean,
    onSetPinCode: suspend (String) -> Unit,
    onClearPinCode: suspend () -> Unit,
    onCanAttemptPin: suspend () -> Boolean,
    onPinFailure: suspend () -> AuthProtectionState,
    onAuthSuccess: suspend () -> Unit,
    onPinCodeChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    var action by rememberSaveable(hasPinCode) {
        mutableStateOf<PinCodeAction?>(if (hasPinCode) null else PinCodeAction.Change)
    }
    var step by rememberSaveable(hasPinCode, action) {
        mutableStateOf(if (hasPinCode) PinCodeStep.Current else PinCodeStep.New)
    }
    var input by rememberSaveable(step) { mutableStateOf("") }
    var newPinCode by rememberSaveable { mutableStateOf("") }
    var errorMessageResId by rememberSaveable { mutableStateOf<Int?>(null) }
    var isProcessing by rememberSaveable { mutableStateOf(false) }
    var isSuccessVisible by rememberSaveable { mutableStateOf(false) }
    var showResetConfirmation by rememberSaveable { mutableStateOf(false) }
    var successMessageResId by rememberSaveable { mutableStateOf(R.string.settings_pin_changed) }
    val coroutineScope = rememberCoroutineScope()
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val retryAfterSeconds = authProtectionState.pinRetryAfterSeconds(nowMillis)
    val isPinLocked = retryAfterSeconds > 0L
    val selectedAction = action

    LaunchedEffect(isSuccessVisible) {
        if (isSuccessVisible) {
            delay(PinCodeSuccessCloseDelayMillis)
            onPinCodeChanged()
        }
    }

    LaunchedEffect(authProtectionState.pinLockedUntilMillis) {
        while (authProtectionState.isPinLocked(System.currentTimeMillis())) {
            nowMillis = System.currentTimeMillis()
            delay(AuthRetryTickerMillis)
        }
        nowMillis = System.currentTimeMillis()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = spacing.securitySheetHorizontal,
                end = spacing.securitySheetHorizontal,
                bottom = spacing.securitySheetBottom
            )
    ) {
        if (isSuccessVisible) {
            PinCodeSuccessContent(messageResId = successMessageResId)
        } else if (selectedAction == null) {
            PinCodeActionSelectionContent(
                onChangeClick = { action = PinCodeAction.Change },
                onResetClick = { action = PinCodeAction.Reset }
            )
        } else {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.settingsTitleVertical),
                text = stringResource(selectedAction.titleResId),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val messageResId = errorMessageResId ?: step.descriptionResId
            PinCodeEntryContent(
                value = input,
                message = if (isPinLocked) {
                    stringResource(R.string.auth_retry_after_seconds, retryAfterSeconds)
                } else {
                    stringResource(messageResId)
                },
                isError = errorMessageResId != null || isPinLocked,
                isEnabled = !isProcessing && !isPinLocked,
                onValueChange = { value ->
                    if (!isProcessing && !isPinLocked) {
                        input = value
                        errorMessageResId = null
                        if (value.length == PinCodeLength) {
                            isProcessing = true
                            coroutineScope.launch {
                                val nextState = handlePinCodeCompleted(
                                    step = step,
                                    enteredPinCode = value,
                                    newPinCode = newPinCode,
                                    onVerifyPinCode = onVerifyPinCode,
                                    onSetPinCode = onSetPinCode,
                                    onCanAttemptPin = onCanAttemptPin,
                                    onPinFailure = onPinFailure,
                                    onAuthSuccess = onAuthSuccess,
                                    action = selectedAction
                                )
                                if (nextState.isResetConfirmationRequired) {
                                    showResetConfirmation = true
                                } else {
                                    step = nextState.step
                                    newPinCode = nextState.newPinCode
                                    errorMessageResId = nextState.errorMessageResId
                                }
                                input = ""
                                isProcessing = false
                                if (nextState.isCompleted) {
                                    isSuccessVisible = true
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    if (showResetConfirmation) {
        PinCodeResetConfirmationDialog(
            onDismissRequest = {
                showResetConfirmation = false
            },
            onConfirmClick = {
                coroutineScope.launch {
                    showResetConfirmation = false
                    isProcessing = true
                    onClearPinCode()
                    successMessageResId = R.string.settings_pin_reset_success
                    isSuccessVisible = true
                    isProcessing = false
                }
            }
        )
    }
}

@Composable
private fun PinCodeActionSelectionContent(
    onChangeClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.settingsTitleVertical),
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
            isDestructive = false
        )
        PinCodeActionRow(
            titleResId = R.string.settings_pin_reset_action,
            onClick = onResetClick,
            isDestructive = true
        )
    }
}

@Composable
private fun PinCodeActionRow(
    titleResId: Int,
    onClick: () -> Unit,
    isDestructive: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

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
                if (isDestructive) {
                    FinanceDeleteIcon(
                        color = contentColor,
                        modifier = Modifier.size(sizing.settingsLeadingIcon)
                    )
                } else {
                    FinanceSettingsLockIcon(
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(sizing.settingsLeadingIcon)
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
private fun PinCodeResetConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.settings_pin_reset_confirm_title))
        },
        text = {
            Text(text = stringResource(R.string.settings_pin_reset_confirm_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(text = stringResource(R.string.settings_pin_reset_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun PinCodeSuccessContent(
    messageResId: Int,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.pinCodeContentVertical),
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
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = spacing.securitySheetHorizontal,
                end = spacing.securitySheetHorizontal,
                bottom = spacing.securitySheetBottom
            )
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.biometricTitleBottom),
            text = stringResource(R.string.settings_face_id),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.biometricContentVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.biometricContentGap)
        ) {
            Box(
                modifier = Modifier
                    .size(sizing.biometricIconContainer)
                    .background(
                        color = FinanceBiometricIconContainer,
                        shape = CircleShape
                    ),
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
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(sizing.biometricToggleRowHeight)
                .background(
                    color = controlContainerColor,
                    shape = RoundedCornerShape(sizing.biometricToggleRowCorner)
                )
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
            Switch(
                checked = isEnabled,
                onCheckedChange = onEnabledChange
            )
        }

        Text(
            modifier = Modifier.padding(top = LocalSpacing.current.xs),
            text = stringResource(R.string.settings_biometry_note),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

private suspend fun handlePinCodeCompleted(
    step: PinCodeStep,
    enteredPinCode: String,
    newPinCode: String,
    onVerifyPinCode: suspend (String) -> Boolean,
    onSetPinCode: suspend (String) -> Unit,
    onCanAttemptPin: suspend () -> Boolean,
    onPinFailure: suspend () -> AuthProtectionState,
    onAuthSuccess: suspend () -> Unit,
    action: PinCodeAction
): PinCodeNextState {
    return when (step) {
        PinCodeStep.Current -> {
            if (!onCanAttemptPin()) {
                PinCodeNextState(
                    step = PinCodeStep.Current,
                    errorMessageResId = R.string.settings_pin_wrong
                )
            } else if (onVerifyPinCode(enteredPinCode)) {
                onAuthSuccess()
                if (action == PinCodeAction.Reset) {
                    PinCodeNextState(
                        step = PinCodeStep.Current,
                        isResetConfirmationRequired = true
                    )
                } else {
                    PinCodeNextState(step = PinCodeStep.New)
                }
            } else {
                onPinFailure()
                PinCodeNextState(
                    step = PinCodeStep.Current,
                    errorMessageResId = R.string.settings_pin_wrong
                )
            }
        }
        PinCodeStep.New -> {
            PinCodeNextState(
                step = PinCodeStep.Confirm,
                newPinCode = enteredPinCode
            )
        }
        PinCodeStep.Confirm -> {
            if (enteredPinCode == newPinCode) {
                onSetPinCode(enteredPinCode)
                onAuthSuccess()
                PinCodeNextState(
                    step = PinCodeStep.Confirm,
                    isCompleted = true
                )
            } else {
                PinCodeNextState(
                    step = PinCodeStep.New,
                    errorMessageResId = R.string.settings_pin_mismatch
                )
            }
        }
    }
}

private data class PinCodeNextState(
    val step: PinCodeStep,
    val newPinCode: String = "",
    val errorMessageResId: Int? = null,
    val isCompleted: Boolean = false,
    val isResetConfirmationRequired: Boolean = false
)

private enum class PinCodeAction(
    val titleResId: Int
) {
    Change(R.string.settings_pin_change_title),
    Reset(R.string.settings_pin_reset_title)
}

private enum class PinCodeStep(
    val descriptionResId: Int
) {
    Current(R.string.settings_pin_current_description),
    New(R.string.settings_pin_new_description),
    Confirm(R.string.settings_pin_confirm_description)
}

private const val PinCodeSuccessCloseDelayMillis = 1_000L
private const val AuthRetryTickerMillis = 1_000L

@Preview(showBackground = true, widthDp = 412, heightDp = 416)
@Composable
private fun BiometricSettingsSheetPreview() {
    FinanceAppTheme(dynamicColor = false) {
        BiometricSettingsSheet(
            isEnabled = true,
            onEnabledChange = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 230)
@Composable
private fun PinCodeSettingsSheetPreview() {
    FinanceAppTheme(dynamicColor = false) {
        PinCodeSettingsSheet(
            hasPinCode = true,
            authProtectionState = AuthProtectionState(),
            onVerifyPinCode = { true },
            onSetPinCode = {},
            onClearPinCode = {},
            onCanAttemptPin = { true },
            onPinFailure = { AuthProtectionState() },
            onAuthSuccess = {},
            onPinCodeChanged = {}
        )
    }
}
