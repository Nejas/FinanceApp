package com.example.financeapp.data.sync

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.network.NetworkMonitor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@Singleton
class SyncBootstrapper @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val syncWorkScheduler: SyncWorkScheduler,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) {

    private val scope = CoroutineScope(SupervisorJob() + defaultDispatcher)
    private var started = false

    fun start() {
        if (started) return
        started = true

        syncWorkScheduler.enqueuePeriodicSync()
        if (networkMonitor.isOnline.value) {
            syncWorkScheduler.enqueueOneTimeSync()
        }

        scope.launch {
            networkMonitor.isOnline
                .drop(1)
                .distinctUntilChanged()
                .collect { isOnline ->
                    if (isOnline) {
                        syncWorkScheduler.enqueueOneTimeSync()
                    }
                }
        }
    }
}
