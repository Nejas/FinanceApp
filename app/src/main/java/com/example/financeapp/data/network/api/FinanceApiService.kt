package com.example.financeapp.data.network.api

import com.example.financeapp.data.network.model.request.AccountCreateRequestDto
import com.example.financeapp.data.network.model.request.AccountUpdateRequestDto
import com.example.financeapp.data.network.model.request.TransactionRequestDto
import com.example.financeapp.data.network.model.response.AccountDetailsResponseDto
import com.example.financeapp.data.network.model.response.AccountHistoryResponseDto
import com.example.financeapp.data.network.model.response.AccountResponseDto
import com.example.financeapp.data.network.model.response.CategoryResponseDto
import com.example.financeapp.data.network.model.response.TransactionPlainResponseDto
import com.example.financeapp.data.network.model.response.TransactionResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface FinanceApiService {

    @GET("accounts")
    suspend fun getAccounts(): List<AccountResponseDto>

    @POST("accounts")
    suspend fun createAccount(
        @Body request: AccountCreateRequestDto
    ): AccountResponseDto

    @GET("accounts/{id}")
    suspend fun getAccount(
        @Path("id") id: Long
    ): AccountDetailsResponseDto

    @PUT("accounts/{id}")
    suspend fun updateAccount(
        @Path("id") id: Long,
        @Body request: AccountUpdateRequestDto
    ): AccountResponseDto

    @DELETE("accounts/{id}")
    suspend fun deleteAccount(
        @Path("id") id: Long
    ): Response<Unit>

    @GET("accounts/{id}/history")
    suspend fun getAccountHistory(
        @Path("id") id: Long
    ): AccountHistoryResponseDto

    @GET("categories")
    suspend fun getCategories(): List<CategoryResponseDto>

    @GET("categories/type/{isIncome}")
    suspend fun getCategoriesByType(
        @Path("isIncome") isIncome: Boolean
    ): List<CategoryResponseDto>

    @POST("transactions")
    suspend fun createTransaction(
        @Body request: TransactionRequestDto
    ): TransactionPlainResponseDto

    @GET("transactions/{id}")
    suspend fun getTransaction(
        @Path("id") id: Long
    ): TransactionResponseDto

    @PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: Long,
        @Body request: TransactionRequestDto
    ): TransactionResponseDto

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: Long
    ): Response<Unit>

    @GET("transactions/account/{accountId}/period")
    suspend fun getTransactionsByPeriod(
        @Path("accountId") accountId: Long,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): List<TransactionResponseDto>
}
