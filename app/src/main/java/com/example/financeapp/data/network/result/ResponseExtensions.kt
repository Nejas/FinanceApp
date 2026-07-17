package com.example.financeapp.data.network.result

import retrofit2.HttpException
import retrofit2.Response

fun Response<Unit>.requireSuccessfulUnit() {
    if (!isSuccessful) {
        throw HttpException(this)
    }
}
