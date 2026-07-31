package com.example.financeapp.presentation.auth

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.financeapp.R

/** Owns the Android biometric prompt lifecycle for the presentation layer. */
class BiometricPromptController(
    private val activity: FragmentActivity
) {
    private var prompt: BiometricPrompt? = null

    fun authenticate(
        onAuthenticated: () -> Unit,
        onFailure: (isFailedAttempt: Boolean) -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.settings_face_id))
            .setSubtitle(activity.getString(R.string.settings_biometry_description))
            .setNegativeButtonText(activity.getString(R.string.action_cancel))
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()
        prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    prompt = null
                    onAuthenticated()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    prompt = null
                    when (errorCode) {
                        BiometricPrompt.ERROR_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED -> onFailure(false)
                        else -> onFailure(true)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailure(true)
                }
            }
        ).also { it.authenticate(promptInfo) }
    }

    fun cancel() {
        prompt?.cancelAuthentication()
        prompt = null
    }
}
