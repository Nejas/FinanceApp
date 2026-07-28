package com.example.financeapp.data.sync

import com.example.financeapp.data.local.TransactionPeriodResolver
import com.example.financeapp.data.local.db.dao.AccountDao
import com.example.financeapp.data.local.db.dao.CategoryDao
import com.example.financeapp.data.local.db.dao.TransactionDao
import com.example.financeapp.data.local.mapper.toEntity
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.network.result.isRetryable
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource
import com.example.financeapp.domain.model.TransactionsQuery
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerSnapshotRefresher @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val periodResolver: TransactionPeriodResolver,
    private val clock: Clock
) {

    suspend fun refresh(): SnapshotRefreshResult {
        val accountsResult = networkDataSource.getAccounts()
        if (accountsResult.isRetryable()) {
            return SnapshotRefreshResult.RetryLater
        }
        val accounts = (accountsResult as? NetworkResult.Success)?.data
            ?: return SnapshotRefreshResult.Completed
        accountDao.replaceSyncedAccounts(accounts.map { account -> account.toEntity() })

        val categoriesResult = networkDataSource.getCategories()
        if (categoriesResult.isRetryable()) {
            return SnapshotRefreshResult.RetryLater
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
                return SnapshotRefreshResult.RetryLater
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
        return SnapshotRefreshResult.Completed
    }
}

enum class SnapshotRefreshResult {
    Completed,
    RetryLater
}
