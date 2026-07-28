package com.example.financeapp.data.sync

import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.data.network.result.NetworkResult
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSyncOperationHandlerTest {

    private val clock = Clock.fixed(
        Instant.parse("2026-07-28T12:00:00Z"),
        ZoneOffset.UTC
    )

    @Test
    fun update_replacesLocalTransactionWhenServerWinsConflict() = runTest {
        val remoteDataSource = FakeFinanceRemoteDataSource().apply {
            getTransactionResult = NetworkResult.Success(
                transactionResponse(
                    comment = "server wins",
                    updatedAt = "2026-07-28T10:01:00Z"
                )
            )
        }
        val transactionDao = FakeTransactionDao()
        val syncOperationDao = FakeSyncOperationDao()
        val handler = handler(
            remoteDataSource = remoteDataSource,
            transactionDao = transactionDao,
            syncOperationDao = syncOperationDao
        )

        val result = handler.handle(
            transactionOperation(
                operationType = SyncOperationType.UPDATE_TRANSACTION,
                createdAtEpochMillis = Instant.parse("2026-07-28T10:00:00Z").toEpochMilli()
            )
        )

        assertTrue(result is NetworkResult.Success)
        assertEquals(0, remoteDataSource.updateTransactionCalls)
        assertEquals("server wins", transactionDao.upsertedTransactions.single().comment)
        assertEquals(listOf(1L), syncOperationDao.deletedOperationIds)
    }

    @Test
    fun update_sendsLocalTransactionWhenLocalWinsConflict() = runTest {
        val remoteDataSource = FakeFinanceRemoteDataSource().apply {
            getTransactionResult = NetworkResult.Success(
                transactionResponse(updatedAt = "2026-07-28T10:00:00Z")
            )
            updateTransactionResult = NetworkResult.Success(
                transactionResponse(
                    comment = "local wins",
                    updatedAt = "2026-07-28T10:03:00Z"
                )
            )
        }
        val transactionDao = FakeTransactionDao()
        val syncOperationDao = FakeSyncOperationDao()
        val handler = handler(
            remoteDataSource = remoteDataSource,
            transactionDao = transactionDao,
            syncOperationDao = syncOperationDao
        )

        val result = handler.handle(
            transactionOperation(
                operationType = SyncOperationType.UPDATE_TRANSACTION,
                createdAtEpochMillis = Instant.parse("2026-07-28T10:02:00Z").toEpochMilli()
            )
        )

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, remoteDataSource.updateTransactionCalls)
        assertEquals("local wins", transactionDao.upsertedTransactions.single().comment)
        assertEquals(listOf(1L), syncOperationDao.deletedOperationIds)
    }

    @Test
    fun delete_restoresServerTransactionWhenServerWinsConflict() = runTest {
        val remoteDataSource = FakeFinanceRemoteDataSource().apply {
            getTransactionResult = NetworkResult.Success(
                transactionResponse(
                    comment = "server survives",
                    updatedAt = "2026-07-28T10:01:00Z"
                )
            )
        }
        val transactionDao = FakeTransactionDao()
        val syncOperationDao = FakeSyncOperationDao()
        val handler = handler(
            remoteDataSource = remoteDataSource,
            transactionDao = transactionDao,
            syncOperationDao = syncOperationDao
        )

        val result = handler.handle(
            transactionOperation(
                operationType = SyncOperationType.DELETE_TRANSACTION,
                createdAtEpochMillis = Instant.parse("2026-07-28T10:00:00Z").toEpochMilli()
            )
        )

        assertTrue(result is NetworkResult.Success)
        assertEquals(0, remoteDataSource.deleteTransactionCalls)
        assertEquals("server survives", transactionDao.upsertedTransactions.single().comment)
        assertTrue(transactionDao.deletedTransactionIds.isEmpty())
        assertEquals(listOf(1L), syncOperationDao.deletedOperationIds)
    }

    @Test
    fun delete_deletesServerTransactionWhenLocalWinsConflict() = runTest {
        val remoteDataSource = FakeFinanceRemoteDataSource().apply {
            getTransactionResult = NetworkResult.Success(
                transactionResponse(updatedAt = "2026-07-28T10:00:00Z")
            )
        }
        val transactionDao = FakeTransactionDao()
        val syncOperationDao = FakeSyncOperationDao()
        val handler = handler(
            remoteDataSource = remoteDataSource,
            transactionDao = transactionDao,
            syncOperationDao = syncOperationDao
        )

        val result = handler.handle(
            transactionOperation(
                operationType = SyncOperationType.DELETE_TRANSACTION,
                createdAtEpochMillis = Instant.parse("2026-07-28T10:02:00Z").toEpochMilli()
            )
        )

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, remoteDataSource.deleteTransactionCalls)
        assertEquals(listOf(10L), transactionDao.deletedTransactionIds)
        assertEquals(listOf(1L), syncOperationDao.deletedOperationIds)
    }

    @Test
    fun delete_clearsLocalOperationWhenServerTransactionIsAlreadyMissing() = runTest {
        val remoteDataSource = FakeFinanceRemoteDataSource().apply {
            getTransactionResult = NetworkResult.HttpError(
                code = 404,
                message = "Not Found",
                errorBody = null
            )
            deleteTransactionResult = NetworkResult.HttpError(
                code = 404,
                message = "Not Found",
                errorBody = null
            )
        }
        val transactionDao = FakeTransactionDao()
        val syncOperationDao = FakeSyncOperationDao()
        val handler = handler(
            remoteDataSource = remoteDataSource,
            transactionDao = transactionDao,
            syncOperationDao = syncOperationDao
        )

        val result = handler.handle(
            transactionOperation(
                operationType = SyncOperationType.DELETE_TRANSACTION,
                createdAtEpochMillis = Instant.parse("2026-07-28T10:02:00Z").toEpochMilli()
            )
        )

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, remoteDataSource.deleteTransactionCalls)
        assertEquals(listOf(10L), transactionDao.deletedTransactionIds)
        assertEquals(listOf(1L), syncOperationDao.deletedOperationIds)
    }

    private fun handler(
        remoteDataSource: FakeFinanceRemoteDataSource,
        transactionDao: FakeTransactionDao,
        syncOperationDao: FakeSyncOperationDao
    ): TransactionSyncOperationHandler {
        return TransactionSyncOperationHandler(
            networkDataSource = remoteDataSource,
            transactionRunner = ImmediateSyncTransactionRunner(),
            transactionDao = transactionDao,
            syncOperationDao = syncOperationDao,
            conflictResolver = LastWriteWinsSyncConflictResolver(),
            clock = clock
        )
    }
}
