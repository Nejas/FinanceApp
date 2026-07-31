package com.example.financeapp.domain.model

data class AuthProtectionState(
    val pinFailedAttempts: Int = 0,
    val pinLockoutLevel: Int = 0,
    val pinLockedUntilMillis: Long? = null,
    val biometricFailedAttempts: Int = 0,
    val isBiometricLocked: Boolean = false
) {
    fun isPinLocked(nowMillis: Long): Boolean {
        val lockedUntil = pinLockedUntilMillis ?: return false
        return lockedUntil > nowMillis
    }

    fun pinRetryAfterSeconds(nowMillis: Long): Long {
        val lockedUntil = pinLockedUntilMillis ?: return 0L
        val remainingMillis = lockedUntil - nowMillis
        return if (remainingMillis <= 0L) {
            0L
        } else {
            (remainingMillis + MillisInSecond - 1L) / MillisInSecond
        }
    }

    private companion object {
        const val MillisInSecond = 1_000L
    }
}
