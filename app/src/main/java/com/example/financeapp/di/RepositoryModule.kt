package com.example.financeapp.di

import com.example.financeapp.data.repository.CategoriesDataRepository
import com.example.financeapp.data.repository.DataStoreUserSettingsRepository
import com.example.financeapp.data.repository.FinancialAccountsDataRepository
import com.example.financeapp.data.repository.TransactionsDataRepository
import com.example.financeapp.data.security.DefaultAuthProtectionManager
import com.example.financeapp.data.security.DataStoreSecurityRepository
import com.example.financeapp.domain.repository.CategoriesRepository
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import com.example.financeapp.domain.repository.SecurityRepository
import com.example.financeapp.domain.repository.TransactionsRepository
import com.example.financeapp.domain.repository.UserSettingsRepository
import com.example.financeapp.domain.security.AuthProtectionManager
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

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(
        repository: DataStoreUserSettingsRepository
    ): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindSecurityRepository(
        repository: DataStoreSecurityRepository
    ): SecurityRepository

    @Binds
    @Singleton
    abstract fun bindAuthProtectionManager(
        manager: DefaultAuthProtectionManager
    ): AuthProtectionManager

    companion object {

        @Provides
        @Singleton
        fun provideClock(): Clock {
            return Clock.systemDefaultZone()
        }
    }
}
