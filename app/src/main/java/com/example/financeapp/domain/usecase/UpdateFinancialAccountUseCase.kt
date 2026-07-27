package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.FinancialAccountPayload
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject

class UpdateFinancialAccountUseCase @Inject constructor(
    private val repository: FinancialAccountsRepository
) {

    suspend operator fun invoke(
        id: Long,
        payload: FinancialAccountPayload
    ): Result<Account> {
        return repository.updateFinancialAccount(
            id = id,
            payload = payload
        )
    }
}
