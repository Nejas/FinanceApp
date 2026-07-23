package com.example.financeapp.domain.usecase

import com.example.financeapp.core.coroutines.DefaultDispatcher
import com.example.financeapp.core.coroutines.suspendRunCatching
import com.example.financeapp.domain.model.AnalyticsCategoryBreakdown
import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.AnalyticsOverview
import com.example.financeapp.domain.model.AnalyticsTransactionEntry
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionsOverviewFilter
import com.example.financeapp.domain.repository.CategoriesRepository
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetAnalyticsOverviewUseCase @Inject constructor(
    private val getTransactionsOverview: GetTransactionsOverviewUseCase,
    private val categoriesRepository: CategoriesRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        filter: AnalyticsFilter
    ): Result<AnalyticsOverview> {
        return suspendRunCatching {
            val categories = categoriesRepository.getCategories(filter.type).getOrThrow()
            val transactionsOverview = getTransactionsOverview(
                filter = filter.toTransactionsOverviewFilter().copy(categoryIds = emptySet())
            ).getOrThrow()

            withContext(defaultDispatcher) {
                val categoriesById = categories.associateBy { category -> category.id }
                val accountsById = transactionsOverview.accounts.associateBy { account -> account.id }
                val filteredTransactions = transactionsOverview.transactions.filterByCategories(filter.categoryIds)
                val total = Money.sum(
                    amounts = filteredTransactions.map { transaction -> transaction.amount },
                    fallbackCurrency = filter.currency
                )

                AnalyticsOverview(
                    total = total,
                    categories = filteredTransactions.toCategoryBreakdowns(
                        categoriesById = categoriesById,
                        total = total,
                        fallbackCurrency = filter.currency
                    ),
                    availableCategories = transactionsOverview.transactions.toAvailableCategories(
                        categoriesById = categoriesById,
                        fallbackCurrency = filter.currency
                    ),
                    transactions = filteredTransactions.map { transaction ->
                        transaction.toAnalyticsTransactionEntry(
                            category = categoriesById[transaction.categoryId],
                            account = accountsById[transaction.accountId]
                        )
                    },
                    filter = filter
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

    private fun List<Transaction>.toAvailableCategories(
        categoriesById: Map<Long, Category>,
        fallbackCurrency: Currency
    ): List<Category> {
        return groupBy { transaction -> transaction.categoryId }
            .mapNotNull { (categoryId, transactions) ->
                categoriesById[categoryId]?.let { category ->
                    val amount = Money.sum(
                        amounts = transactions.map { transaction -> transaction.amount },
                        fallbackCurrency = fallbackCurrency
                    )
                    category to amount
                }
            }
            .sortedByDescending { (_, amount) -> amount.amount }
            .map { (category, _) -> category }
            .toList()
    }

    private fun AnalyticsFilter.toTransactionsOverviewFilter(): TransactionsOverviewFilter {
        return TransactionsOverviewFilter(
            accountId = accountId,
            startDate = startDate,
            endDate = endDate,
            type = type,
            currency = currency,
            categoryIds = categoryIds
        )
    }

    private fun List<Transaction>.toCategoryBreakdowns(
        categoriesById: Map<Long, Category>,
        total: Money,
        fallbackCurrency: Currency
    ): List<AnalyticsCategoryBreakdown> {
        return groupBy { transaction -> transaction.categoryId }
            .map { (categoryId, transactions) ->
                val category = categoriesById[categoryId]
                val amount = Money.sum(
                    amounts = transactions.map { transaction -> transaction.amount },
                    fallbackCurrency = fallbackCurrency
                )
                AnalyticsCategoryBreakdown(
                    categoryId = categoryId,
                    category = category,
                    amount = amount,
                    sharePercent = amount.percentOf(total)
                )
            }
            .sortedByDescending { category -> category.amount.amount }
    }

    private fun Transaction.toAnalyticsTransactionEntry(
        category: Category?,
        account: FinancialAccount?
    ): AnalyticsTransactionEntry {
        return AnalyticsTransactionEntry(
            transaction = this,
            category = category,
            account = account
        )
    }

    private fun Money.percentOf(total: Money): Int {
        if (total.amount.compareTo(BigDecimal.ZERO) == 0) return 0
        return amount
            .multiply(PercentScale)
            .divide(total.amount, PercentRoundingScale, RoundingMode.HALF_UP)
            .setScale(0, RoundingMode.HALF_UP)
            .toInt()
    }

    private companion object {
        val PercentScale: BigDecimal = BigDecimal.valueOf(100)
        const val PercentRoundingScale = 2
    }
}
