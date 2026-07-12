package com.example.financeapp.data.repository

import android.util.Log
import com.example.financeapp.data.mock.MockExpensesDataSource
import com.example.financeapp.data.mock.MockIncomeDataSource
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsDataRepository @Inject constructor(
    private val expensesDataSource: MockExpensesDataSource,
    private val incomeDataSource: MockIncomeDataSource
) : TransactionsRepository {

    override suspend fun getTransactions(
        type: TransactionType
    ): Result<List<Transaction>> {
        Log.d(TAG, "Loading transactions: type=$type")
        return runCatching {
            when (type) {
                TransactionType.EXPENSE -> expensesDataSource.getExpenses()
                TransactionType.INCOME -> incomeDataSource.getIncome()
            }
        }.onFailure { error ->
            Log.e(TAG, "Failed to load transactions: type=$type", error)
        }
    }

    private companion object {
        const val TAG = "TransactionsRepository"
    }
}
