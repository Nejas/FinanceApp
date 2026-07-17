package com.example.financeapp.domain.repository

import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.common.FinancialAccountPayload

interface FinancialAccountsRepository {

    suspend fun getFinancialAccounts(): Result<List<FinancialAccount>>

    suspend fun createFinancialAccount(
        payload: FinancialAccountPayload
    ): Result<FinancialAccount>

    suspend fun getFinancialAccount(id: Long): Result<FinancialAccount>

    suspend fun updateFinancialAccount(
        id: Long,
        payload: FinancialAccountPayload
    ): Result<FinancialAccount>

    suspend fun deleteFinancialAccount(id: Long): Result<Unit>
}
