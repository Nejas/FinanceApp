package com.example.financeapp.data.repository

import android.util.Log
import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.data.local.db.dao.AccountDao
import com.example.financeapp.data.local.db.entity.AccountSyncState
import com.example.financeapp.data.local.mapper.toDomain as localToDomain
import com.example.financeapp.data.local.mapper.toEntity
import com.example.financeapp.data.mapper.toCreateRequestDto
import com.example.financeapp.data.mapper.toDomain
import com.example.financeapp.data.mapper.toUpdateRequestDto
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.offline.PendingAccountMutationStore
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.AccountDraft
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

@Singleton
class FinancialAccountsDataRepository @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource,
    private val accountDao: AccountDao,
    private val pendingMutations: PendingAccountMutationStore,
    private val networkMonitor: NetworkMonitor,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : FinancialAccountsRepository {

    override suspend fun getFinancialAccounts(): Result<List<Account>> {
        Log.d(TAG, "Loading financial accounts")
        val result = networkDataSource.getAccounts()
        if (result is NetworkResult.Success) {
            accountDao.replaceSyncedAccounts(result.data.map { account -> account.toEntity() })
            return Result.success(
                accountDao.getAccounts().map { account -> account.localToDomain() }
            )
        }

        val networkResult = result.mapToResult(defaultDispatcher) { accounts ->
            accounts.map { account -> account.toDomain() }
        }
        val error = requireNotNull(networkResult.exceptionOrNull())
        Log.e(TAG, "Failed to load financial accounts from network", error)
        if (!error.canReadFromLocalCache(networkMonitor.isOnline.value)) {
            return networkResult
        }

        val cachedAccounts = accountDao.getAccounts().map { account -> account.localToDomain() }
        return if (cachedAccounts.isNotEmpty()) {
            Result.success(cachedAccounts)
        } else {
            networkResult
        }
    }

    override suspend fun createFinancialAccount(
        payload: AccountDraft
    ): Result<Account> {
        Log.d(TAG, "Creating financial account")
        val result = networkDataSource.createAccount(payload.toCreateRequestDto())
        if (result is NetworkResult.Success) {
            accountDao.upsertAccount(result.data.toEntity())
            return result.mapToResult(defaultDispatcher) { account -> account.toDomain() }
        }
        val networkResult = result.mapToResult(defaultDispatcher) { account ->
            account.toDomain()
        }
        val error = requireNotNull(networkResult.exceptionOrNull())
        Log.e(TAG, "Failed to create financial account", error)
        return if (error.canReadFromLocalCache(networkMonitor.isOnline.value)) {
            Result.success(pendingMutations.create(payload))
        } else {
            networkResult
        }
    }

    override suspend fun getFinancialAccount(id: Long): Result<Account> {
        Log.d(TAG, "Loading financial account: id=$id")
        val cachedAccount = accountDao.getAccount(id)
        if (id < 0 || cachedAccount?.syncState == AccountSyncState.PENDING.name) {
            return cachedAccount?.let { account ->
                Result.success(account.localToDomain())
            } ?: Result.failure(NoSuchElementException("Local account not found: id=$id"))
        }
        val result = networkDataSource.getAccount(id)
        if (result is NetworkResult.Success) {
            accountDao.upsertAccount(result.data.toEntity())
            return result.mapToResult(defaultDispatcher) { account ->
                account.toDomain()
            }
        }

        val networkResult = result.mapToResult(defaultDispatcher) { account ->
            account.toDomain()
        }
        val cachedAccountDomain = cachedAccount?.localToDomain()
        val error = requireNotNull(networkResult.exceptionOrNull())
        return if (cachedAccountDomain != null && error.canReadFromLocalCache(networkMonitor.isOnline.value)) {
            Result.success(cachedAccountDomain)
        } else {
            networkResult.onFailure { error ->
                Log.e(TAG, "Failed to load financial account: id=$id", error)
            }
        }
    }

    override suspend fun updateFinancialAccount(
        id: Long,
        payload: AccountDraft
    ): Result<Account> {
        Log.d(TAG, "Updating financial account: id=$id")
        if (id < 0) {
            return Result.success(pendingMutations.updatePending(id, payload))
        }
        val result = networkDataSource.updateAccount(
            id = id,
            request = payload.toUpdateRequestDto()
        )
        if (result is NetworkResult.Success) {
            accountDao.upsertAccount(result.data.toEntity())
            return result.mapToResult(defaultDispatcher) { account -> account.toDomain() }
        }
        val networkResult = result.mapToResult(defaultDispatcher) { account ->
            account.toDomain()
        }
        val error = requireNotNull(networkResult.exceptionOrNull())
        Log.e(TAG, "Failed to update financial account: id=$id", error)
        return if (error.canReadFromLocalCache(networkMonitor.isOnline.value)) {
            Result.success(pendingMutations.queueUpdate(id, payload))
        } else {
            networkResult
        }
    }

    override suspend fun deleteFinancialAccount(id: Long): Result<Unit> {
        Log.d(TAG, "Deleting financial account: id=$id")
        val result = networkDataSource.deleteAccount(id)
        if (result is NetworkResult.Success) {
            accountDao.deleteAccount(id)
        }
        return result.mapToResult(defaultDispatcher) { }
            .onFailure { error ->
                Log.e(TAG, "Failed to delete financial account: id=$id", error)
            }
    }

    private companion object {
        const val TAG = "FinancialAccountsRepo"
    }
}
