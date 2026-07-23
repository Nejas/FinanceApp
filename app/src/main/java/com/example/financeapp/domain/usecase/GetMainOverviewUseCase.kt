package com.example.financeapp.domain.usecase

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.domain.model.CategorizedTransactionsOverview
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.FinancialAccountsFilter
import com.example.financeapp.domain.model.FinancialAccountsOverview
import com.example.financeapp.domain.model.MainOverviewFilter
import com.example.financeapp.domain.model.MainTransactionsOverview
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionsOverview
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.CategoriesRepository
import com.example.financeapp.domain.repository.TransactionsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class MainOverviewLoadResult(
    val accounts: Result<FinancialAccountsOverview>,
    val transactions: Result<MainTransactionsOverview>
)

class GetMainOverviewUseCase @Inject constructor(
    private val getFinancialAccountsOverview: GetFinancialAccountsOverviewUseCase,
    private val categoriesRepository: CategoriesRepository,
    private val transactionsRepository: TransactionsRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        filter: MainOverviewFilter
    ): MainOverviewLoadResult {
        return coroutineScope {
            val accountsDeferred = async {
                getFinancialAccountsOverview(
                    filter = FinancialAccountsFilter(),
                    totalCurrency = filter.currency
                )
            }
            val categoriesDeferred = async {
                categoriesRepository.getCategories()
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
                        MainTransactionsOverview(
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

            MainOverviewLoadResult(
                accounts = accountsDeferred.await(),
                transactions = transactionsDeferred.await()
            )
        }
    }

    private fun buildOverview(
        type: TransactionType,
        currency: Currency,
        accounts: List<FinancialAccount>,
        categories: List<Category>,
        transactions: List<Transaction>
    ): CategorizedTransactionsOverview {
        val categoriesById = categories.associateBy { category -> category.id }
        val typeCategories = categories.filter { category -> category.type == type }
        val typeTransactions = transactions
            .filter { transaction -> categoriesById[transaction.categoryId]?.type == type }
            .sortedByDescending { transaction -> transaction.transactionDate }

        return CategorizedTransactionsOverview(
            overview = TransactionsOverview(
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
