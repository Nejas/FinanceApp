package com.example.financeapp.data.repository

import android.util.Log
import com.example.financeapp.data.mock.MockFinancialAccountsDataSource
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialAccountsDataRepository @Inject constructor(
    private val dataSource: MockFinancialAccountsDataSource
) : FinancialAccountsRepository {

    override suspend fun getFinancialAccounts(): Result<List<FinancialAccount>> {
        Log.d(TAG, "Loading financial accounts")
        return runCatching(dataSource::getFinancialAccounts)
            .onFailure { error ->
                Log.e(TAG, "Failed to load financial accounts", error)
            }
    }

    private companion object {
        const val TAG = "FinancialAccountsRepo"
    }
}
