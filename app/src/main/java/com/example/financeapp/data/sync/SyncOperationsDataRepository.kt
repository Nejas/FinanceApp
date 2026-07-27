package com.example.financeapp.data.sync

import androidx.room.withTransaction
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.data.local.db.FinanceDatabase
import com.example.financeapp.data.local.db.dao.AccountDao
import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.dao.TransactionDao
import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.domain.repository.SyncOperationsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncOperationsDataRepository @Inject constructor(
    private val database: FinanceDatabase,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val syncOperationDao: SyncOperationDao,
    private val syncWorkScheduler: SyncWorkScheduler
) : SyncOperationsRepository {

    override suspend fun retryFailedOperations(): Result<Unit> = suspendRunCatching {
        syncOperationDao.retryFailedOperations()
        syncWorkScheduler.enqueueOneTimeSync()
    }

    override suspend fun discardFailedOperations(): Result<Unit> = suspendRunCatching {
        database.withTransaction {
            syncOperationDao.getFailedOperations().forEach { operation ->
                when (SyncOperationType.valueOf(operation.operationType)) {
                    SyncOperationType.CREATE_TRANSACTION,
                    SyncOperationType.UPDATE_TRANSACTION,
                    SyncOperationType.DELETE_TRANSACTION -> transactionDao.markSynced(
                        operation.localTransactionId
                    )
                    SyncOperationType.CREATE_ACCOUNT,
                    SyncOperationType.UPDATE_ACCOUNT -> accountDao.markSynced(operation.accountId)
                }
            }
            syncOperationDao.deleteFailedOperations()
        }
    }
}
