package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject

class GetTransactionUseCase @Inject constructor(
    private val repository: TransactionsRepository
) {

    suspend operator fun invoke(id: Long): Result<Transaction> {
        return repository.getTransaction(id)
    }
}
