package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.CategoriesRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoriesRepository
) {

    suspend operator fun invoke(type: TransactionType? = null): Result<List<Category>> {
        return repository.getCategories(type)
    }
}

