package com.example.financeapp.presentation.bottomSheets.settings

import com.example.financeapp.R
import com.example.financeapp.domain.model.AuthProtectionState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinCodeSettingsProcessorTest {

    @Test
    fun currentStep_withValidCodeForChange_opensNewCodeStep() = runBlocking {
        var authenticated = false

        val result = completePinCodeStep(
            step = PinCodeStep.Current,
            enteredPinCode = "1234",
            newPinCode = "",
            onVerifyPinCode = { true },
            onSetPinCode = {},
            onCanAttemptPin = { true },
            onPinFailure = { AuthProtectionState() },
            onAuthSuccess = { authenticated = true },
            action = PinCodeAction.Change
        )

        assertEquals(PinCodeStep.New, result.step)
        assertTrue(authenticated)
        assertFalse(result.isResetConfirmationRequired)
    }

    @Test
    fun currentStep_withInvalidCode_registersFailureAndShowsError() = runBlocking {
        var failureRegistered = false

        val result = completePinCodeStep(
            step = PinCodeStep.Current,
            enteredPinCode = "0000",
            newPinCode = "",
            onVerifyPinCode = { false },
            onSetPinCode = {},
            onCanAttemptPin = { true },
            onPinFailure = {
                failureRegistered = true
                AuthProtectionState()
            },
            onAuthSuccess = {},
            action = PinCodeAction.Change
        )

        assertEquals(PinCodeStep.Current, result.step)
        assertEquals(R.string.settings_pin_wrong, result.errorMessageResId)
        assertTrue(failureRegistered)
    }

    @Test
    fun confirmStep_withDifferentCode_returnsToNewCodeStep() = runBlocking {
        var saved = false

        val result = completePinCodeStep(
            step = PinCodeStep.Confirm,
            enteredPinCode = "4321",
            newPinCode = "1234",
            onVerifyPinCode = { true },
            onSetPinCode = { saved = true },
            onCanAttemptPin = { true },
            onPinFailure = { AuthProtectionState() },
            onAuthSuccess = {},
            action = PinCodeAction.Change
        )

        assertEquals(PinCodeStep.New, result.step)
        assertEquals(R.string.settings_pin_mismatch, result.errorMessageResId)
        assertFalse(saved)
    }
}
