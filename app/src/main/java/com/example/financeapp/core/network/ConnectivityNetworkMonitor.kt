package com.example.financeapp.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.example.financeapp.core.coroutines.DefaultDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

@Singleton
class ConnectivityNetworkMonitor @Inject constructor(
    @ApplicationContext context: Context,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : NetworkMonitor {

    private val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager
    private val scope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    override val isOnline: StateFlow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(connectivityManager.hasValidatedInternet())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(networkCapabilities.hasValidatedInternet())
            }

            override fun onLost(network: Network) {
                trySend(connectivityManager.hasValidatedInternet())
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        trySend(connectivityManager.hasValidatedInternet())
        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = connectivityManager.hasValidatedInternet()
        )
}

private fun ConnectivityManager.hasValidatedInternet(): Boolean {
    val activeNetwork = activeNetwork ?: return false
    val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasValidatedInternet()
}

private fun NetworkCapabilities.hasValidatedInternet(): Boolean {
    return hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
