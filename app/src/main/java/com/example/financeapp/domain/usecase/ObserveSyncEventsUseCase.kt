package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.SyncEvent
import com.example.financeapp.domain.repository.SyncEventsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSyncEventsUseCase @Inject constructor(
    private val repository: SyncEventsRepository
) {

    operator fun invoke(): Flow<SyncEvent> {
        return repository.events
    }
}
