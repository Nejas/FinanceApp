package com.example.financeapp.domain.repository

import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionFilter
import com.example.financeapp.domain.model.common.TransactionPayload

interface TransactionsRepository {

    suspend fun getTransactions(filter: TransactionFilter): Result<List<Transaction>>

    suspend fun createTransaction(payload: TransactionPayload): Result<Transaction>

    suspend fun getTransaction(id: Long): Result<Transaction>

    suspend fun updateTransaction(
        id: Long,
        payload: TransactionPayload
    ): Result<Transaction>

    suspend fun deleteTransaction(id: Long): Result<Unit>
}
