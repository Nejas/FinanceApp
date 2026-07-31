package com.example.financeapp.domain.repository

import com.example.financeapp.domain.model.AuthProtectionState
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    val hasPinCode: Flow<Boolean>
    val authProtectionState: Flow<AuthProtectionState>

    suspend fun setPinCode(pinCode: String)

    suspend fun clearPinCode()

    suspend fun verifyPinCode(pinCode: String): Boolean

    suspend fun updateAuthProtectionState(
        transform: (AuthProtectionState) -> AuthProtectionState
    ): AuthProtectionState
}
