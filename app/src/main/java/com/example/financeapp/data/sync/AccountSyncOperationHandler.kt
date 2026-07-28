package com.example.financeapp.data.sync

import com.example.financeapp.data.local.db.dao.AccountDao
import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.dao.TransactionDao
import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.mapper.toEntity
import com.example.financeapp.data.mapper.toCreateRequestDto
import com.example.financeapp.data.mapper.toMoney
import com.example.financeapp.data.mapper.toUpdateRequestDto
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource
import com.example.financeapp.domain.model.AccountDraft
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountSyncOperationHandler @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource,
    private val transactionRunner: SyncTransactionRunner,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val syncOperationDao: SyncOperationDao,
    private val conflictResolver: SyncConflictResolver
) : SyncOperationHandler {

    override suspend fun handle(operation: SyncOperationEntity): NetworkResult<Unit> {
        return when (operation.type) {
            com.example.financeapp.data.local.db.entity.SyncOperationType.CREATE_ACCOUNT -> create(operation)
            com.example.financeapp.data.local.db.entity.SyncOperationType.UPDATE_ACCOUNT -> update(operation)
            else -> error("Unsupported account sync operation: ${operation.operationType}")
        }
    }

    private suspend fun create(operation: SyncOperationEntity): NetworkResult<Unit> {
        val createResult = networkDataSource.createAccount(
            operation.toAccountDraft().toCreateRequestDto()
        )
        if (createResult !is NetworkResult.Success) {
            return createResult.asUnit()
        }
        transactionRunner.runInTransaction {
            accountDao.deleteAccount(operation.accountId)
            accountDao.upsertAccount(createResult.data.toEntity())
            transactionDao.replaceAccountId(
                oldAccountId = operation.accountId,
                newAccountId = createResult.data.id
            )
            syncOperationDao.replaceTransactionAccountId(
                oldAccountId = operation.accountId,
                newAccountId = createResult.data.id
            )
            syncOperationDao.deleteOperation(operation.id)
        }
        return NetworkResult.Success(Unit)
    }

    private suspend fun update(operation: SyncOperationEntity): NetworkResult<Unit> {
        val serverId = operation.serverAccountId ?: operation.accountId
        val serverAccountResult = networkDataSource.getAccount(serverId)
        if (serverAccountResult !is NetworkResult.Success) {
            return serverAccountResult.asUnit()
        }
        if (
            conflictResolver.resolve(
                localUpdatedAtEpochMillis = operation.createdAtEpochMillis,
                serverUpdatedAt = serverAccountResult.data.updatedAt
            ) == SyncConflictResolution.ServerWins
        ) {
            transactionRunner.runInTransaction {
                accountDao.upsertAccount(serverAccountResult.data.toEntity())
                syncOperationDao.deleteOperation(operation.id)
            }
            return NetworkResult.Success(Unit)
        }

        val updateResult = networkDataSource.updateAccount(
            id = serverId,
            request = operation.toAccountDraft().toUpdateRequestDto()
        )
        if (updateResult !is NetworkResult.Success) {
            return updateResult.asUnit()
        }
        transactionRunner.runInTransaction {
            accountDao.upsertAccount(updateResult.data.toEntity())
            syncOperationDao.deleteOperation(operation.id)
        }
        return NetworkResult.Success(Unit)
    }
}

private fun SyncOperationEntity.toAccountDraft(): AccountDraft {
    return AccountDraft(
        name = requireNotNull(accountName),
        emoji = accountEmoji,
        balance = requireNotNull(accountBalance).toMoney(
            requireNotNull(accountCurrencyCode)
        )
    )
}
