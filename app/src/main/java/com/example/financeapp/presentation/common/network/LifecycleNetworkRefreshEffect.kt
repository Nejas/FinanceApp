package com.example.financeapp.presentation.common.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

private const val DEFAULT_NETWORK_REFRESH_INTERVAL_MILLIS = 30_000L

@Composable
fun LifecycleNetworkRefreshEffect(
    refreshable: NetworkRefreshable,
    isOnline: Boolean = true,
    intervalMillis: Long = DEFAULT_NETWORK_REFRESH_INTERVAL_MILLIS,
    refreshImmediately: Boolean = true
) {
    val lifecycleOwner = LocalContext.current as? LifecycleOwner ?: return

    LaunchedEffect(lifecycleOwner, refreshable, isOnline, intervalMillis, refreshImmediately) {
        if (!isOnline) {
            return@LaunchedEffect
        }

        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (refreshImmediately) {
                refreshable.refreshFromNetwork(isSilent = true)
            }

            networkRefreshTickerFlow(intervalMillis).collect {
                refreshable.refreshFromNetwork(isSilent = true)
            }
        }
    }
}

private fun networkRefreshTickerFlow(intervalMillis: Long): Flow<Unit> = flow {
    while (currentCoroutineContext().isActive) {
        delay(intervalMillis)
        emit(Unit)
    }
}
