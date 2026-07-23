package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.AnalyticsFilter
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.FinancialAccountPayload
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionPayload
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.CategoriesRepository
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import com.example.financeapp.domain.repository.TransactionsRepository
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class GetAnalyticsOverviewUseCaseTest {

    @Test
    fun invoke_returnsDomainEntriesAndCategoryBreakdowns() = runTest {
        val account = FinancialAccount(
            id = 1,
            name = "Main account",
            balance = Money(amountInMinorUnits = 100_000, currency = Currency.RUB),
            emoji = "wallet",
            createdAt = Instant.parse("2026-07-01T00:00:00Z")
        )
        val food = Category(
            id = 10,
            name = "Food",
            emoji = "food",
            type = TransactionType.EXPENSE
        )
        val transport = Category(
            id = 20,
            name = "Transport",
            emoji = "transport",
            type = TransactionType.EXPENSE
        )
        val transactions = listOf(
            transaction(id = 101, categoryId = food.id, amountInMinorUnits = 7_500),
            transaction(id = 102, categoryId = transport.id, amountInMinorUnits = 2_500)
        )
        val useCase = GetAnalyticsOverviewUseCase(
            getTransactionsOverview = GetTransactionsOverviewUseCase(
                transactionsRepository = FakeTransactionsRepository(transactions),
                getFinancialAccountsOverview = GetFinancialAccountsOverviewUseCase(
                    repository = FakeFinancialAccountsRepository(listOf(account)),
                    defaultDispatcher = Dispatchers.Unconfined
                ),
                defaultDispatcher = Dispatchers.Unconfined
            ),
            categoriesRepository = FakeCategoriesRepository(listOf(food, transport)),
            defaultDispatcher = Dispatchers.Unconfined
        )

        val overview = useCase(
            AnalyticsFilter(
                startDate = LocalDate.of(2026, 7, 1),
                endDate = LocalDate.of(2026, 7, 31),
                currency = Currency.RUB
            )
        ).getOrThrow()

        assertEquals(Money(amountInMinorUnits = 10_000, currency = Currency.RUB), overview.total)
        assertEquals(listOf(food, transport), overview.availableCategories)
        assertEquals(listOf(75, 25), overview.categories.map { category -> category.sharePercent })
        assertSame(food, overview.categories.first().category)
        assertSame(transactions.first(), overview.transactions.first().transaction)
        assertSame(food, overview.transactions.first().category)
        assertSame(account, overview.transactions.first().account)
    }

    private fun transaction(
        id: Long,
        categoryId: Long,
        amountInMinorUnits: Long
    ) = Transaction(
        id = id,
        amount = Money(amountInMinorUnits = amountInMinorUnits, currency = Currency.RUB),
        categoryId = categoryId,
        accountId = 1,
        transactionDate = Instant.parse("2026-07-20T12:00:00Z")
    )

    private class FakeCategoriesRepository(
        private val categories: List<Category>
    ) : CategoriesRepository {

        override suspend fun getCategories(type: TransactionType?): Result<List<Category>> {
            return Result.success(categories.filter { category -> type == null || category.type == type })
        }
    }

    private class FakeFinancialAccountsRepository(
        private val accounts: List<FinancialAccount>
    ) : FinancialAccountsRepository {

        override suspend fun getFinancialAccounts(): Result<List<FinancialAccount>> {
            return Result.success(accounts)
        }

        override suspend fun createFinancialAccount(
            payload: FinancialAccountPayload
        ): Result<FinancialAccount> {
            return Result.success(accounts.first())
        }

        override suspend fun getFinancialAccount(id: Long): Result<FinancialAccount> {
            return Result.success(accounts.first { account -> account.id == id })
        }

        override suspend fun updateFinancialAccount(
            id: Long,
            payload: FinancialAccountPayload
        ): Result<FinancialAccount> {
            return getFinancialAccount(id)
        }

        override suspend fun deleteFinancialAccount(id: Long): Result<Unit> {
            return Result.success(Unit)
        }
    }

    private class FakeTransactionsRepository(
        private val transactions: List<Transaction>
    ) : TransactionsRepository {

        override suspend fun getTransactions(
            query: TransactionsQuery
        ): Result<List<Transaction>> {
            return Result.success(transactions.filter { transaction -> transaction.accountId in query.accountIds })
        }

        override suspend fun createTransaction(
            payload: TransactionPayload
        ): Result<Transaction> {
            return Result.success(transactions.first())
        }

        override suspend fun getTransaction(id: Long): Result<Transaction> {
            return Result.success(transactions.first { transaction -> transaction.id == id })
        }

        override suspend fun updateTransaction(
            id: Long,
            payload: TransactionPayload
        ): Result<Transaction> {
            return getTransaction(id)
        }

        override suspend fun deleteTransaction(id: Long): Result<Unit> {
            return Result.success(Unit)
        }
    }
}
