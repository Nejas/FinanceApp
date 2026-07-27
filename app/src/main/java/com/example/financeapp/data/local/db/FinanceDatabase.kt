package com.example.financeapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.financeapp.data.local.db.dao.AccountDao
import com.example.financeapp.data.local.db.dao.CategoryDao
import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.dao.TransactionDao
import com.example.financeapp.data.local.db.entity.AccountEntity
import com.example.financeapp.data.local.db.entity.CategoryEntity
import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.db.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        SyncOperationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao

    abstract fun categoryDao(): CategoryDao

    abstract fun transactionDao(): TransactionDao

    abstract fun syncOperationDao(): SyncOperationDao
}
