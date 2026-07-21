package com.example.financeapp.core.network.di

import com.example.financeapp.core.network.ConnectivityNetworkMonitor
import com.example.financeapp.core.network.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkMonitorModule {

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        monitor: ConnectivityNetworkMonitor
    ): NetworkMonitor
}
