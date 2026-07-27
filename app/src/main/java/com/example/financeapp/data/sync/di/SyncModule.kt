package com.example.financeapp.data.sync.di

import com.example.financeapp.data.sync.DefaultSyncEventsRepository
import com.example.financeapp.data.sync.SyncEventPublisher
import com.example.financeapp.data.sync.SyncWorkScheduler
import com.example.financeapp.data.sync.SyncOperationsDataRepository
import com.example.financeapp.data.sync.WorkManagerSyncWorkScheduler
import com.example.financeapp.domain.repository.SyncEventsRepository
import com.example.financeapp.domain.repository.SyncOperationsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncWorkScheduler(
        scheduler: WorkManagerSyncWorkScheduler
    ): SyncWorkScheduler

    @Binds
    @Singleton
    abstract fun bindSyncEventsRepository(
        repository: DefaultSyncEventsRepository
    ): SyncEventsRepository

    @Binds
    @Singleton
    abstract fun bindSyncEventPublisher(
        publisher: DefaultSyncEventsRepository
    ): SyncEventPublisher

    @Binds
    @Singleton
    abstract fun bindSyncOperationsRepository(
        repository: SyncOperationsDataRepository
    ): SyncOperationsRepository
}
