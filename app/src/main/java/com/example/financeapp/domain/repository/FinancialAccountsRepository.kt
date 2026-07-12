package com.example.financeapp.domain.repository

import com.example.financeapp.domain.model.FinancialAccount

interface FinancialAccountsRepository {

    suspend fun getFinancialAccounts(): Result<List<FinancialAccount>>
}
