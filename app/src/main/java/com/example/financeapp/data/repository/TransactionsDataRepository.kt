package com.example.financeapp.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.data.local.LocalTransactionIdGenerator
import com.example.financeapp.data.local.TransactionPeriodResolver
import com.example.financeapp.data.local.db.FinanceDatabase
import com.example.financeapp.data.local.db.dao.CategoryDao
import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.dao.TransactionDao
import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.data.local.db.entity.TransactionEntity
import com.example.financeapp.data.local.db.entity.TransactionSyncState
import com.example.financeapp.data.local.mapper.toDomain as localToDomain
import com.example.financeapp.data.local.mapper.toEntity
import com.example.financeapp.data.local.mapper.toSyncOperation
import com.example.financeapp.data.mapper.toDomain
import com.example.financeapp.data.mapper.toRequestDto
import com.example.financeapp.data.network.model.response.TransactionResponseDto
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource
import com.example.financeapp.data.sync.SyncWorkScheduler
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionDraft
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.repository.TransactionsRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class TransactionsDataRepository @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource,
    private val database: FinanceDatabase,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val syncOperationDao: SyncOperationDao,
    private val localIdGenerator: LocalTransactionIdGenerator,
    private val periodResolver: TransactionPeriodResolver,
    private val syncWorkScheduler: SyncWorkScheduler,
    private val networkMonitor: NetworkMonitor,
    private val clock: Clock,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : TransactionsRepository {

    override suspend fun getTransactions(
        query: TransactionsQuery
    ): Result<List<Transaction>> {
        if (query.accountIds.isEmpty()) {
            return Result.success(emptyList())
        }

        Log.d(TAG, "Loading transactions: query=$query")
        val period = periodResolver.resolve(query)
        val loadedTransactions = mutableListOf<Transaction>()
        var failedResult: Result<List<Transaction>>? = null

        for (accountId in query.accountIds) {
            if (accountId < 0) {
                loadedTransactions += transactionDao.getTransactionsByPeriod(
                    accountIds = setOf(accountId),
                    startEpochMillis = period.startEpochMillis,
                    endEpochMillis = period.endEpochMillis
                )
                    .filterByType(query.type)
                    .map { transaction -> transaction.localToDomain() }
                continue
            }

            val result = networkDataSource.getTransactionsByPeriod(
                accountId = accountId,
                startDate = period.startDate.toString(),
                endDate = period.endDate.toString()
            )

            if (result is NetworkResult.Success) {
                val now = clock.millis()
                val entities = result.data.map { transaction -> transaction.toEntity(now) }
                transactionDao.replaceSyncedTransactionsForPeriod(
                    accountId = accountId,
                    startEpochMillis = period.startEpochMillis,
                    endEpochMillis = period.endEpochMillis,
                    transactions = entities
                )
                loadedTransactions += result.data
                    .filter { transaction -> transaction.matchesType(query.type) }
                    .map { transaction -> transaction.toDomain() }
            } else {
                failedResult = result.mapToResult(defaultDispatcher) { emptyList<Transaction>() }
                break
            }
        }

        failedResult?.let { failure ->
            val error = requireNotNull(failure.exceptionOrNull())
            Log.e(TAG, "Failed to load transactions from network: query=$query", error)
            if (!error.canReadFromLocalCache(networkMonitor.isOnline.value)) {
                return failure
            }
            return loadCachedTransactions(query)
        }

        return Result.success(
            mergePendingTransactions(
                query = query,
                serverTransactions = loadedTransactions
            )
        )
    }

    override suspend fun createTransaction(
        payload: TransactionDraft
    ): Result<Transaction> {
        val request = payload.toRequestDto()
        Log.d(TAG, "Creating transaction: payload=${payload.logSummary()}, request=$request")
        val result = networkDataSource.createTransaction(request)
        if (result is NetworkResult.Success) {
            val created = when (val details = networkDataSource.getTransaction(result.data.id)) {
                is NetworkResult.Success -> details.data.toEntity(clock.millis()).localToDomain()
                else -> result.data.toEntity(
                    currencyCode = payload.amount.currency.code,
                    updatedAtEpochMillis = clock.millis()
                ).localToDomain()
            }
            transactionDao.upsertTransaction(
                created.toEntity(updatedAtEpochMillis = clock.millis())
            )
            return Result.success(created)
        }

        val networkResult = result.mapToResult(defaultDispatcher) { transaction ->
            transaction.toDomain(currencyCode = payload.amount.currency.code)
        }
        val error = requireNotNull(networkResult.exceptionOrNull())
        Log.e(TAG, "Failed to create transaction: payload=${payload.logSummary()}", error)
        return if (error.canReadFromLocalCache(networkMonitor.isOnline.value)) {
            Result.success(createPendingTransaction(payload))
        } else {
            networkResult
        }
    }

    override suspend fun getTransaction(id: Long): Result<Transaction> {
        Log.d(TAG, "Loading transaction: id=$id")
        if (id < 0) {
            return transactionDao.getTransaction(id)?.let { transaction ->
                Result.success(transaction.localToDomain())
            } ?: Result.failure(NoSuchElementException("Local transaction not found: id=$id"))
        }

        val result = networkDataSource.getTransaction(id)
        if (result is NetworkResult.Success) {
            transactionDao.upsertTransaction(result.data.toEntity(clock.millis()))
            return result.mapToResult(defaultDispatcher) { transaction -> transaction.toDomain() }
        }

        val networkResult = result.mapToResult(defaultDispatcher) { transaction ->
            transaction.toDomain()
        }
        val error = requireNotNull(networkResult.exceptionOrNull())
        Log.e(TAG, "Failed to load transaction from network: id=$id", error)
        val cachedTransaction = transactionDao.getTransaction(id)?.localToDomain()
        return if (cachedTransaction != null && error.canReadFromLocalCache(networkMonitor.isOnline.value)) {
            Result.success(cachedTransaction)
        } else {
            networkResult
        }
    }

    override suspend fun updateTransaction(
        id: Long,
        payload: TransactionDraft
    ): Result<Transaction> {
        val request = payload.toRequestDto()
        Log.d(TAG, "Updating transaction: id=$id, payload=${payload.logSummary()}, request=$request")
        if (id < 0) {
            return Result.success(updatePendingTransaction(id, payload))
        }

        val result = networkDataSource.updateTransaction(
            id = id,
            request = request
        )
        if (result is NetworkResult.Success) {
            transactionDao.upsertTransaction(result.data.toEntity(clock.millis()))
            return result.mapToResult(defaultDispatcher) { transaction -> transaction.toDomain() }
        }

        val networkResult = result.mapToResult(defaultDispatcher) { transaction ->
            transaction.toDomain()
        }
        val error = requireNotNull(networkResult.exceptionOrNull())
        Log.e(TAG, "Failed to update transaction: id=$id, payload=${payload.logSummary()}", error)
        return if (error.canReadFromLocalCache(networkMonitor.isOnline.value)) {
            Result.success(queueTransactionUpdate(id, payload))
        } else {
            networkResult
        }
    }

    override suspend fun deleteTransaction(id: Long): Result<Unit> {
        Log.d(TAG, "Deleting transaction: id=$id")
        if (id < 0) {
            database.withTransaction {
                transactionDao.deleteTransaction(id)
                syncOperationDao.deletePendingOperationsForTransaction(id)
            }
            return Result.success(Unit)
        }

        val result = networkDataSource.deleteTransaction(id)
        if (result is NetworkResult.Success) {
            transactionDao.deleteTransaction(id)
        }
        val mappedResult = result.mapToResult(defaultDispatcher) { }
        val error = mappedResult.exceptionOrNull()
        return when {
            mappedResult.isSuccess -> mappedResult
            error != null && error.canReadFromLocalCache(networkMonitor.isOnline.value) -> {
                queueTransactionDelete(id)
            }
            else -> mappedResult.onFailure { failure ->
                Log.e(TAG, "Failed to delete transaction: id=$id", failure)
            }
        }
    }

    private suspend fun loadCachedTransactions(
        query: TransactionsQuery
    ): Result<List<Transaction>> {
        return suspendRunCatching {
            val period = periodResolver.resolve(query)
            val entities = transactionDao.getTransactionsByPeriod(
                accountIds = query.accountIds,
                startEpochMillis = period.startEpochMillis,
                endEpochMillis = period.endEpochMillis
            )
            entities
                .filterByType(query.type)
                .map { transaction -> transaction.localToDomain() }
        }.onFailure { error ->
            Log.e(TAG, "Failed to load cached transactions: query=$query", error)
        }
    }

    private suspend fun mergePendingTransactions(
        query: TransactionsQuery,
        serverTransactions: List<Transaction>
    ): List<Transaction> {
        val period = periodResolver.resolve(query)
        val pendingTransactions = transactionDao.getPendingTransactionsByPeriod(
            accountIds = query.accountIds,
            startEpochMillis = period.startEpochMillis,
            endEpochMillis = period.endEpochMillis
        )
            .filterByType(query.type)
            .map { transaction -> transaction.localToDomain() }

        if (pendingTransactions.isEmpty()) {
            return serverTransactions.sortedByDescending { transaction -> transaction.transactionDate }
        }

        return (pendingTransactions + serverTransactions)
            .distinctBy { transaction -> transaction.id }
            .sortedByDescending { transaction -> transaction.transactionDate }
    }

    private suspend fun List<TransactionEntity>.filterByType(
        type: TransactionType?
    ): List<TransactionEntity> {
        val isIncome = when (type) {
            TransactionType.EXPENSE -> false
            TransactionType.INCOME -> true
            null -> return this
        }
        val categoryIds = categoryDao.getCategoryIdsByType(isIncome).toSet()
        return filter { transaction -> transaction.categoryId in categoryIds }
    }

    private suspend fun createPendingTransaction(payload: TransactionDraft): Transaction {
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
        return entity.localToDomain()
    }

    private suspend fun updatePendingTransaction(
        id: Long,
        payload: TransactionDraft
    ): Transaction {
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
        return entity.localToDomain()
    }

    private suspend fun queueTransactionUpdate(
        id: Long,
        payload: TransactionDraft
    ): Transaction {
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
        return entity.localToDomain()
    }

    private suspend fun queueTransactionDelete(id: Long): Result<Unit> {
        val cachedTransaction = transactionDao.getTransaction(id)
            ?: return Result.failure(NoSuchElementException("Cached transaction not found: id=$id"))
        val now = clock.millis()
        val operation = cachedTransaction.toDeleteOperation(
            createdAtEpochMillis = now
        )
        database.withTransaction {
            transactionDao.markDeletedPendingSync(
                id = id,
                updatedAtEpochMillis = now
            )
            syncOperationDao.deleteOperationsForTransaction(id)
            syncOperationDao.upsertOperation(operation)
        }
        syncWorkScheduler.enqueueOneTimeSync()
        return Result.success(Unit)
    }

    private fun TransactionResponseDto.matchesType(type: TransactionType?): Boolean {
        return when (type) {
            TransactionType.EXPENSE -> !category.isIncome
            TransactionType.INCOME -> category.isIncome
            null -> true
        }
    }

    private companion object {
        const val TAG = "TransactionsRepository"
    }
}

private fun SyncOperationEntity.withExistingId(id: Long?): SyncOperationEntity {
    return if (id == null) this else copy(id = id)
}

private fun TransactionEntity.toDeleteOperation(
    createdAtEpochMillis: Long
): SyncOperationEntity {
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

private fun TransactionDraft.logSummary(): String {
    return "TransactionDraft(" +
        "accountId=$accountId, " +
        "categoryId=$categoryId, " +
        "amount=${amount.amount.toPlainString()} ${amount.currency.code}, " +
        "transactionDate=$transactionDate, " +
        "comment=${comment.logValue()}" +
        ")"
}

private fun String?.logValue(): String {
    return when {
        this == null -> "null"
        isEmpty() -> "empty"
        isBlank() -> "blank(length=$length)"
        else -> "value(length=$length)"
    }
}
