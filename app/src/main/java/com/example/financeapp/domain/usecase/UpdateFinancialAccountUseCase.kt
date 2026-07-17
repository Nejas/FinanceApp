package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.common.FinancialAccountPayload
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject

class UpdateFinancialAccountUseCase @Inject constructor(
    private val repository: FinancialAccountsRepository
) {

    suspend operator fun invoke(
        id: Long,
        payload: FinancialAccountPayload
    ): Result<FinancialAccount> {
        return repository.updateFinancialAccount(
            id = id,
            payload = payload
        )
    }
}
