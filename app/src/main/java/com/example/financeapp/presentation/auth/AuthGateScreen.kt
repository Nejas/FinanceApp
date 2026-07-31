package com.example.financeapp.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.financeapp.R
import com.example.financeapp.core.theme.FinanceBiometricIconContainer
import com.example.financeapp.core.theme.LocalSizing
import com.example.financeapp.core.theme.LocalSpacing
import com.example.financeapp.domain.model.AuthProtectionState
import com.example.financeapp.presentation.common.components.PinCodeEntryContent
import com.example.financeapp.presentation.common.components.PinCodeLength
import com.example.financeapp.presentation.common.components.base.RoundFrame
import com.example.financeapp.presentation.common.components.icons.FinanceSettingsBiometryIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthGateScreen(
    authProtectionState: AuthProtectionState,
    isBiometricLoginEnabled: Boolean,
    isBiometricAvailable: Boolean,
    onVerifyPinCode: suspend (String) -> Boolean,
    onCanAttemptPin: suspend () -> Boolean,
    onPinFailure: suspend () -> AuthProtectionState,
    onBiometricFailure: suspend () -> AuthProtectionState,
    onAuthSuccess: suspend () -> Unit,
    onBiometricAuthenticationRequest: (
        onAuthenticated: () -> Unit,
        onFailure: (isFailedAttempt: Boolean) -> Unit
    ) -> Unit,
    onBiometricAuthenticationCancel: () -> Unit,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val sizing = LocalSizing.current
    val coroutineScope = rememberCoroutineScope()
    var input by rememberSaveable { mutableStateOf("") }
    var isProcessing by rememberSaveable { mutableStateOf(false) }
    var hasPinError by rememberSaveable { mutableStateOf(false) }
    var hasRequestedBiometrics by rememberSaveable { mutableStateOf(false) }
    var pinFocusRequestKey by rememberSaveable { mutableStateOf(0) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val retryAfterSeconds = authProtectionState.pinRetryAfterSeconds(nowMillis)
    val isPinLocked = retryAfterSeconds > 0L
    val canUseBiometrics = isBiometricLoginEnabled &&
        isBiometricAvailable &&
        !authProtectionState.isBiometricLocked &&
        !isPinLocked
    val isBiometricBlockedByLockout = isBiometricLoginEnabled &&
        isBiometricAvailable &&
        (isPinLocked || authProtectionState.isBiometricLocked)

    fun requestBiometricAuthentication() {
        onBiometricAuthenticationRequest(
            {
                coroutineScope.launch {
                    onAuthSuccess()
                    onAuthenticated()
                }
            },
            { isFailedAttempt ->
                coroutineScope.launch {
                    if (isFailedAttempt) {
                        val nextState = onBiometricFailure()
                        if (nextState.isBiometricLocked) {
                            onBiometricAuthenticationCancel()
                        }
                    }
                    pinFocusRequestKey++
                }
            }
        )
    }

    LaunchedEffect(canUseBiometrics) {
        if (canUseBiometrics && !hasRequestedBiometrics) {
            hasRequestedBiometrics = true
            requestBiometricAuthentication()
        }
    }

    LaunchedEffect(authProtectionState.pinLockedUntilMillis) {
        while (authProtectionState.isPinLocked(System.currentTimeMillis())) {
            nowMillis = System.currentTimeMillis()
            delay(AuthRetryTickerMillis)
        }
        nowMillis = System.currentTimeMillis()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = spacing.securitySheetHorizontal,
                end = spacing.securitySheetHorizontal,
                bottom = spacing.authContentLift
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Text(
                text = stringResource(R.string.auth_pin_title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            PinCodeEntryContent(
                value = input,
                message = when {
                    isPinLocked -> stringResource(R.string.auth_retry_after_seconds, retryAfterSeconds)
                    hasPinError -> stringResource(R.string.settings_pin_wrong)
                    else -> stringResource(R.string.auth_pin_description)
                },
                isError = hasPinError || isPinLocked,
                isEnabled = !isProcessing && !isPinLocked,
                focusRequestKey = pinFocusRequestKey,
                onValueChange = { value ->
                    if (!isProcessing && !isPinLocked) {
                        input = value
                        hasPinError = false
                        if (value.length == PinCodeLength) {
                            isProcessing = true
                            coroutineScope.launch {
                                if (!onCanAttemptPin()) {
                                    hasPinError = true
                                } else if (onVerifyPinCode(value)) {
                                    onAuthSuccess()
                                    onAuthenticated()
                                } else {
                                    onPinFailure()
                                    hasPinError = true
                                }
                                input = ""
                                isProcessing = false
                            }
                        }
                    }
                },
                footer = {
                    if (canUseBiometrics) {
                        RoundFrame(
                            modifier = Modifier.clickable {
                                requestBiometricAuthentication()
                            },
                            size = sizing.biometricIconContainer,
                            backgroundColor = FinanceBiometricIconContainer,
                            content = {
                                FinanceSettingsBiometryIcon(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(sizing.biometricIcon)
                                )
                            }
                        )
                    } else if (isBiometricBlockedByLockout) {
                        Text(
                            text = stringResource(R.string.auth_biometric_locked_by_timeout),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        }
    }
}

private const val AuthRetryTickerMillis = 1_000L
