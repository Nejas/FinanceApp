package com.example.financeapp.domain.repository

import com.example.financeapp.domain.model.SyncEvent
import kotlinx.coroutines.flow.Flow

interface SyncEventsRepository {

    val events: Flow<SyncEvent>
}
