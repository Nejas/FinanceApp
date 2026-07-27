package com.example.financeapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.financeapp.data.local.db.entity.CategoryEntity

@Dao
interface CategoryDao {

    @Query(
        """
        SELECT * FROM categories
        WHERE (:isIncome IS NULL OR isIncome = :isIncome)
        ORDER BY id
        """
    )
    suspend fun getCategories(isIncome: Boolean? = null): List<CategoryEntity>

    @Query("SELECT id FROM categories WHERE isIncome = :isIncome")
    suspend fun getCategoryIdsByType(isIncome: Boolean): List<Long>

    @Upsert
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun clearCategories()
}
