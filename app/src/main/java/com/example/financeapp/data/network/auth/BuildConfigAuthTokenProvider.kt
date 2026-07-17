package com.example.financeapp.data.network.auth

import com.example.financeapp.BuildConfig
import javax.inject.Inject

class BuildConfigAuthTokenProvider @Inject constructor() : AuthTokenProvider {

    override fun getToken(): String? {
        return BuildConfig.SHMR_API_TOKEN.takeIf { token -> token.isNotBlank() }
    }
}
