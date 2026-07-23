package com.example.financeapp.domain.repository

import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.model.TransactionPayload

interface TransactionsRepository {

    suspend fun getTransactions(query: TransactionsQuery): Result<List<Transaction>>

    suspend fun createTransaction(payload: TransactionPayload): Result<Transaction>

    suspend fun getTransaction(id: Long): Result<Transaction>

    suspend fun updateTransaction(
        id: Long,
        payload: TransactionPayload
    ): Result<Transaction>

    suspend fun deleteTransaction(id: Long): Result<Unit>
}
