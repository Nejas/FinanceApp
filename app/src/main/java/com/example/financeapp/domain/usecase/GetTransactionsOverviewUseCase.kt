package com.example.financeapp.domain.usecase

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.domain.model.FinancialAccountsFilter
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.model.TransactionsOverview
import com.example.financeapp.domain.model.TransactionsOverviewFilter
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetTransactionsOverviewUseCase @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val getFinancialAccountsOverview: GetFinancialAccountsOverviewUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        filter: TransactionsOverviewFilter
    ): Result<TransactionsOverview> {
        return suspendRunCatching {
            val accounts = getFinancialAccountsOverview(
                filter = FinancialAccountsFilter(currency = filter.currency),
                totalCurrency = filter.currency
            ).getOrThrow().accounts
            val targetAccounts = accounts.filter { account ->
                filter.accountId == null || account.id == filter.accountId
            }
            val transactions = transactionsRepository.getTransactions(
                TransactionsQuery(
                    accountIds = targetAccounts.mapTo(mutableSetOf()) { account -> account.id },
                    startDate = filter.startDate,
                    endDate = filter.endDate,
                    type = filter.type
                )
            ).getOrThrow()

            withContext(defaultDispatcher) {
                val filteredTransactions = transactions
                    .filterByCategories(filter.categoryIds)
                    .sortedByDescending { transaction -> transaction.transactionDate }

                TransactionsOverview(
                    transactions = filteredTransactions,
                    accounts = targetAccounts,
                    total = Money.sum(
                        amounts = filteredTransactions.map { transaction -> transaction.amount },
                        fallbackCurrency = filter.currency
                    )
                )
            }
        }
    }

    private fun List<Transaction>.filterByCategories(
        categoryIds: Set<Long>
    ): List<Transaction> {
        if (categoryIds.isEmpty()) return this
        return filter { transaction -> transaction.categoryId in categoryIds }
    }
}
