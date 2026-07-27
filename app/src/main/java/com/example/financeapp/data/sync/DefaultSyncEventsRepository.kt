package com.example.financeapp.data.sync

import com.example.financeapp.domain.model.SyncEvent
import com.example.financeapp.domain.repository.SyncEventsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface SyncEventPublisher {

    suspend fun publish(event: SyncEvent)
}

@Singleton
class DefaultSyncEventsRepository @Inject constructor() : SyncEventsRepository, SyncEventPublisher {

    private val mutableEvents = MutableSharedFlow<SyncEvent>(
        extraBufferCapacity = EVENT_BUFFER_CAPACITY
    )

    override val events: Flow<SyncEvent> = mutableEvents.asSharedFlow()

    override suspend fun publish(event: SyncEvent) {
        mutableEvents.emit(event)
    }

    private companion object {
        const val EVENT_BUFFER_CAPACITY = 8
    }
}
