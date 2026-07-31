package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.AuthProtectionState
import com.example.financeapp.domain.repository.SecurityRepository
import com.example.financeapp.domain.security.AuthProtectionManager
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SecurityUseCases @Inject constructor(
    private val repository: SecurityRepository,
    private val authProtectionManager: AuthProtectionManager
) {
    val hasPinCode = repository.hasPinCode
    val authProtectionState: Flow<AuthProtectionState> = authProtectionManager.state

    suspend fun setPinCode(pinCode: String) {
        repository.setPinCode(pinCode)
    }

    suspend fun clearPinCode() {
        repository.clearPinCode()
    }

    suspend fun verifyPinCode(pinCode: String): Boolean {
        return repository.verifyPinCode(pinCode)
    }

    suspend fun canAttemptPin(): Boolean {
        return authProtectionManager.canAttemptPin()
    }

    suspend fun registerPinFailure(): AuthProtectionState {
        return authProtectionManager.registerPinFailure()
    }

    suspend fun registerBiometricFailure(): AuthProtectionState {
        return authProtectionManager.registerBiometricFailure()
    }

    suspend fun registerAuthSuccess() {
        authProtectionManager.registerSuccess()
    }
}
