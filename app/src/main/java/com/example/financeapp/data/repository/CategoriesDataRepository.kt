package com.example.financeapp.data.repository

import android.util.Log
import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.data.local.db.dao.CategoryDao
import com.example.financeapp.data.local.mapper.toDomain as localToDomain
import com.example.financeapp.data.local.mapper.toEntity
import com.example.financeapp.data.mapper.toDomain
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.CategoriesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

@Singleton
class CategoriesDataRepository @Inject constructor(
    private val networkDataSource: FinanceRemoteDataSource,
    private val categoryDao: CategoryDao,
    private val networkMonitor: NetworkMonitor,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : CategoriesRepository {

    override suspend fun getCategories(type: TransactionType?): Result<List<Category>> {
        Log.d(TAG, "Loading categories: type=$type")
        val result = when (type) {
            TransactionType.EXPENSE -> networkDataSource.getCategoriesByType(isIncome = false)
            TransactionType.INCOME -> networkDataSource.getCategoriesByType(isIncome = true)
            null -> networkDataSource.getCategories()
        }
        if (result is NetworkResult.Success) {
            categoryDao.upsertCategories(result.data.map { category -> category.toEntity() })
            return result.mapToResult(defaultDispatcher) { categories ->
                categories.map { category -> category.toDomain() }
            }
        }

        val networkResult = result.mapToResult(defaultDispatcher) { categories ->
            categories.map { category -> category.toDomain() }
        }
        val error = requireNotNull(networkResult.exceptionOrNull())
        Log.e(TAG, "Failed to load categories from network: type=$type", error)
        if (!error.canReadFromLocalCache(networkMonitor.isOnline.value)) {
            return networkResult
        }

        val cachedCategories = categoryDao.getCategories(type.toIsIncomeOrNull())
            .map { category -> category.localToDomain() }

        return if (cachedCategories.isNotEmpty()) {
            Result.success(cachedCategories)
        } else {
            networkResult
        }
    }

    private companion object {
        const val TAG = "CategoriesRepository"
    }
}

private fun TransactionType?.toIsIncomeOrNull(): Boolean? {
    return when (this) {
        TransactionType.INCOME -> true
        TransactionType.EXPENSE -> false
        null -> null
    }
}
