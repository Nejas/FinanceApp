package com.example.financeapp.presentation.analytics

sealed interface AnalyticsEffect {
    data object NavigateBack : AnalyticsEffect
}
