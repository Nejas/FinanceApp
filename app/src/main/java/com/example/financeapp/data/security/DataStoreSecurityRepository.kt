package com.example.financeapp.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.financeapp.domain.model.AuthProtectionState
import com.example.financeapp.domain.repository.SecurityRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "security_settings"
)

class DataStoreSecurityRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SecurityRepository {

    private val securityPreferences: Flow<Preferences> = context.securityDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }

    override val hasPinCode: Flow<Boolean> = securityPreferences
        .map { preferences ->
            preferences[SecurityKeys.PinSalt] != null &&
                preferences[SecurityKeys.PinVerifier] != null
        }

    override val authProtectionState: Flow<AuthProtectionState> = securityPreferences
        .map { preferences ->
            preferences.toAuthProtectionState()
        }

    override suspend fun setPinCode(pinCode: String) {
        val salt = ByteArray(PinSaltLengthBytes).also(SecureRandom()::nextBytes)
        val verifier = createPinVerifier(pinCode = pinCode, salt = salt)

        context.securityDataStore.edit { preferences ->
            preferences[SecurityKeys.PinSalt] = salt.encodeBase64()
            preferences[SecurityKeys.PinVerifier] = verifier.encodeBase64()
        }
    }

    override suspend fun clearPinCode() {
        context.securityDataStore.edit { preferences ->
            preferences.remove(SecurityKeys.PinSalt)
            preferences.remove(SecurityKeys.PinVerifier)
            preferences.remove(SecurityKeys.PinFailedAttempts)
            preferences.remove(SecurityKeys.PinLockoutLevel)
            preferences.remove(SecurityKeys.PinLockedUntilMillis)
            preferences.remove(SecurityKeys.BiometricFailedAttempts)
            preferences.remove(SecurityKeys.IsBiometricLocked)
        }
    }

    override suspend fun verifyPinCode(pinCode: String): Boolean {
        val preferences = securityPreferences.first()
        val salt = preferences[SecurityKeys.PinSalt]?.decodeBase64()
        val verifier = preferences[SecurityKeys.PinVerifier]?.decodeBase64()

        return if (salt == null || verifier == null) {
            false
        } else {
            MessageDigest.isEqual(
                createPinVerifier(pinCode = pinCode, salt = salt),
                verifier
            )
        }
    }

    override suspend fun updateAuthProtectionState(
        transform: (AuthProtectionState) -> AuthProtectionState
    ): AuthProtectionState {
        var updatedState = AuthProtectionState()

        context.securityDataStore.edit { preferences ->
            val currentState = preferences.toAuthProtectionState()
            updatedState = transform(currentState)
            if (updatedState != currentState) {
                preferences.writeAuthProtectionState(updatedState)
            }
        }

        return updatedState
    }

    private fun createPinVerifier(pinCode: String, salt: ByteArray): ByteArray {
        val mac = Mac.getInstance(PinMacAlgorithm)
        mac.init(getOrCreatePinKey())
        mac.update(salt)
        return mac.doFinal(pinCode.toByteArray(Charsets.UTF_8))
    }

    private fun getOrCreatePinKey(): SecretKey {
        val keyStore = KeyStore.getInstance(AndroidKeyStore).apply {
            load(null)
        }
        val existingKey = keyStore.getKey(PinKeyAlias, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
            AndroidKeyStore
        )
        val keySpec = KeyGenParameterSpec.Builder(
            PinKeyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private fun ByteArray.encodeBase64(): String {
        return Base64.encodeToString(this, Base64.NO_WRAP)
    }

    private fun String.decodeBase64(): ByteArray {
        return Base64.decode(this, Base64.NO_WRAP)
    }

    private fun Preferences.toAuthProtectionState(): AuthProtectionState {
        return AuthProtectionState(
            pinFailedAttempts = this[SecurityKeys.PinFailedAttempts] ?: 0,
            pinLockoutLevel = this[SecurityKeys.PinLockoutLevel] ?: 0,
            pinLockedUntilMillis = this[SecurityKeys.PinLockedUntilMillis],
            biometricFailedAttempts = this[SecurityKeys.BiometricFailedAttempts] ?: 0,
            isBiometricLocked = this[SecurityKeys.IsBiometricLocked] ?: false
        )
    }

    private fun MutablePreferences.writeAuthProtectionState(state: AuthProtectionState) {
        this[SecurityKeys.PinFailedAttempts] = state.pinFailedAttempts
        this[SecurityKeys.PinLockoutLevel] = state.pinLockoutLevel
        state.pinLockedUntilMillis?.let { lockedUntilMillis ->
            this[SecurityKeys.PinLockedUntilMillis] = lockedUntilMillis
        } ?: remove(SecurityKeys.PinLockedUntilMillis)
        this[SecurityKeys.BiometricFailedAttempts] = state.biometricFailedAttempts
        this[SecurityKeys.IsBiometricLocked] = state.isBiometricLocked
    }

    private object SecurityKeys {
        val PinSalt = stringPreferencesKey("pin_salt")
        val PinVerifier = stringPreferencesKey("pin_verifier")
        val PinFailedAttempts = intPreferencesKey("pin_failed_attempts")
        val PinLockoutLevel = intPreferencesKey("pin_lockout_level")
        val PinLockedUntilMillis = longPreferencesKey("pin_locked_until_millis")
        val BiometricFailedAttempts = intPreferencesKey("biometric_failed_attempts")
        val IsBiometricLocked = booleanPreferencesKey("is_biometric_locked")
    }

    private companion object {
        const val AndroidKeyStore = "AndroidKeyStore"
        const val PinKeyAlias = "finance_app_pin_hmac_key"
        const val PinMacAlgorithm = "HmacSHA256"
        const val PinSaltLengthBytes = 16
    }
}
