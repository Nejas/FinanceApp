package com.example.financeapp.presentation.main

import java.time.LocalDate

sealed interface MainIntent {
    data class DateSelected(val date: LocalDate) : MainIntent
    data class DeleteTransaction(val transactionId: Long) : MainIntent
    data class DeleteFinancialAccount(val accountId: Long) : MainIntent
    data object DataChanged : MainIntent
    data object Retry : MainIntent
}

sealed interface MainEffect {
    data object DeleteFailed : MainEffect
}
