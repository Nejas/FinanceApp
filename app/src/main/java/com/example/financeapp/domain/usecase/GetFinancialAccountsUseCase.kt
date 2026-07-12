package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccountsOverview
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject

class GetFinancialAccountsUseCase @Inject constructor(
    private val repository: FinancialAccountsRepository,
    private val calculateMoneyTotal: CalculateMoneyTotalUseCase
) {

    suspend operator fun invoke(
        fallbackCurrency: Currency = Currency.RUB
    ): Result<FinancialAccountsOverview> {
        return repository.getFinancialAccounts().mapCatching { accounts ->
            FinancialAccountsOverview(
                accounts = accounts,
                totalBalance = calculateMoneyTotal(
                    amounts = accounts.map { account -> account.balance },
                    fallbackCurrency = fallbackCurrency
                )
            )
        }
    }
}
