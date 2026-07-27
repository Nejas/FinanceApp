package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.SyncEvent
import com.example.financeapp.domain.repository.SyncEventsRepository
import com.example.financeapp.domain.repository.SyncOperationsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SynchronizationUseCases @Inject constructor(
    private val syncEventsRepository: SyncEventsRepository,
    private val syncOperationsRepository: SyncOperationsRepository
) {

    fun observeEvents(): Flow<SyncEvent> = syncEventsRepository.events

    suspend fun retryFailedOperations(): Result<Unit> =
        syncOperationsRepository.retryFailedOperations()

    suspend fun discardFailedOperations(): Result<Unit> =
        syncOperationsRepository.discardFailedOperations()
}
