package com.example.financeapp.di

import com.example.financeapp.data.repository.CategoriesDataRepository
import com.example.financeapp.data.repository.FinancialAccountsDataRepository
import com.example.financeapp.data.repository.TransactionsDataRepository
import com.example.financeapp.domain.repository.CategoriesRepository
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import com.example.financeapp.domain.repository.TransactionsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionsRepository(
        repository: TransactionsDataRepository
    ): TransactionsRepository

    @Binds
    @Singleton
    abstract fun bindFinancialAccountsRepository(
        repository: FinancialAccountsDataRepository
    ): FinancialAccountsRepository

    @Binds
    @Singleton
    abstract fun bindCategoriesRepository(
        repository: CategoriesDataRepository
    ): CategoriesRepository

    companion object {

        @Provides
        @Singleton
        fun provideClock(): Clock {
            return Clock.systemDefaultZone()
        }
    }
}
