package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject

class DeleteFinancialAccountUseCase @Inject constructor(
    private val repository: FinancialAccountsRepository
) {

    suspend operator fun invoke(id: Long): Result<Unit> {
        return repository.deleteFinancialAccount(id)
    }
}
