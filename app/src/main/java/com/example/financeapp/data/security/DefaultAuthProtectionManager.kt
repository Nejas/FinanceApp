package com.example.financeapp.data.security

import com.example.financeapp.domain.model.AuthProtectionState
import com.example.financeapp.domain.repository.SecurityRepository
import com.example.financeapp.domain.security.AuthProtectionManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultAuthProtectionManager @Inject constructor(
    private val repository: SecurityRepository
) : AuthProtectionManager {

    override val state: Flow<AuthProtectionState> = repository.authProtectionState

    override suspend fun canAttemptPin(nowMillis: Long): Boolean {
        var canAttempt = false

        repository.updateAuthProtectionState { currentState ->
            canAttempt = !currentState.isPinLocked(nowMillis)
            if (canAttempt) {
                currentState.clearExpiredPinLockout(nowMillis)
            } else {
                currentState
            }
        }

        return canAttempt
    }

    override suspend fun registerPinFailure(nowMillis: Long): AuthProtectionState {
        return repository.updateAuthProtectionState { currentState ->
            if (currentState.isPinLocked(nowMillis)) {
                currentState
            } else {
                val nextFailedAttempts = currentState.pinFailedAttempts + 1
                val attemptsLimit = if (currentState.pinLockoutLevel == 0) {
                    InitialPinAttemptsLimit
                } else {
                    AttemptsLimitAfterLockout
                }
                if (nextFailedAttempts >= attemptsLimit) {
                    val nextLockoutLevel = currentState.pinLockoutLevel + 1
                    currentState.copy(
                        pinFailedAttempts = 0,
                        pinLockoutLevel = nextLockoutLevel,
                        pinLockedUntilMillis = nowMillis + nextLockoutDurationMillis(nextLockoutLevel),
                        isBiometricLocked = true
                    )
                } else {
                    currentState.copy(pinFailedAttempts = nextFailedAttempts)
                }
            }
        }
    }

    override suspend fun registerBiometricFailure(): AuthProtectionState {
        return repository.updateAuthProtectionState { currentState ->
            val nextFailedAttempts = currentState.biometricFailedAttempts + 1
            currentState.copy(
                biometricFailedAttempts = nextFailedAttempts,
                isBiometricLocked = nextFailedAttempts >= BiometricAttemptsLimit
            )
        }
    }

    override suspend fun registerSuccess() {
        repository.updateAuthProtectionState { AuthProtectionState() }
    }

    private fun AuthProtectionState.clearExpiredPinLockout(nowMillis: Long): AuthProtectionState {
        return if (pinLockedUntilMillis != null && !isPinLocked(nowMillis)) {
            copy(pinLockedUntilMillis = null)
        } else {
            this
        }
    }

    private fun nextLockoutDurationMillis(lockoutLevel: Int): Long {
        return if (lockoutLevel == 1) {
            InitialPinLockoutMillis
        } else {
            RepeatedPinLockoutMillis
        }
    }

    private companion object {
        const val InitialPinAttemptsLimit = 5
        const val AttemptsLimitAfterLockout = 1
        const val BiometricAttemptsLimit = 3
        const val InitialPinLockoutMillis = 30_000L
        const val RepeatedPinLockoutMillis = 60_000L
    }
}
