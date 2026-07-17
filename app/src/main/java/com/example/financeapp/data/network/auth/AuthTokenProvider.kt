package com.example.financeapp.data.network.auth

interface AuthTokenProvider {

    fun getToken(): String?
}
