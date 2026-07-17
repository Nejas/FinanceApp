package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionsRepository
) {

    suspend operator fun invoke(id: Long): Result<Unit> {
        return repository.deleteTransaction(id)
    }
}
