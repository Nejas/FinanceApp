package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject

class GetFinancialAccountUseCase @Inject constructor(
    private val repository: FinancialAccountsRepository
) {

    suspend operator fun invoke(id: Long): Result<FinancialAccount> {
        return repository.getFinancialAccount(id)
    }
}
