package com.example.financeapp.domain.model

sealed interface SyncEvent {
    data object DataRefreshed : SyncEvent
    data class OperationsFailed(val count: Int) : SyncEvent
}
