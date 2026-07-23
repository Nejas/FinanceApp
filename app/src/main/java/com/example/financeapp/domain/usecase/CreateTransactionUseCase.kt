package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionPayload
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject

class CreateTransactionUseCase @Inject constructor(
    private val repository: TransactionsRepository
) {

    suspend operator fun invoke(
        payload: TransactionPayload
    ): Result<Transaction> {
        return repository.createTransaction(payload)
    }
}
