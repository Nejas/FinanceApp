package com.example.financeapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.financeapp.data.sync.ForegroundSyncObserver
import com.example.financeapp.data.sync.SyncBootstrapper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FinanceApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncBootstrapper: SyncBootstrapper

    @Inject
    lateinit var foregroundSyncObserver: ForegroundSyncObserver

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        syncBootstrapper.start()
        foregroundSyncObserver.start()
    }
}
