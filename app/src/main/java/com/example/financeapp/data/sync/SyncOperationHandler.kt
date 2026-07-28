package com.example.financeapp.data.sync

import com.example.financeapp.data.local.db.entity.SyncOperationEntity
import com.example.financeapp.data.local.db.entity.SyncOperationType
import com.example.financeapp.data.network.result.NetworkResult

interface SyncOperationHandler {
    suspend fun handle(operation: SyncOperationEntity): NetworkResult<Unit>
}

val SyncOperationEntity.type: SyncOperationType
    get() = SyncOperationType.valueOf(operationType)

fun NetworkResult<*>.errorMessage(): String {
    return when (this) {
        is NetworkResult.Success -> "success"
        is NetworkResult.HttpError -> "HTTP $code ${message.orEmpty()}".trim()
        is NetworkResult.NetworkError -> throwable.message ?: "Network error"
        is NetworkResult.TimeoutError -> throwable.message ?: "Timeout"
        is NetworkResult.SerializationError -> throwable.message ?: "Serialization error"
        is NetworkResult.UnknownError -> throwable.message ?: "Unknown error"
    }
}

fun NetworkResult<*>.asUnit(): NetworkResult<Unit> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.HttpError -> this
        is NetworkResult.NetworkError -> this
        is NetworkResult.TimeoutError -> this
        is NetworkResult.SerializationError -> this
        is NetworkResult.UnknownError -> this
    }
}

fun NetworkResult<*>.isNotFound(): Boolean {
    return this is NetworkResult.HttpError && code == HTTP_NOT_FOUND
}

private const val HTTP_NOT_FOUND = 404
