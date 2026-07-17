package com.example.financeapp.data.error

sealed class NetworkDataException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    class Http(
        val code: Int,
        val errorBody: String?,
        message: String?
    ) : NetworkDataException(
        message = "HTTP error $code${message?.let { ": $it" }.orEmpty()}"
    )

    class Network(
        cause: Throwable
    ) : NetworkDataException("Network error", cause)

    class Timeout(
        cause: Throwable
    ) : NetworkDataException("Network request timed out", cause)

    class Serialization(
        cause: Throwable
    ) : NetworkDataException("Failed to parse network response", cause)

    class Unknown(
        cause: Throwable
    ) : NetworkDataException("Unknown network error", cause)
}
