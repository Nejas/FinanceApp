package com.example.financeapp.data.network.executor

import com.example.financeapp.data.network.result.NetworkResult

interface NetworkCallExecutor {

    suspend fun <T> execute(
        call: suspend () -> T
    ): NetworkResult<T>
}
