package com.example.financeapp.data.repository

import android.util.Log
import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.data.mapper.toCreateRequestDto
import com.example.financeapp.data.mapper.toDomain
import com.example.financeapp.data.mapper.toUpdateRequestDto
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.FinancialAccountPayload
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

@Singleton
class FinancialAccountsDataRepository @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : FinancialAccountsRepository {

    override suspend fun getFinancialAccounts(): Result<List<FinancialAccount>> {
        Log.d(TAG, "Loading financial accounts")
        return networkDataSource.getAccounts().mapToResult(defaultDispatcher) { accounts ->
            accounts.map { account -> account.toDomain() }
        }.onFailure { error ->
            Log.e(TAG, "Failed to load financial accounts", error)
        }
    }

    override suspend fun createFinancialAccount(
        payload: FinancialAccountPayload
    ): Result<FinancialAccount> {
        Log.d(TAG, "Creating financial account")
        return networkDataSource.createAccount(payload.toCreateRequestDto()).mapToResult(defaultDispatcher) { account ->
            account.toDomain()
        }.onFailure { error ->
            Log.e(TAG, "Failed to create financial account", error)
        }
    }

    override suspend fun getFinancialAccount(id: Long): Result<FinancialAccount> {
        Log.d(TAG, "Loading financial account: id=$id")
        return networkDataSource.getAccount(id).mapToResult(defaultDispatcher) { account ->
            account.toDomain()
        }.onFailure { error ->
            Log.e(TAG, "Failed to load financial account: id=$id", error)
        }
    }

    override suspend fun updateFinancialAccount(
        id: Long,
        payload: FinancialAccountPayload
    ): Result<FinancialAccount> {
        Log.d(TAG, "Updating financial account: id=$id")
        return networkDataSource.updateAccount(
            id = id,
            request = payload.toUpdateRequestDto()
        ).mapToResult(defaultDispatcher) { account ->
            account.toDomain()
        }.onFailure { error ->
            Log.e(TAG, "Failed to update financial account: id=$id", error)
        }
    }

    override suspend fun deleteFinancialAccount(id: Long): Result<Unit> {
        Log.d(TAG, "Deleting financial account: id=$id")
        return networkDataSource.deleteAccount(id).mapToResult(defaultDispatcher) { }
            .onFailure { error ->
                Log.e(TAG, "Failed to delete financial account: id=$id", error)
            }
    }

    private companion object {
        const val TAG = "FinancialAccountsRepo"
    }
}
