package com.example.financeapp.presentation.analytics

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AnalyticsEffect {
    data object NavigateBack : AnalyticsEffect
}
