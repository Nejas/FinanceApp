package com.example.financeapp.domain.usecase

import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.domain.repository.UserSettingsRepository
import javax.inject.Inject

class UserSettingsUseCases @Inject constructor(
    private val repository: UserSettingsRepository
) {
    val settings = repository.settings

    suspend fun setThemeMode(themeMode: AppThemeMode) {
        repository.setThemeMode(themeMode)
    }
}
