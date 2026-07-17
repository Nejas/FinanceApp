package com.example.financeapp.data.repository

import android.util.Log
import com.example.financeapp.data.mapper.toDomain
import com.example.financeapp.data.network.provider.FinanceRemoteDataSource
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.CategoriesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriesDataRepository @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource
) : CategoriesRepository {

    override suspend fun getCategories(type: TransactionType?): Result<List<Category>> {
        Log.d(TAG, "Loading categories: type=$type")
        val result = when (type) {
            TransactionType.EXPENSE -> networkDataSource.getCategoriesByType(isIncome = false)
            TransactionType.INCOME -> networkDataSource.getCategoriesByType(isIncome = true)
            null -> networkDataSource.getCategories()
        }
        return result.mapToResult { categories ->
            categories.map { category -> category.toDomain() }
        }.onFailure { error ->
            Log.e(TAG, "Failed to load categories: type=$type", error)
        }
    }

    private companion object {
        const val TAG = "CategoriesRepository"
    }
}
