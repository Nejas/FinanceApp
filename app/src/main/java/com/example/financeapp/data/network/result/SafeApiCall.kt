package com.example.financeapp.data.network.result

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

suspend inline fun <T> safeApiCall(
    crossinline call: suspend () -> T
): NetworkResult<T> {
    return try {
        NetworkResult.Success(call())
    } catch (error: HttpException) {
        NetworkResult.HttpError(
            code = error.code(),
            message = error.message(),
            errorBody = error.response()?.errorBody()?.string()
        )
    } catch (error: TimeoutCancellationException) {
        NetworkResult.TimeoutError(error)
    } catch (error: CancellationException) {
        throw error
    } catch (error: SerializationException) {
        NetworkResult.SerializationError(error)
    } catch (error: IOException) {
        NetworkResult.NetworkError(error)
    } catch (error: Throwable) {
        NetworkResult.UnknownError(error)
    }
}
