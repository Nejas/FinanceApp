package com.example.financeapp.data.sync

import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.dao.TransactionDao
import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.mapper.toEntity
import com.example.financeapp.data.mapper.toMoney
import com.example.financeapp.data.mapper.toRequestDto
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource
import com.example.financeapp.domain.model.TransactionDraft
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionSyncOperationHandler @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource,
    private val transactionRunner: SyncTransactionRunner,
    private val transactionDao: TransactionDao,
    private val syncOperationDao: SyncOperationDao,
    private val conflictResolver: SyncConflictResolver,
    private val clock: Clock
) : SyncOperationHandler {

    override suspend fun handle(operation: SyncOperationEntity): NetworkResult<Unit> {
        return when (operation.type) {
            com.example.financeapp.data.local.db.entity.SyncOperationType.CREATE_TRANSACTION -> create(operation)
            com.example.financeapp.data.local.db.entity.SyncOperationType.UPDATE_TRANSACTION -> update(operation)
            com.example.financeapp.data.local.db.entity.SyncOperationType.DELETE_TRANSACTION -> delete(operation)
            else -> error("Unsupported transaction sync operation: ${operation.operationType}")
        }
    }

    private suspend fun create(operation: SyncOperationEntity): NetworkResult<Unit> {
        val createResult = networkDataSource.createTransaction(
            operation.toTransactionDraft().toRequestDto()
        )
        if (createResult !is NetworkResult.Success) {
            return createResult.asUnit()
        }

        val now = clock.millis()
        val serverEntity = when (val details = networkDataSource.getTransaction(createResult.data.id)) {
            is NetworkResult.Success -> details.data.toEntity(now)
            else -> createResult.data.toEntity(
                currencyCode = operation.currencyCode,
                updatedAtEpochMillis = now
            )
        }
        transactionRunner.runInTransaction {
            transactionDao.deleteTransaction(operation.localTransactionId)
            transactionDao.upsertTransaction(serverEntity)
            syncOperationDao.deleteOperation(operation.id)
        }
        return NetworkResult.Success(Unit)
    }

    private suspend fun update(operation: SyncOperationEntity): NetworkResult<Unit> {
        val serverId = operation.serverTransactionId ?: operation.localTransactionId
        val serverTransactionResult = networkDataSource.getTransaction(serverId)
        if (serverTransactionResult !is NetworkResult.Success) {
            return serverTransactionResult.asUnit()
        }
        if (
            conflictResolver.resolve(
                localUpdatedAtEpochMillis = operation.createdAtEpochMillis,
                serverUpdatedAt = serverTransactionResult.data.updatedAt
            ) == SyncConflictResolution.ServerWins
        ) {
            transactionRunner.runInTransaction {
                transactionDao.upsertTransaction(serverTransactionResult.data.toEntity(clock.millis()))
                syncOperationDao.deleteOperation(operation.id)
            }
            return NetworkResult.Success(Unit)
        }

        val updateResult = networkDataSource.updateTransaction(
            id = serverId,
            request = operation.toTransactionDraft().toRequestDto()
        )
        if (updateResult !is NetworkResult.Success) {
            return updateResult.asUnit()
        }
        transactionRunner.runInTransaction {
            transactionDao.upsertTransaction(updateResult.data.toEntity(clock.millis()))
            syncOperationDao.deleteOperation(operation.id)
        }
        return NetworkResult.Success(Unit)
    }

    private suspend fun delete(operation: SyncOperationEntity): NetworkResult<Unit> {
        val serverId = operation.serverTransactionId ?: operation.localTransactionId
        val serverTransactionResult = networkDataSource.getTransaction(serverId)
        if (serverTransactionResult is NetworkResult.Success) {
            if (
                conflictResolver.resolve(
                    localUpdatedAtEpochMillis = operation.createdAtEpochMillis,
                    serverUpdatedAt = serverTransactionResult.data.updatedAt
                ) == SyncConflictResolution.ServerWins
            ) {
                transactionRunner.runInTransaction {
                    transactionDao.upsertTransaction(serverTransactionResult.data.toEntity(clock.millis()))
                    syncOperationDao.deleteOperation(operation.id)
                }
                return NetworkResult.Success(Unit)
            }
        } else if (!serverTransactionResult.isNotFound()) {
            return serverTransactionResult.asUnit()
        }

        val deleteResult = networkDataSource.deleteTransaction(serverId)
        if (deleteResult !is NetworkResult.Success && !deleteResult.isNotFound()) {
            return deleteResult
        }
        transactionRunner.runInTransaction {
            transactionDao.deleteTransaction(operation.localTransactionId)
            syncOperationDao.deleteOperation(operation.id)
        }
        return NetworkResult.Success(Unit)
    }
}

private fun SyncOperationEntity.toTransactionDraft(): TransactionDraft {
    return TransactionDraft(
        accountId = accountId,
        categoryId = categoryId,
        amount = amount.toMoney(currencyCode),
        transactionDate = java.time.Instant.parse(transactionDate),
        comment = comment
    )
}
