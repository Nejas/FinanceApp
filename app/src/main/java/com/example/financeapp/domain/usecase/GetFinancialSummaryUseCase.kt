package com.example.financeapp.domain.usecase

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.domain.model.TransactionCategorySummary
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.AccountQuery
import com.example.financeapp.domain.model.AccountSummary
import com.example.financeapp.domain.model.FinancialSummaryCriteria
import com.example.financeapp.domain.model.FinancialFlowSummary
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionSummary
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class FinancialSummaryLoadResult(
    val accounts: Result<AccountSummary>,
    val transactions: Result<FinancialFlowSummary>
)

class GetFinancialSummaryUseCase @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val categoryUseCase: CategoryUseCase,
    private val transactionsRepository: TransactionsRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        filter: FinancialSummaryCriteria
    ): FinancialSummaryLoadResult {
        return coroutineScope {
            val accountsDeferred = async {
                accountUseCases.getSummary(
                    query = AccountQuery(currency = filter.currency),
                    totalCurrency = filter.currency
                )
            }
            val categoriesDeferred = async {
                categoryUseCase.getCategories()
            }
            val transactionsDeferred = async {
                suspendRunCatching {
                    val accounts = accountsDeferred.await()
                        .getOrThrow()
                        .accounts
                        .filter { account -> account.balance.currency == filter.currency }
                    val categories = categoriesDeferred.await().getOrThrow()
                    val transactions = transactionsRepository.getTransactions(
                        TransactionsQuery(
                            accountIds = accounts.mapTo(mutableSetOf()) { account -> account.id },
                            startDate = filter.startDate,
                            endDate = filter.endDate
                        )
                    ).getOrThrow()

                    withContext(defaultDispatcher) {
                        FinancialFlowSummary(
                            expenses = buildOverview(
                                type = TransactionType.EXPENSE,
                                currency = filter.currency,
                                accounts = accounts,
                                categories = categories,
                                transactions = transactions
                            ),
                            income = buildOverview(
                                type = TransactionType.INCOME,
                                currency = filter.currency,
                                accounts = accounts,
                                categories = categories,
                                transactions = transactions
                            )
                        )
                    }
                }
            }

            FinancialSummaryLoadResult(
                accounts = accountsDeferred.await(),
                transactions = transactionsDeferred.await()
            )
        }
    }

    private fun buildOverview(
        type: TransactionType,
        currency: Currency,
        accounts: List<Account>,
        categories: List<Category>,
        transactions: List<Transaction>
    ): TransactionCategorySummary {
        val categoriesById = categories.associateBy { category -> category.id }
        val typeCategories = categories.filter { category -> category.type == type }
        val typeTransactions = transactions
            .filter { transaction -> categoriesById[transaction.categoryId]?.type == type }
            .sortedByDescending { transaction -> transaction.transactionDate }

        return TransactionCategorySummary(
            overview = TransactionSummary(
                transactions = typeTransactions,
                accounts = accounts,
                total = Money.sum(
                    amounts = typeTransactions.map { transaction -> transaction.amount },
                    fallbackCurrency = currency
                )
            ),
            categories = typeCategories
        )
    }
}
