package com.example.financeapp.domain.repository

import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionType

interface TransactionsRepository {

    suspend fun getTransactions(type: TransactionType): Result<List<Transaction>>
}
