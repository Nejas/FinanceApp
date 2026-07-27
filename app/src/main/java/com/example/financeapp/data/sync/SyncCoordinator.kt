package com.example.financeapp.data.sync

import androidx.room.withTransaction
import androidx.work.ListenableWorker
import com.example.financeapp.data.local.TransactionPeriodResolver
import com.example.financeapp.data.local.db.FinanceDatabase
import com.example.financeapp.data.local.db.dao.AccountDao
import com.example.financeapp.data.local.db.dao.CategoryDao
import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.dao.TransactionDao
import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.data.local.mapper.toEntity
import com.example.financeapp.data.mapper.toCreateRequestDto
import com.example.financeapp.data.mapper.toMoney
import com.example.financeapp.data.mapper.toRequestDto
import com.example.financeapp.data.mapper.toUpdateRequestDto
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.network.result.isRetryable
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource
import com.example.financeapp.domain.model.SyncEvent
import com.example.financeapp.domain.model.AccountDraft
import com.example.financeapp.domain.model.TransactionDraft
import com.example.financeapp.domain.model.TransactionsQuery
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SyncCoordinator @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource,
    private val database: FinanceDatabase,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val syncOperationDao: SyncOperationDao,
    private val periodResolver: TransactionPeriodResolver,
    private val syncEventPublisher: SyncEventPublisher,
    private val clock: Clock
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

        return when (refreshCurrentServerSnapshot()) {
            RefreshResult.Success -> {
                syncEventPublisher.publish(SyncEvent.DataRefreshed)
                if (failedOperationsCount > 0) {
                    syncEventPublisher.publish(SyncEvent.OperationsFailed(failedOperationsCount))
                }
                ListenableWorker.Result.success()
            }
            RefreshResult.RetryLater -> ListenableWorker.Result.retry()
        }
    }

    private suspend fun syncOperation(
        operation: SyncOperationEntity
    ): SyncOperationResult {
        val result = when (operation.type) {
            SyncOperationType.CREATE_TRANSACTION -> syncCreate(operation)
            SyncOperationType.UPDATE_TRANSACTION -> syncUpdate(operation)
            SyncOperationType.DELETE_TRANSACTION -> syncDelete(operation)
            SyncOperationType.CREATE_ACCOUNT -> syncCreateAccount(operation)
            SyncOperationType.UPDATE_ACCOUNT -> syncUpdateAccount(operation)
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

    private suspend fun syncCreate(
        operation: SyncOperationEntity
    ): NetworkResult<Unit> {
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

        database.withTransaction {
            transactionDao.deleteTransaction(operation.localTransactionId)
            transactionDao.upsertTransaction(serverEntity)
            syncOperationDao.deleteOperation(operation.id)
        }
        return NetworkResult.Success(Unit)
    }

    private suspend fun syncUpdate(
        operation: SyncOperationEntity
    ): NetworkResult<Unit> {
        val serverId = operation.serverTransactionId ?: operation.localTransactionId
        val updateResult = networkDataSource.updateTransaction(
            id = serverId,
            request = operation.toTransactionDraft().toRequestDto()
        )
        if (updateResult !is NetworkResult.Success) {
            return updateResult.asUnit()
        }

        database.withTransaction {
            transactionDao.upsertTransaction(updateResult.data.toEntity(clock.millis()))
            syncOperationDao.deleteOperation(operation.id)
        }
        return NetworkResult.Success(Unit)
    }

    private suspend fun syncDelete(
        operation: SyncOperationEntity
    ): NetworkResult<Unit> {
        val serverId = operation.serverTransactionId ?: operation.localTransactionId
        val deleteResult = networkDataSource.deleteTransaction(serverId)
        if (deleteResult !is NetworkResult.Success) {
            return deleteResult
        }

        database.withTransaction {
            transactionDao.deleteTransaction(operation.localTransactionId)
            syncOperationDao.deleteOperation(operation.id)
        }
        return NetworkResult.Success(Unit)
    }

    private suspend fun syncCreateAccount(
        operation: SyncOperationEntity
    ): NetworkResult<Unit> {
        val createResult = networkDataSource.createAccount(
            operation.toAccountDraft().toCreateRequestDto()
        )
        if (createResult !is NetworkResult.Success) {
            return createResult.asUnit()
        }

        database.withTransaction {
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

    private suspend fun syncUpdateAccount(
        operation: SyncOperationEntity
    ): NetworkResult<Unit> {
        val serverId = operation.serverAccountId ?: operation.accountId
        val updateResult = networkDataSource.updateAccount(
            id = serverId,
            request = operation.toAccountDraft().toUpdateRequestDto()
        )
        if (updateResult !is NetworkResult.Success) {
            return updateResult.asUnit()
        }

        database.withTransaction {
            accountDao.upsertAccount(updateResult.data.toEntity())
            syncOperationDao.deleteOperation(operation.id)
        }
        return NetworkResult.Success(Unit)
    }

    private suspend fun refreshCurrentServerSnapshot(): RefreshResult {
        val accountsResult = networkDataSource.getAccounts()
        if (accountsResult.isRetryable()) {
            return RefreshResult.RetryLater
        }
        val accounts = (accountsResult as? NetworkResult.Success)?.data ?: return RefreshResult.Success
        accountDao.replaceSyncedAccounts(accounts.map { account -> account.toEntity() })

        val categoriesResult = networkDataSource.getCategories()
        if (categoriesResult.isRetryable()) {
            return RefreshResult.RetryLater
        }
        (categoriesResult as? NetworkResult.Success)?.let { result ->
            categoryDao.upsertCategories(result.data.map { category -> category.toEntity() })
        }

        val period = periodResolver.resolve(
            TransactionsQuery(accountIds = accounts.mapTo(mutableSetOf()) { account -> account.id })
        )
        for (account in accounts) {
            val transactionsResult = networkDataSource.getTransactionsByPeriod(
                accountId = account.id,
                startDate = period.startDate.toString(),
                endDate = period.endDate.toString()
            )
            if (transactionsResult.isRetryable()) {
                return RefreshResult.RetryLater
            }
            if (transactionsResult is NetworkResult.Success) {
                transactionDao.replaceSyncedTransactionsForPeriod(
                    accountId = account.id,
                    startEpochMillis = period.startEpochMillis,
                    endEpochMillis = period.endEpochMillis,
                    transactions = transactionsResult.data.map { transaction ->
                        transaction.toEntity(clock.millis())
                    }
                )
            }
        }

        return RefreshResult.Success
    }

    private val SyncOperationEntity.type: SyncOperationType
        get() = SyncOperationType.valueOf(operationType)
}

private enum class SyncOperationResult {
    Done,
    Failed,
    RetryLater
}

private enum class RefreshResult {
    Success,
    RetryLater
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

private fun SyncOperationEntity.toAccountDraft(): AccountDraft {
    return AccountDraft(
        name = requireNotNull(accountName),
        emoji = accountEmoji,
        balance = requireNotNull(accountBalance).toMoney(
            requireNotNull(accountCurrencyCode)
        )
    )
}

private fun NetworkResult<*>.errorMessage(): String {
    return when (this) {
        is NetworkResult.Success -> "success"
        is NetworkResult.HttpError -> "HTTP $code ${message.orEmpty()}".trim()
        is NetworkResult.NetworkError -> throwable.message ?: "Network error"
        is NetworkResult.TimeoutError -> throwable.message ?: "Timeout"
        is NetworkResult.SerializationError -> throwable.message ?: "Serialization error"
        is NetworkResult.UnknownError -> throwable.message ?: "Unknown error"
    }
}

private fun NetworkResult<*>.asUnit(): NetworkResult<Unit> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.HttpError -> this
        is NetworkResult.NetworkError -> this
        is NetworkResult.TimeoutError -> this
        is NetworkResult.SerializationError -> this
        is NetworkResult.UnknownError -> this
    }
}
