package com.example.financeapp.data.remote.datasource

import com.example.financeapp.data.network.api.FinanceApiService
import com.example.financeapp.data.network.executor.NetworkCallExecutor
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
import com.example.financeapp.data.network.result.requireSuccessfulUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitFinanceRemoteDataSource @Inject constructor(
    private val apiService: FinanceApiService,
    private val requestExecutor: NetworkCallExecutor
) : FinanceRemoteDataSource {

    override suspend fun getAccounts(): NetworkResult<List<AccountResponseDto>> {
        return requestExecutor.execute {
            apiService.getAccounts()
        }
    }

    override suspend fun createAccount(
        request: AccountCreateRequestDto
    ): NetworkResult<AccountResponseDto> {
        return requestExecutor.execute {
            apiService.createAccount(request)
        }
    }

    override suspend fun getAccount(id: Long): NetworkResult<AccountDetailsResponseDto> {
        return requestExecutor.execute {
            apiService.getAccount(id)
        }
    }

    override suspend fun updateAccount(
        id: Long,
        request: AccountUpdateRequestDto
    ): NetworkResult<AccountResponseDto> {
        return requestExecutor.execute {
            apiService.updateAccount(
                id = id,
                request = request
            )
        }
    }

    override suspend fun deleteAccount(id: Long): NetworkResult<Unit> {
        return requestExecutor.execute {
            apiService.deleteAccount(id).requireSuccessfulUnit()
        }
    }

    override suspend fun getAccountHistory(id: Long): NetworkResult<AccountHistoryResponseDto> {
        return requestExecutor.execute {
            apiService.getAccountHistory(id)
        }
    }

    override suspend fun getCategories(): NetworkResult<List<CategoryResponseDto>> {
        return requestExecutor.execute {
            apiService.getCategories()
        }
    }

    override suspend fun getCategoriesByType(
        isIncome: Boolean
    ): NetworkResult<List<CategoryResponseDto>> {
        return requestExecutor.execute {
            apiService.getCategoriesByType(isIncome)
        }
    }

    override suspend fun createTransaction(
        request: TransactionRequestDto
    ): NetworkResult<TransactionPlainResponseDto> {
        return requestExecutor.execute {
            apiService.createTransaction(request)
        }
    }

    override suspend fun getTransaction(id: Long): NetworkResult<TransactionResponseDto> {
        return requestExecutor.execute {
            apiService.getTransaction(id)
        }
    }

    override suspend fun updateTransaction(
        id: Long,
        request: TransactionRequestDto
    ): NetworkResult<TransactionResponseDto> {
        return requestExecutor.execute {
            apiService.updateTransaction(
                id = id,
                request = request
            )
        }
    }

    override suspend fun deleteTransaction(id: Long): NetworkResult<Unit> {
        return requestExecutor.execute {
            apiService.deleteTransaction(id).requireSuccessfulUnit()
        }
    }

    override suspend fun getTransactionsByPeriod(
        accountId: Long,
        startDate: String?,
        endDate: String?
    ): NetworkResult<List<TransactionResponseDto>> {
        return requestExecutor.execute {
            apiService.getTransactionsByPeriod(
                accountId = accountId,
                startDate = startDate,
                endDate = endDate
            )
        }
    }
}
