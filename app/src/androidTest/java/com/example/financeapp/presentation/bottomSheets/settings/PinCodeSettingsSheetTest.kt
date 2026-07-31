package com.example.financeapp.presentation.bottomSheets.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.example.financeapp.core.theme.FinanceAppTheme
import com.example.financeapp.domain.model.AuthProtectionState
import com.example.financeapp.presentation.common.testing.FinanceTestTags
import org.junit.Rule
import org.junit.Test

class PinCodeSettingsSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun enteringMatchingPinCodeSavesPin() {
        var savedPinCode: String? = null

        composeRule.setContent {
            FinanceAppTheme(dynamicColor = false) {
                PinCodeSettingsSheet(
                    hasPinCode = false,
                    authProtectionState = AuthProtectionState(),
                    onVerifyPinCode = { true },
                    onSetPinCode = { pinCode -> savedPinCode = pinCode },
                    onClearPinCode = {},
                    onCanAttemptPin = { true },
                    onPinFailure = { AuthProtectionState() },
                    onAuthSuccess = {},
                    onPinCodeChanged = {}
                )
            }
        }

        composeRule.onNodeWithTag(FinanceTestTags.PinCodeInput).performTextInput("1234")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(FinanceTestTags.PinCodeInput).performTextInput("1234")

        composeRule.waitUntil {
            savedPinCode == "1234"
        }
    }
}
