package com.example.financeapp.domain.repository

interface SyncOperationsRepository {

    suspend fun retryFailedOperations(): Result<Unit>

    suspend fun discardFailedOperations(): Result<Unit>
}
