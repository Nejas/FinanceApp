package com.example.financeapp.domain.repository

import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.FinancialAccountPayload

interface FinancialAccountsRepository {

    suspend fun getFinancialAccounts(): Result<List<Account>>

    suspend fun createFinancialAccount(
        payload: FinancialAccountPayload
    ): Result<Account>

    suspend fun getFinancialAccount(id: Long): Result<Account>

    suspend fun updateFinancialAccount(
        id: Long,
        payload: FinancialAccountPayload
    ): Result<Account>

    suspend fun deleteFinancialAccount(id: Long): Result<Unit>
}
