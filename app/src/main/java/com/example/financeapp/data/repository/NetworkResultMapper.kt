package com.example.financeapp.data.repository

import com.example.financeapp.data.error.NetworkDataException
import com.example.financeapp.data.network.result.NetworkResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

inline fun <T, R> NetworkResult<T>.mapToResult(
    crossinline mapper: (T) -> R
): Result<R> {
    return when (this) {
        is NetworkResult.Success -> runCatching {
            mapper(data)
        }
        is NetworkResult.HttpError -> Result.failure(
            NetworkDataException.Http(
                code = code,
                message = message,
                errorBody = errorBody
            )
        )
        is NetworkResult.NetworkError -> Result.failure(
            NetworkDataException.Network(throwable)
        )
        is NetworkResult.TimeoutError -> Result.failure(
            NetworkDataException.Timeout(throwable)
        )
        is NetworkResult.SerializationError -> Result.failure(
            NetworkDataException.Serialization(throwable)
        )
        is NetworkResult.UnknownError -> Result.failure(
            NetworkDataException.Unknown(throwable)
        )
    }
}

suspend inline fun <T, R> NetworkResult<T>.mapToResult(
    dispatcher: CoroutineDispatcher,
    crossinline mapper: (T) -> R
): Result<R> {
    return withContext(dispatcher) {
        mapToResult(mapper)
    }
}
