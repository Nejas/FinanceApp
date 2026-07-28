package com.example.financeapp.data.offline

import androidx.room.withTransaction
import com.example.financeapp.data.local.LocalTransactionIdGenerator
import com.example.financeapp.data.local.db.FinanceDatabase
import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.dao.TransactionDao
import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.data.local.db.entity.TransactionEntity
import com.example.financeapp.data.local.db.entity.TransactionSyncState
import com.example.financeapp.data.local.mapper.toDomain
import com.example.financeapp.data.local.mapper.toEntity
import com.example.financeapp.data.local.mapper.toSyncOperation
import com.example.financeapp.data.sync.SyncWorkScheduler
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionDraft
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingTransactionMutationStore @Inject constructor(
    private val database: FinanceDatabase,
    private val transactionDao: TransactionDao,
    private val syncOperationDao: SyncOperationDao,
    private val localIdGenerator: LocalTransactionIdGenerator,
    private val syncWorkScheduler: SyncWorkScheduler,
    private val clock: Clock
) {

    suspend fun create(payload: TransactionDraft): Transaction {
        val id = localIdGenerator.nextId()
        val now = clock.millis()
        val entity = payload.toEntity(
            id = id,
            syncState = TransactionSyncState.PENDING,
            updatedAtEpochMillis = now
        )
        val operation = payload.toSyncOperation(
            operationType = SyncOperationType.CREATE_TRANSACTION,
            localTransactionId = id,
            serverTransactionId = null,
            createdAtEpochMillis = now
        )
        database.withTransaction {
            transactionDao.upsertTransaction(entity)
            syncOperationDao.upsertOperation(operation)
        }
        syncWorkScheduler.enqueueOneTimeSync()
        return entity.toDomain()
    }

    suspend fun updatePending(id: Long, payload: TransactionDraft): Transaction {
        val now = clock.millis()
        val entity = payload.toEntity(
            id = id,
            syncState = TransactionSyncState.PENDING,
            updatedAtEpochMillis = now
        )
        val pendingCreate = syncOperationDao.getCreateTransactionOperation(id)
        val operation = payload.toSyncOperation(
            operationType = SyncOperationType.CREATE_TRANSACTION,
            localTransactionId = id,
            serverTransactionId = null,
            createdAtEpochMillis = pendingCreate?.createdAtEpochMillis ?: now
        ).withExistingId(pendingCreate?.id)
        database.withTransaction {
            transactionDao.upsertTransaction(entity)
            syncOperationDao.upsertOperation(operation)
        }
        syncWorkScheduler.enqueueOneTimeSync()
        return entity.toDomain()
    }

    suspend fun queueUpdate(id: Long, payload: TransactionDraft): Transaction {
        val now = clock.millis()
        val entity = payload.toEntity(
            id = id,
            syncState = TransactionSyncState.PENDING,
            updatedAtEpochMillis = now
        )
        val operation = payload.toSyncOperation(
            operationType = SyncOperationType.UPDATE_TRANSACTION,
            localTransactionId = id,
            serverTransactionId = id,
            createdAtEpochMillis = now
        )
        database.withTransaction {
            transactionDao.upsertTransaction(entity)
            syncOperationDao.deleteOperationsForTransaction(id)
            syncOperationDao.upsertOperation(operation)
        }
        syncWorkScheduler.enqueueOneTimeSync()
        return entity.toDomain()
    }

    suspend fun discardLocal(id: Long) {
        database.withTransaction {
            transactionDao.deleteTransaction(id)
            syncOperationDao.deletePendingOperationsForTransaction(id)
        }
    }

    suspend fun queueDelete(id: Long): Result<Unit> {
        val cachedTransaction = transactionDao.getTransaction(id)
            ?: return Result.failure(NoSuchElementException("Cached transaction not found: id=$id"))
        val now = clock.millis()
        database.withTransaction {
            transactionDao.markDeletedPendingSync(
                id = id,
                updatedAtEpochMillis = now
            )
            syncOperationDao.deleteOperationsForTransaction(id)
            syncOperationDao.upsertOperation(cachedTransaction.toDeleteOperation(now))
        }
        syncWorkScheduler.enqueueOneTimeSync()
        return Result.success(Unit)
    }
}

private fun SyncOperationEntity.withExistingId(id: Long?): SyncOperationEntity {
    return if (id == null) this else copy(id = id)
}

private fun TransactionEntity.toDeleteOperation(createdAtEpochMillis: Long): SyncOperationEntity {
    return SyncOperationEntity(
        operationType = SyncOperationType.DELETE_TRANSACTION.name,
        localTransactionId = id,
        serverTransactionId = id,
        accountId = accountId,
        categoryId = categoryId,
        amount = amount,
        currencyCode = currencyCode,
        transactionDate = transactionDate,
        transactionDateEpochMillis = transactionDateEpochMillis,
        comment = comment,
        createdAtEpochMillis = createdAtEpochMillis
    )
}
