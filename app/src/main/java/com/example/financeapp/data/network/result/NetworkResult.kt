package com.example.financeapp.data.network.result

sealed interface NetworkResult<out T> {

    data class Success<T>(
        val data: T
    ) : NetworkResult<T>

    data class HttpError(
        val code: Int,
        val message: String?,
        val errorBody: String?
    ) : NetworkResult<Nothing>

    data class NetworkError(
        val throwable: Throwable
    ) : NetworkResult<Nothing>

    data class TimeoutError(
        val throwable: Throwable
    ) : NetworkResult<Nothing>

    data class SerializationError(
        val throwable: Throwable
    ) : NetworkResult<Nothing>

    data class UnknownError(
        val throwable: Throwable
    ) : NetworkResult<Nothing>
}
