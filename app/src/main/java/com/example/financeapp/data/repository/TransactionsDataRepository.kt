package com.example.financeapp.data.repository

import android.util.Log
import com.example.financeapp.data.mapper.toDomain
import com.example.financeapp.data.mapper.toRequestDto
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.network.provider.FinanceRemoteDataSource
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionFilter
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.model.common.TransactionPayload
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsDataRepository @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource
) : TransactionsRepository {

    override suspend fun getTransactions(
        filter: TransactionFilter
    ): Result<List<Transaction>> {
        Log.d(TAG, "Loading transactions: filter=$filter")
        return networkDataSource.getTransactionsByPeriod(
            accountId = filter.accountId,
            startDate = filter.startDate?.toString(),
            endDate = filter.endDate?.toString()
        ).mapToResult { transactions ->
            transactions
                .filter { transaction ->
                    when (filter.type) {
                        TransactionType.EXPENSE -> !transaction.category.isIncome
                        TransactionType.INCOME -> transaction.category.isIncome
                        null -> true
                    }
                }
                .map { transaction -> transaction.toDomain() }
        }.onFailure { error ->
            Log.e(TAG, "Failed to load transactions: filter=$filter", error)
        }
    }

    override suspend fun createTransaction(
        payload: TransactionPayload
    ): Result<Transaction> {
        Log.d(TAG, "Creating transaction")
        return when (val createResult = networkDataSource.createTransaction(payload.toRequestDto())) {
            is NetworkResult.Success -> {
                networkDataSource.getTransaction(createResult.data.id).mapToResult { transaction ->
                    transaction.toDomain()
                }
            }
            else -> createResult.mapToResult { transaction ->
                transaction.toDomain(currencyCode = payload.amount.currency.code)
            }
        }.onFailure { error ->
            Log.e(TAG, "Failed to create transaction", error)
        }
    }

    override suspend fun getTransaction(id: Long): Result<Transaction> {
        Log.d(TAG, "Loading transaction: id=$id")
        return networkDataSource.getTransaction(id).mapToResult { transaction ->
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
        ).mapToResult { transaction ->
            transaction.toDomain()
        }.onFailure { error ->
            Log.e(TAG, "Failed to update transaction: id=$id", error)
        }
    }

    override suspend fun deleteTransaction(id: Long): Result<Unit> {
        Log.d(TAG, "Deleting transaction: id=$id")
        return networkDataSource.deleteTransaction(id).mapToResult { }
            .onFailure { error ->
                Log.e(TAG, "Failed to delete transaction: id=$id", error)
            }
    }

    private companion object {
        const val TAG = "TransactionsRepository"
    }
}
