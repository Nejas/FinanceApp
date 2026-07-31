package com.example.financeapp.presentation.bottomSheets.settings

import androidx.annotation.StringRes
import com.example.financeapp.R
import com.example.financeapp.domain.model.AuthProtectionState

/** Resolves the next UI state for one completed PIN entry. */
internal suspend fun completePinCodeStep(
    step: PinCodeStep,
    enteredPinCode: String,
    newPinCode: String,
    onVerifyPinCode: suspend (String) -> Boolean,
    onSetPinCode: suspend (String) -> Unit,
    onCanAttemptPin: suspend () -> Boolean,
    onPinFailure: suspend () -> AuthProtectionState,
    onAuthSuccess: suspend () -> Unit,
    action: PinCodeAction
): PinCodeNextState = when (step) {
    PinCodeStep.Current -> when {
        !onCanAttemptPin() -> PinCodeNextState(
            step = PinCodeStep.Current,
            errorMessageResId = R.string.settings_pin_wrong
        )
        onVerifyPinCode(enteredPinCode) -> {
            onAuthSuccess()
            if (action == PinCodeAction.Reset) {
                PinCodeNextState(step = PinCodeStep.Current, isResetConfirmationRequired = true)
            } else {
                PinCodeNextState(step = PinCodeStep.New)
            }
        }
        else -> {
            onPinFailure()
            PinCodeNextState(
                step = PinCodeStep.Current,
                errorMessageResId = R.string.settings_pin_wrong
            )
        }
    }
    PinCodeStep.New -> PinCodeNextState(step = PinCodeStep.Confirm, newPinCode = enteredPinCode)
    PinCodeStep.Confirm -> if (enteredPinCode == newPinCode) {
        onSetPinCode(enteredPinCode)
        onAuthSuccess()
        PinCodeNextState(step = PinCodeStep.Confirm, isCompleted = true)
    } else {
        PinCodeNextState(step = PinCodeStep.New, errorMessageResId = R.string.settings_pin_mismatch)
    }
}

internal data class PinCodeNextState(
    val step: PinCodeStep,
    val newPinCode: String = "",
    @StringRes val errorMessageResId: Int? = null,
    val isCompleted: Boolean = false,
    val isResetConfirmationRequired: Boolean = false
)

internal enum class PinCodeAction(@StringRes val titleResId: Int) {
    Change(R.string.settings_pin_change_title),
    Reset(R.string.settings_pin_reset_title)
}

internal enum class PinCodeStep(@StringRes val descriptionResId: Int) {
    Current(R.string.settings_pin_current_description),
    New(R.string.settings_pin_new_description),
    Confirm(R.string.settings_pin_confirm_description)
}
