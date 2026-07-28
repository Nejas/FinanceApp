package com.example.financeapp.data.sync

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class LastWriteWinsSyncConflictResolverTest {

    private val resolver = LastWriteWinsSyncConflictResolver()

    @Test
    fun resolve_returnsServerWinsWhenServerTimestampIsLater() {
        val result = resolver.resolve(
            localUpdatedAtEpochMillis = Instant.parse("2026-07-28T10:00:00Z").toEpochMilli(),
            serverUpdatedAt = "2026-07-28T10:01:00Z"
        )

        assertEquals(SyncConflictResolution.ServerWins, result)
    }

    @Test
    fun resolve_returnsLocalWinsWhenLocalTimestampIsLater() {
        val result = resolver.resolve(
            localUpdatedAtEpochMillis = Instant.parse("2026-07-28T10:02:00Z").toEpochMilli(),
            serverUpdatedAt = "2026-07-28T10:01:00Z"
        )

        assertEquals(SyncConflictResolution.LocalWins, result)
    }

    @Test
    fun resolve_returnsLocalWinsWhenTimestampsAreEqual() {
        val timestamp = Instant.parse("2026-07-28T10:00:00Z")

        val result = resolver.resolve(
            localUpdatedAtEpochMillis = timestamp.toEpochMilli(),
            serverUpdatedAt = timestamp.toString()
        )

        assertEquals(SyncConflictResolution.LocalWins, result)
    }
}
