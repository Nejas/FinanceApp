package com.example.financeapp.data.repository

import android.util.Log
import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.data.mapper.toDomain
import com.example.financeapp.data.mapper.toRequestDto
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.model.TransactionPayload
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Singleton
class TransactionsDataRepository @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : TransactionsRepository {

    override suspend fun getTransactions(
        query: TransactionsQuery
    ): Result<List<Transaction>> {
        Log.d(TAG, "Loading transactions: query=$query")
        return suspendRunCatching {
            coroutineScope {
                query.accountIds.map { accountId ->
                    async {
                        networkDataSource.getTransactionsByPeriod(
                            accountId = accountId,
                            startDate = query.startDate?.toString(),
                            endDate = query.endDate?.toString()
                        ).mapToResult(defaultDispatcher) { transactions ->
                            transactions
                                .filter { transaction ->
                                    when (query.type) {
                                        TransactionType.EXPENSE -> !transaction.category.isIncome
                                        TransactionType.INCOME -> transaction.category.isIncome
                                        null -> true
                                    }
                                }
                                .map { transaction -> transaction.toDomain() }
                        }.getOrThrow()
                    }
                }.awaitAll().flatten()
            }
        }.onFailure { error ->
            Log.e(TAG, "Failed to load transactions: query=$query", error)
        }
    }

    override suspend fun createTransaction(
        payload: TransactionPayload
    ): Result<Transaction> {
        Log.d(TAG, "Creating transaction")
        return when (val createResult = networkDataSource.createTransaction(payload.toRequestDto())) {
            is NetworkResult.Success -> {
                networkDataSource.getTransaction(createResult.data.id).mapToResult(defaultDispatcher) { transaction ->
                    transaction.toDomain()
                }
            }
            else -> createResult.mapToResult(defaultDispatcher) { transaction ->
                transaction.toDomain(currencyCode = payload.amount.currency.code)
            }
        }.onFailure { error ->
            Log.e(TAG, "Failed to create transaction", error)
        }
    }

    override suspend fun getTransaction(id: Long): Result<Transaction> {
        Log.d(TAG, "Loading transaction: id=$id")
        return networkDataSource.getTransaction(id).mapToResult(defaultDispatcher) { transaction ->
            transaction.toDomain()
        }.onFailure { error ->
            Log.e(TAG, "Failed to load transaction: id=$id", error)
        }
    }

    override suspend fun updateTransaction(
        id: Long,
        payload: TransactionPayload
    ): Result<Transaction> {
        Log.d(TAG, "Updating transaction: id=$id")
        return networkDataSource.updateTransaction(
            id = id,
            request = payload.toRequestDto()
        ).mapToResult(defaultDispatcher) { transaction ->
            transaction.toDomain()
        }.onFailure { error ->
            Log.e(TAG, "Failed to update transaction: id=$id", error)
        }
    }

    override suspend fun deleteTransaction(id: Long): Result<Unit> {
        Log.d(TAG, "Deleting transaction: id=$id")
        return networkDataSource.deleteTransaction(id).mapToResult(defaultDispatcher) { }
            .onFailure { error ->
                Log.e(TAG, "Failed to delete transaction: id=$id", error)
            }
    }

    private companion object {
        const val TAG = "TransactionsRepository"
    }
}
