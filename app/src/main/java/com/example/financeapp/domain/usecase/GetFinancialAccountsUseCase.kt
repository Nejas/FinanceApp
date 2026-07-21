package com.example.financeapp.domain.usecase

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccountsOverview
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetFinancialAccountsUseCase @Inject constructor(
    private val repository: FinancialAccountsRepository,
    private val calculateMoneyTotal: CalculateMoneyTotalUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        currency: Currency
    ): Result<FinancialAccountsOverview> {
        val accounts = repository.getFinancialAccounts()
            .getOrElse { error -> return Result.failure(error) }

        return suspendRunCatching {
            withContext(defaultDispatcher) {
                val filteredAccounts = accounts.filter { account ->
                    account.balance.currency == currency
                }
                FinancialAccountsOverview(
                    accounts = filteredAccounts,
                    totalBalance = calculateMoneyTotal(
                        amounts = filteredAccounts.map { account -> account.balance },
                        fallbackCurrency = currency
                    )
                )
            }
        }
    }
}
