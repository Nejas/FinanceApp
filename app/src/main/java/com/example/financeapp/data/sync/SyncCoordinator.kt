package com.example.financeapp.data.sync

import androidx.work.ListenableWorker
import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.network.result.isRetryable
import com.example.financeapp.domain.model.SyncEvent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SyncCoordinator @Inject constructor(
    private val syncOperationDao: SyncOperationDao,
    private val transactionHandler: TransactionSyncOperationHandler,
    private val accountHandler: AccountSyncOperationHandler,
    private val snapshotRefresher: ServerSnapshotRefresher,
    private val syncEventPublisher: SyncEventPublisher,
) {

    private val syncMutex = Mutex()

    suspend fun sync(): ListenableWorker.Result {
        return syncMutex.withLock {
            syncInternal()
        }
    }

    private suspend fun syncInternal(): ListenableWorker.Result {
        var failedOperationsCount = 0
        for (operation in syncOperationDao.getPendingOperations()) {
            val result = syncOperation(operation)
            if (result == SyncOperationResult.RetryLater) {
                return ListenableWorker.Result.retry()
            }
            if (result == SyncOperationResult.Failed) {
                failedOperationsCount++
            }
        }

        return when (snapshotRefresher.refresh()) {
            SnapshotRefreshResult.Completed -> {
                syncEventPublisher.publish(SyncEvent.DataRefreshed)
                if (failedOperationsCount > 0) {
                    syncEventPublisher.publish(SyncEvent.OperationsFailed(failedOperationsCount))
                }
                ListenableWorker.Result.success()
            }
            SnapshotRefreshResult.RetryLater -> ListenableWorker.Result.retry()
        }
    }

    private suspend fun syncOperation(
        operation: SyncOperationEntity
    ): SyncOperationResult {
        val result = when (operation.type) {
            SyncOperationType.CREATE_TRANSACTION,
            SyncOperationType.UPDATE_TRANSACTION,
            SyncOperationType.DELETE_TRANSACTION -> transactionHandler.handle(operation)
            SyncOperationType.CREATE_ACCOUNT,
            SyncOperationType.UPDATE_ACCOUNT -> accountHandler.handle(operation)
        }

        if (result.isRetryable()) {
            syncOperationDao.recordRetryableError(
                id = operation.id,
                message = result.errorMessage()
            )
            return SyncOperationResult.RetryLater
        }

        if (result !is NetworkResult.Success) {
            syncOperationDao.markFailed(
                id = operation.id,
                message = result.errorMessage()
            )
            return SyncOperationResult.Failed
        }

        return SyncOperationResult.Done
    }

}

private enum class SyncOperationResult {
    Done,
    Failed,
    RetryLater
}
