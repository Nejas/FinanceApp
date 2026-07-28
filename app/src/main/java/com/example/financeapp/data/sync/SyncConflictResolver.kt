package com.example.financeapp.data.sync

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SyncConflictResolution {
    data object LocalWins : SyncConflictResolution
    data object ServerWins : SyncConflictResolution
}

interface SyncConflictResolver {
    fun resolve(
        localUpdatedAtEpochMillis: Long,
        serverUpdatedAt: String
    ): SyncConflictResolution
}

@Singleton
class LastWriteWinsSyncConflictResolver @Inject constructor() : SyncConflictResolver {

    override fun resolve(
        localUpdatedAtEpochMillis: Long,
        serverUpdatedAt: String
    ): SyncConflictResolution {
        val serverUpdatedAtEpochMillis = Instant.parse(serverUpdatedAt).toEpochMilli()
        return if (serverUpdatedAtEpochMillis > localUpdatedAtEpochMillis) {
            SyncConflictResolution.ServerWins
        } else {
            SyncConflictResolution.LocalWins
        }
    }
}
