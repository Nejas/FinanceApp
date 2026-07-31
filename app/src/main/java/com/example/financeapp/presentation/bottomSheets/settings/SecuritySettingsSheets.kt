package com.example.financeapp.presentation.bottomSheets.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.financeapp.R
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.AuthProtectionState
import com.example.financeapp.presentation.common.components.PinCodeEntryContent
import com.example.financeapp.presentation.common.components.PinCodeLength
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Coordinates the UI state of changing or resetting the local PIN code. */
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
    var successMessageResId by rememberSaveable { mutableIntStateOf(R.string.settings_pin_changed) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val coroutineScope = rememberCoroutineScope()
    val retryAfterSeconds = authProtectionState.pinRetryAfterSeconds(nowMillis)
    val isPinLocked = retryAfterSeconds > 0L

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
        modifier = modifier.fillMaxWidth().padding(
            start = spacing.securitySheetHorizontal,
            end = spacing.securitySheetHorizontal,
            bottom = spacing.securitySheetBottom
        )
    ) {
        when {
            isSuccessVisible -> PinCodeSuccessContent(successMessageResId)
            action == null -> PinCodeActionSelectionContent(
                onChangeClick = { action = PinCodeAction.Change },
                onResetClick = { action = PinCodeAction.Reset }
            )
            else -> {
                val selectedAction = requireNotNull(action)
                Text(
                    modifier = Modifier.fillMaxWidth().padding(vertical = spacing.settingsTitleVertical),
                    text = stringResource(selectedAction.titleResId),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                PinCodeEntryContent(
                    value = input,
                    message = if (isPinLocked) {
                        stringResource(R.string.auth_retry_after_seconds, retryAfterSeconds)
                    } else {
                        stringResource(errorMessageResId ?: step.descriptionResId)
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
                                    val nextState = completePinCodeStep(
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
                                    showResetConfirmation = nextState.isResetConfirmationRequired
                                    step = nextState.step
                                    newPinCode = nextState.newPinCode
                                    errorMessageResId = nextState.errorMessageResId
                                    input = ""
                                    isProcessing = false
                                    isSuccessVisible = nextState.isCompleted
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    if (showResetConfirmation) {
        PinCodeResetConfirmationDialog(
            onDismissRequest = { showResetConfirmation = false },
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

private const val PinCodeSuccessCloseDelayMillis = 1_000L
private const val AuthRetryTickerMillis = 1_000L

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
