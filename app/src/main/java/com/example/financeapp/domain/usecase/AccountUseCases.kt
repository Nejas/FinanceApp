package com.example.financeapp.domain.usecase

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.AccountDraft
import com.example.financeapp.domain.model.AccountQuery
import com.example.financeapp.domain.model.AccountSummary
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** Operations belonging to the financial-account aggregate. */
class AccountUseCases @Inject constructor(
    private val repository: FinancialAccountsRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend fun getAccount(id: Long): Result<Account> = repository.getFinancialAccount(id)

    suspend fun getSummary(
        query: AccountQuery,
        totalCurrency: Currency
    ): Result<AccountSummary> {
        val accounts = repository.getFinancialAccounts()
            .getOrElse { error -> return Result.failure(error) }

        return suspendRunCatching {
            withContext(defaultDispatcher) {
                val filteredAccounts = query.currency?.let { currency ->
                    accounts.filter { account -> account.balance.currency == currency }
                } ?: accounts

                AccountSummary(
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

    suspend fun create(draft: AccountDraft): Result<Account> =
        repository.createFinancialAccount(draft)

    suspend fun update(id: Long, draft: AccountDraft): Result<Account> =
        repository.updateFinancialAccount(id = id, payload = draft)

    suspend fun delete(id: Long): Result<Unit> = repository.deleteFinancialAccount(id)
}
