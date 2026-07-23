package com.example.financeapp.domain.usecase

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccountsFilter
import com.example.financeapp.domain.model.FinancialAccountsOverview
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetFinancialAccountsOverviewUseCase @Inject constructor(
    private val repository: FinancialAccountsRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        filter: FinancialAccountsFilter,
        totalCurrency: Currency
    ): Result<FinancialAccountsOverview> {
        val accounts = repository.getFinancialAccounts()
            .getOrElse { error -> return Result.failure(error) }

        return suspendRunCatching {
            withContext(defaultDispatcher) {
                val filteredAccounts = filter.currency?.let { currency ->
                    accounts.filter { account -> account.balance.currency == currency }
                } ?: accounts

                FinancialAccountsOverview(
                    accounts = filteredAccounts,
                    totalBalance = Money.sum(
                        amounts = filteredAccounts
                            .filter { account -> account.balance.currency == totalCurrency }
                            .map { account -> account.balance },
                        fallbackCurrency = totalCurrency
                    )
                )
            }
        }
    }
}
