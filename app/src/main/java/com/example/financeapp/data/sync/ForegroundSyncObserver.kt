package com.example.financeapp.data.sync

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.financeapp.core.network.NetworkMonitor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Singleton
class ForegroundSyncObserver @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val syncCoordinator: SyncCoordinator
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var started = false

    fun start() {
        if (started) return
        started = true

        scope.launch {
            ProcessLifecycleOwner.get().lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isOnline
                    .collectLatest { isOnline ->
                        if (!isOnline) return@collectLatest

                        syncCoordinator.sync()
                        while (currentCoroutineContext().isActive) {
                            delay(FOREGROUND_SYNC_INTERVAL_MILLIS)
                            syncCoordinator.sync()
                        }
                    }
            }
        }
    }

    private companion object {
        const val FOREGROUND_SYNC_INTERVAL_MILLIS = 30_000L
    }
}
