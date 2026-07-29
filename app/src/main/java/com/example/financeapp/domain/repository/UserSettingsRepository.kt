package com.example.financeapp.domain.repository

import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setThemeMode(themeMode: AppThemeMode)
}
