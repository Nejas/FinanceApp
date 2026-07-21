package com.example.financeapp.domain.usecase

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionFilter
import com.example.financeapp.domain.model.TransactionsOverview
import com.example.financeapp.domain.model.TransactionsOverviewFilter
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class GetTransactionsOverviewUseCase @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val getFinancialAccounts: GetFinancialAccountsUseCase,
    private val calculateMoneyTotal: CalculateMoneyTotalUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        filter: TransactionsOverviewFilter
    ): Result<TransactionsOverview> {
        return suspendRunCatching {
            coroutineScope {
                val accounts = getFinancialAccounts(filter.currency).getOrThrow().accounts
                val targetAccounts = accounts.targetAccounts(filter)

                val transactions = targetAccounts.map { account ->
                    async {
                        transactionsRepository.getTransactions(
                            TransactionFilter(
                                accountId = account.id,
                                startDate = filter.startDate,
                                endDate = filter.endDate,
                                type = filter.type
                            )
                        ).getOrThrow()
                    }
                }
                    .awaitAll()
                    .flatten()

                withContext(defaultDispatcher) {
                    val filteredTransactions = transactions
                        .filterByCategories(filter.categoryIds)
                        .sortedByDescending { transaction -> transaction.transactionDate }

                    TransactionsOverview(
                        transactions = filteredTransactions,
                        accounts = targetAccounts,
                        total = calculateMoneyTotal(
                            amounts = filteredTransactions.map { transaction -> transaction.amount },
                            fallbackCurrency = filter.currency
                        )
                    )
                }
            }
        }
    }

    private fun List<FinancialAccount>.targetAccounts(
        filter: TransactionsOverviewFilter
    ): List<FinancialAccount> {
        return asSequence()
            .filter { account ->
                filter.accountId == null || account.id == filter.accountId
            }
            .toList()
    }

    private fun List<Transaction>.filterByCategories(
        categoryIds: Set<Long>
    ): List<Transaction> {
        if (categoryIds.isEmpty()) return this
        return filter { transaction -> transaction.categoryId in categoryIds }
    }
}
