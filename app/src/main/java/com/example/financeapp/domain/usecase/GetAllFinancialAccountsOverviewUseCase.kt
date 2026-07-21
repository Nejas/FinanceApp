package com.example.financeapp.domain.usecase

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccountsOverview
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetAllFinancialAccountsOverviewUseCase @Inject constructor(
    private val repository: FinancialAccountsRepository,
    private val calculateMoneyTotal: CalculateMoneyTotalUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        totalCurrency: Currency
    ): Result<FinancialAccountsOverview> {
        val accounts = repository.getFinancialAccounts()
            .getOrElse { error -> return Result.failure(error) }

        return suspendRunCatching {
            withContext(defaultDispatcher) {
                FinancialAccountsOverview(
                    accounts = accounts,
                    totalBalance = calculateMoneyTotal(
                        amounts = accounts
                            .filter { account -> account.balance.currency == totalCurrency }
                            .map { account -> account.balance },
                        fallbackCurrency = totalCurrency
                    )
                )
            }
        }
    }
}
