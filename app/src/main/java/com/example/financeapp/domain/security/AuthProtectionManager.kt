package com.example.financeapp.domain.security

import com.example.financeapp.domain.model.AuthProtectionState
import kotlinx.coroutines.flow.Flow

interface AuthProtectionManager {
    val state: Flow<AuthProtectionState>

    suspend fun canAttemptPin(nowMillis: Long = System.currentTimeMillis()): Boolean

    suspend fun registerPinFailure(nowMillis: Long = System.currentTimeMillis()): AuthProtectionState

    suspend fun registerBiometricFailure(): AuthProtectionState

    suspend fun registerSuccess()
}
