package com.example.financeapp.presentation.main

import com.example.financeapp.presentation.common.placeholders.ScreenError
import java.time.LocalDate

sealed interface MainIntent {
    data class DateSelected(val date: LocalDate) : MainIntent
    data class DeleteTransaction(val transactionId: Long) : MainIntent
    data class DeleteFinancialAccount(val accountId: Long) : MainIntent
    data object RetryFailedSyncOperations : MainIntent
    data object DiscardFailedSyncOperations : MainIntent
    data object DataChanged : MainIntent
    data object Retry : MainIntent
}

sealed interface MainEffect {
    data class DeleteFailed(val error: ScreenError) : MainEffect
    data class SyncFailed(val count: Int) : MainEffect
}
