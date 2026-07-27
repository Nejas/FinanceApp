package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.FinancialAccountPayload
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject

class CreateFinancialAccountUseCase @Inject constructor(
    private val repository: FinancialAccountsRepository
) {

    suspend operator fun invoke(
        payload: FinancialAccountPayload
    ): Result<Account> {
        return repository.createFinancialAccount(payload)
    }
}
