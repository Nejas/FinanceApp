package com.example.financeapp.data.sync

import com.example.financeapp.data.network.result.NetworkResult
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountSyncOperationHandlerTest {

    @Test
    fun update_replacesLocalAccountWhenServerWinsConflict() = runTest {
        val remoteDataSource = FakeFinanceRemoteDataSource().apply {
            getAccountResult = NetworkResult.Success(
                accountDetailsResponse(
                    name = "Server account",
                    updatedAt = "2026-07-28T10:01:00Z"
                )
            )
        }
        val accountDao = FakeAccountDao()
        val syncOperationDao = FakeSyncOperationDao()
        val handler = handler(
            remoteDataSource = remoteDataSource,
            accountDao = accountDao,
            syncOperationDao = syncOperationDao
        )

        val result = handler.handle(
            accountOperation(
                createdAtEpochMillis = Instant.parse("2026-07-28T10:00:00Z").toEpochMilli()
            )
        )

        assertTrue(result is NetworkResult.Success)
        assertEquals(0, remoteDataSource.updateAccountCalls)
        assertEquals("Server account", accountDao.upsertedAccounts.single().name)
        assertEquals(listOf(1L), syncOperationDao.deletedOperationIds)
    }

    @Test
    fun update_sendsLocalAccountWhenLocalWinsConflict() = runTest {
        val remoteDataSource = FakeFinanceRemoteDataSource().apply {
            getAccountResult = NetworkResult.Success(
                accountDetailsResponse(updatedAt = "2026-07-28T10:00:00Z")
            )
            updateAccountResult = NetworkResult.Success(
                accountResponse(
                    name = "Local account",
                    updatedAt = "2026-07-28T10:03:00Z"
                )
            )
        }
        val accountDao = FakeAccountDao()
        val syncOperationDao = FakeSyncOperationDao()
        val handler = handler(
            remoteDataSource = remoteDataSource,
            accountDao = accountDao,
            syncOperationDao = syncOperationDao
        )

        val result = handler.handle(
            accountOperation(
                createdAtEpochMillis = Instant.parse("2026-07-28T10:02:00Z").toEpochMilli()
            )
        )

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, remoteDataSource.updateAccountCalls)
        assertEquals("Local account", accountDao.upsertedAccounts.single().name)
        assertEquals(listOf(1L), syncOperationDao.deletedOperationIds)
    }

    @Test
    fun update_returnsServerReadErrorWithoutDeletingPendingOperation() = runTest {
        val remoteDataSource = FakeFinanceRemoteDataSource().apply {
            getAccountResult = NetworkResult.HttpError(
                code = 409,
                message = "Conflict",
                errorBody = null
            )
        }
        val accountDao = FakeAccountDao()
        val syncOperationDao = FakeSyncOperationDao()
        val handler = handler(
            remoteDataSource = remoteDataSource,
            accountDao = accountDao,
            syncOperationDao = syncOperationDao
        )

        val result = handler.handle(
            accountOperation(
                createdAtEpochMillis = Instant.parse("2026-07-28T10:02:00Z").toEpochMilli()
            )
        )

        assertTrue(result is NetworkResult.HttpError)
        assertEquals(0, remoteDataSource.updateAccountCalls)
        assertTrue(accountDao.upsertedAccounts.isEmpty())
        assertTrue(syncOperationDao.deletedOperationIds.isEmpty())
    }

    private fun handler(
        remoteDataSource: FakeFinanceRemoteDataSource,
        accountDao: FakeAccountDao,
        syncOperationDao: FakeSyncOperationDao
    ): AccountSyncOperationHandler {
        return AccountSyncOperationHandler(
            networkDataSource = remoteDataSource,
            transactionRunner = ImmediateSyncTransactionRunner(),
            accountDao = accountDao,
            transactionDao = FakeTransactionDao(),
            syncOperationDao = syncOperationDao,
            conflictResolver = LastWriteWinsSyncConflictResolver()
        )
    }
}
