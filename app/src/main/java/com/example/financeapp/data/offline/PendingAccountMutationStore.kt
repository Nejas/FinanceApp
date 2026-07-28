package com.example.financeapp.data.offline

import androidx.room.withTransaction
import com.example.financeapp.data.local.LocalAccountIdGenerator
import com.example.financeapp.data.local.db.FinanceDatabase
import com.example.financeapp.data.local.db.dao.AccountDao
import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.entity.AccountSyncState
import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.data.local.mapper.toAccountSyncOperation
import com.example.financeapp.data.local.mapper.toDomain
import com.example.financeapp.data.local.mapper.toEntity
import com.example.financeapp.data.sync.SyncWorkScheduler
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.AccountDraft
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingAccountMutationStore @Inject constructor(
    private val database: FinanceDatabase,
    private val accountDao: AccountDao,
    private val syncOperationDao: SyncOperationDao,
    private val localIdGenerator: LocalAccountIdGenerator,
    private val syncWorkScheduler: SyncWorkScheduler,
    private val clock: Clock
) {

    suspend fun create(payload: AccountDraft): Account {
        val id = localIdGenerator.nextId()
        val now = clock.millis()
        val entity = payload.toEntity(
            id = id,
            createdAt = Instant.ofEpochMilli(now),
            syncState = AccountSyncState.PENDING
        )
        val operation = payload.toAccountSyncOperation(
            operationType = SyncOperationType.CREATE_ACCOUNT,
            accountId = id,
            serverAccountId = null,
            createdAtEpochMillis = now
        )
        database.withTransaction {
            accountDao.upsertAccount(entity)
            syncOperationDao.upsertOperation(operation)
        }
        syncWorkScheduler.enqueueOneTimeSync()
        return entity.toDomain()
    }

    suspend fun updatePending(id: Long, payload: AccountDraft): Account {
        val now = clock.millis()
        val entity = payload.toEntity(
            id = id,
            createdAt = accountDao.getAccount(id)?.createdAt?.let(Instant::parse)
                ?: Instant.ofEpochMilli(now),
            syncState = AccountSyncState.PENDING
        )
        val existingCreate = syncOperationDao.getCreateAccountOperation(id)
        val operation = payload.toAccountSyncOperation(
            operationType = SyncOperationType.CREATE_ACCOUNT,
            accountId = id,
            serverAccountId = null,
            createdAtEpochMillis = existingCreate?.createdAtEpochMillis ?: now
        ).withExistingId(existingCreate?.id)
        database.withTransaction {
            accountDao.upsertAccount(entity)
            syncOperationDao.upsertOperation(operation)
        }
        syncWorkScheduler.enqueueOneTimeSync()
        return entity.toDomain()
    }

    suspend fun queueUpdate(id: Long, payload: AccountDraft): Account {
        val now = clock.millis()
        val entity = payload.toEntity(
            id = id,
            createdAt = accountDao.getAccount(id)?.createdAt?.let(Instant::parse)
                ?: Instant.ofEpochMilli(now),
            syncState = AccountSyncState.PENDING
        )
        val operation = payload.toAccountSyncOperation(
            operationType = SyncOperationType.UPDATE_ACCOUNT,
            accountId = id,
            serverAccountId = id,
            createdAtEpochMillis = now
        )
        database.withTransaction {
            accountDao.upsertAccount(entity)
            syncOperationDao.deleteOperationsForAccount(id)
            syncOperationDao.upsertOperation(operation)
        }
        syncWorkScheduler.enqueueOneTimeSync()
        return entity.toDomain()
    }
}

private fun SyncOperationEntity.withExistingId(id: Long?): SyncOperationEntity {
    return if (id == null) this else copy(id = id)
}
