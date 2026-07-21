package com.example.financeapp.data.network.executor

import com.example.financeapp.core.coroutines.IoDispatcher
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.network.result.NoInternetConnectionException
import com.example.financeapp.data.network.result.RetryPolicy
import com.example.financeapp.data.network.result.isRetryable
import com.example.financeapp.data.network.result.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@Singleton
class NetworkRequestExecutor @Inject constructor(
    private val retryPolicy: RetryPolicy,
    private val networkMonitor: NetworkMonitor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkCallExecutor {

    override suspend fun <T> execute(
        call: suspend () -> T
    ): NetworkResult<T> {
        return withContext(ioDispatcher) {
            executeWithRetry(call)
        }
    }

    private suspend fun <T> executeWithRetry(
        call: suspend () -> T
    ): NetworkResult<T> {
        var retryCount = 0
        var lastResult: NetworkResult<T>

        while (true) {
            if (!networkMonitor.isOnline.value) {
                return NetworkResult.NetworkError(NoInternetConnectionException())
            }

            lastResult = safeApiCall {
                withTimeout(retryPolicy.requestTimeoutMillis) {
                    call()
                }
            }

            if (lastResult is NetworkResult.Success) {
                return lastResult
            }

            if (retryCount >= retryPolicy.maxRetries || !lastResult.isRetryable()) {
                return lastResult
            }

            retryCount++

            if (retryPolicy.retryDelayMillis > 0) {
                delay(retryPolicy.retryDelayMillis)
            }
        }
    }
}
