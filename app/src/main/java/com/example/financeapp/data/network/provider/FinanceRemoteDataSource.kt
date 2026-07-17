package com.example.financeapp.data.network.provider

import com.example.financeapp.data.network.model.request.AccountCreateRequestDto
import com.example.financeapp.data.network.model.request.AccountUpdateRequestDto
import com.example.financeapp.data.network.model.request.TransactionRequestDto
import com.example.financeapp.data.network.model.response.AccountDetailsResponseDto
import com.example.financeapp.data.network.model.response.AccountHistoryResponseDto
import com.example.financeapp.data.network.model.response.AccountResponseDto
import com.example.financeapp.data.network.model.response.CategoryResponseDto
import com.example.financeapp.data.network.model.response.TransactionPlainResponseDto
import com.example.financeapp.data.network.model.response.TransactionResponseDto
import com.example.financeapp.data.network.result.NetworkResult

interface FinanceRemoteDataSource {

    suspend fun getAccounts(): NetworkResult<List<AccountResponseDto>>

    suspend fun createAccount(
        request: AccountCreateRequestDto
    ): NetworkResult<AccountResponseDto>

    suspend fun getAccount(id: Long): NetworkResult<AccountDetailsResponseDto>

    suspend fun updateAccount(
        id: Long,
        request: AccountUpdateRequestDto
    ): NetworkResult<AccountResponseDto>

    suspend fun deleteAccount(id: Long): NetworkResult<Unit>

    suspend fun getAccountHistory(id: Long): NetworkResult<AccountHistoryResponseDto>

    suspend fun getCategories(): NetworkResult<List<CategoryResponseDto>>

    suspend fun getCategoriesByType(isIncome: Boolean): NetworkResult<List<CategoryResponseDto>>

    suspend fun createTransaction(
        request: TransactionRequestDto
    ): NetworkResult<TransactionPlainResponseDto>

    suspend fun getTransaction(id: Long): NetworkResult<TransactionResponseDto>

    suspend fun updateTransaction(
        id: Long,
        request: TransactionRequestDto
    ): NetworkResult<TransactionResponseDto>

    suspend fun deleteTransaction(id: Long): NetworkResult<Unit>

    suspend fun getTransactionsByPeriod(
        accountId: Long,
        startDate: String? = null,
        endDate: String? = null
    ): NetworkResult<List<TransactionResponseDto>>
}
