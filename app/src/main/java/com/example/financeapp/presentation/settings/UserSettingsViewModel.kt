package com.example.financeapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.core.theme.AppThemeMode
import com.example.financeapp.domain.model.UserSettings
import com.example.financeapp.domain.usecase.UserSettingsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val useCases: UserSettingsUseCases
) : ViewModel() {

    val settings: StateFlow<UserSettings> = useCases.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
        initialValue = UserSettings(
            themeMode = AppThemeMode.SYSTEM
        )
    )

    fun setThemeMode(themeMode: AppThemeMode) {
        viewModelScope.launch {
            useCases.setThemeMode(themeMode)
        }
    }

    private companion object {
        const val StopTimeoutMillis = 5_000L
    }
}
