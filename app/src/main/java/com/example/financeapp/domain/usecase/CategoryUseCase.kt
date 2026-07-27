package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.CategoriesRepository
import javax.inject.Inject

class CategoryUseCase @Inject constructor(
    private val repository: CategoriesRepository
) {

    suspend fun getCategories(type: TransactionType? = null): Result<List<Category>> =
        repository.getCategories(type)
}
