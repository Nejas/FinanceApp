package com.example.financeapp.data.local.di

import android.content.Context
import androidx.room.Room
import com.example.financeapp.data.local.LocalTransactionIdGenerator
import com.example.financeapp.data.local.LocalAccountIdGenerator
import com.example.financeapp.data.local.NegativeLocalAccountIdGenerator
import com.example.financeapp.data.local.NegativeLocalTransactionIdGenerator
import com.example.financeapp.data.local.db.FinanceDatabase
import com.example.financeapp.data.local.db.MIGRATION_2_3
import com.example.financeapp.data.local.db.MIGRATION_3_4
import com.example.financeapp.data.local.db.MIGRATION_4_5
import com.example.financeapp.data.local.db.dao.AccountDao
import com.example.financeapp.data.local.db.dao.CategoryDao
import com.example.financeapp.data.local.db.dao.SyncOperationDao
import com.example.financeapp.data.local.db.dao.TransactionDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataBindingsModule {

    @Binds
    @Singleton
    abstract fun bindLocalTransactionIdGenerator(
        generator: NegativeLocalTransactionIdGenerator
    ): LocalTransactionIdGenerator

    @Binds
    @Singleton
    abstract fun bindLocalAccountIdGenerator(
        generator: NegativeLocalAccountIdGenerator
    ): LocalAccountIdGenerator
}

@Module
@InstallIn(SingletonComponent::class)
object LocalDataModule {

    @Provides
    @Singleton
    fun provideFinanceDatabase(
        @ApplicationContext context: Context
    ): FinanceDatabase {
        return Room.databaseBuilder(
            context,
            FinanceDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    @Provides
    fun provideAccountDao(database: FinanceDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    fun provideCategoryDao(database: FinanceDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideTransactionDao(database: FinanceDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideSyncOperationDao(database: FinanceDatabase): SyncOperationDao {
        return database.syncOperationDao()
    }

    private const val DATABASE_NAME = "finance.db"
}
