package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionFilter
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionsRepository
) {

    suspend operator fun invoke(
        filter: TransactionFilter
    ): Result<List<Transaction>> {
        return repository.getTransactions(filter)
    }
}
