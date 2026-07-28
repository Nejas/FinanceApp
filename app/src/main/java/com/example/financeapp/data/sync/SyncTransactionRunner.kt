package com.example.financeapp.data.sync

import androidx.room.withTransaction
import com.example.financeapp.data.local.db.FinanceDatabase
import javax.inject.Inject
import javax.inject.Singleton

interface SyncTransactionRunner {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}

@Singleton
class RoomSyncTransactionRunner @Inject constructor(
    private val database: FinanceDatabase
) : SyncTransactionRunner {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return database.withTransaction(block)
    }
}
