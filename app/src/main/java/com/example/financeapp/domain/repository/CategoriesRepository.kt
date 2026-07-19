package com.example.financeapp.domain.repository

import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.TransactionType

interface CategoriesRepository {

    suspend fun getCategories(type: TransactionType? = null): Result<List<Category>>
}

