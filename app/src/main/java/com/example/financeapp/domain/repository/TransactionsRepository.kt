package com.example.financeapp.domain.repository

import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.model.TransactionDraft

interface TransactionsRepository {

    suspend fun getTransactions(query: TransactionsQuery): Result<List<Transaction>>

    suspend fun createTransaction(payload: TransactionDraft): Result<Transaction>

    suspend fun getTransaction(id: Long): Result<Transaction>

    suspend fun updateTransaction(
        id: Long,
        payload: TransactionDraft
    ): Result<Transaction>

    suspend fun deleteTransaction(id: Long): Result<Unit>
}
