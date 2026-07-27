package com.example.financeapp.domain.usecase

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.domain.model.AccountQuery
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionDraft
import com.example.financeapp.domain.model.TransactionSelectionCriteria
import com.example.financeapp.domain.model.TransactionSummary
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** Operations belonging to the transaction aggregate. */
class TransactionUseCases @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    private val accountUseCases: AccountUseCases,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend fun getTransaction(id: Long): Result<Transaction> =
        transactionsRepository.getTransaction(id)

    suspend fun getSummary(criteria: TransactionSelectionCriteria): Result<TransactionSummary> {
        return suspendRunCatching {
            val accounts = accountUseCases.getSummary(
                query = AccountQuery(currency = criteria.currency),
                totalCurrency = criteria.currency
            ).getOrThrow().accounts
            val targetAccounts = accounts.filter { account ->
                criteria.accountId == null || account.id == criteria.accountId
            }
            val transactions = transactionsRepository.getTransactions(
                TransactionsQuery(
                    accountIds = targetAccounts.mapTo(mutableSetOf()) { account -> account.id },
                    startDate = criteria.startDate,
                    endDate = criteria.endDate,
                    type = criteria.type
                )
            ).getOrThrow()

            withContext(defaultDispatcher) {
                val filteredTransactions = transactions
                    .filterByCategories(criteria.categoryIds)
                    .sortedByDescending { transaction -> transaction.transactionDate }

                TransactionSummary(
                    transactions = filteredTransactions,
                    accounts = targetAccounts,
                    total = Money.sum(
                        amounts = filteredTransactions.map { transaction -> transaction.amount },
                        fallbackCurrency = criteria.currency
                    )
                )
            }
        }
    }

    suspend fun create(draft: TransactionDraft): Result<Transaction> =
        transactionsRepository.createTransaction(draft)

    suspend fun update(id: Long, draft: TransactionDraft): Result<Transaction> =
        transactionsRepository.updateTransaction(id = id, payload = draft)

    suspend fun delete(id: Long): Result<Unit> = transactionsRepository.deleteTransaction(id)

    private fun List<Transaction>.filterByCategories(categoryIds: Set<Long>): List<Transaction> {
        if (categoryIds.isEmpty()) return this
        return filter { transaction -> transaction.categoryId in categoryIds }
    }
}
