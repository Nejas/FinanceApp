package com.example.financeapp.presentation.main

import java.time.LocalDate

sealed interface MainIntent {
    data class DateSelected(val date: LocalDate) : MainIntent
    data object Retry : MainIntent
}
