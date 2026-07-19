package com.example.financeapp.data.repository

import android.util.Log
import com.example.financeapp.data.mock.MockCategoriesDataSource
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.CategoriesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriesDataRepository @Inject constructor(
    private val dataSource: MockCategoriesDataSource
) : CategoriesRepository {

    override suspend fun getCategories(type: TransactionType?): Result<List<Category>> {
        Log.d(TAG, "Loading categories: type=$type")
        return runCatching {
            when (type) {
                TransactionType.EXPENSE -> dataSource.getExpenseCategories()
                TransactionType.INCOME -> dataSource.getIncomeCategories()
                null -> dataSource.getCategories()
            }
        }.onFailure { error ->
            Log.e(TAG, "Failed to load categories: type=$type", error)
        }
    }

    private companion object {
        const val TAG = "CategoriesRepository"
    }
}
