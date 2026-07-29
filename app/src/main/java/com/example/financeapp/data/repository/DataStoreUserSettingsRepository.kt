package com.example.financeapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.domain.model.UserSettings
import com.example.financeapp.domain.repository.UserSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings"
)

class DataStoreUserSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : UserSettingsRepository {

    override val settings: Flow<UserSettings> = context.userSettingsDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            UserSettings(
                themeMode = preferences[UserSettingsKeys.ThemeMode]
                    ?.let(AppThemeMode::fromName)
                    ?: AppThemeMode.SYSTEM
            )
        }

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[UserSettingsKeys.ThemeMode] = themeMode.name
        }
    }

    private object UserSettingsKeys {
        val ThemeMode = stringPreferencesKey("theme_mode")
    }
}
