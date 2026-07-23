package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.FinancialAccountPayload
import com.example.financeapp.domain.model.MainOverviewFilter
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionPayload
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.CategoriesRepository
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import com.example.financeapp.domain.repository.TransactionsRepository
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetMainOverviewUseCaseTest {

    @Test
    fun invoke_buildsExpenseIncomeAndAccountsOverviewFromOneAccountsLoad() = runTest {
        val accounts = listOf(
            account(id = 1, currency = Currency.RUB, balance = 1_000),
            account(id = 2, currency = Currency.USD, balance = 2_000)
        )
        val categories = listOf(
            category(id = 10, type = TransactionType.EXPENSE),
            category(id = 20, type = TransactionType.INCOME)
        )
        val transactionsRepository = FakeTransactionsRepository(
            transactions = listOf(
                transaction(id = 1, categoryId = 10, amount = 100, dateHour = 10),
                transaction(id = 2, categoryId = 20, amount = 500, dateHour = 11),
                transaction(id = 3, categoryId = 10, amount = 250, dateHour = 12)
            )
        )
        val accountsRepository = FakeFinancialAccountsRepository(accounts = accounts)
        val useCase = createUseCase(
            accountsRepository = accountsRepository,
            categoriesRepository = FakeCategoriesRepository(categories),
            transactionsRepository = transactionsRepository
        )

        val result = useCase(
            MainOverviewFilter(currency = Currency.RUB)
        )

        val accountsOverview = result.accounts.getOrThrow()
        val transactionsOverview = result.transactions.getOrThrow()

        assertEquals(1, accountsRepository.loadCount)
        assertEquals(accounts, accountsOverview.accounts)
        assertEquals(Money(amountInMinorUnits = 1_000), accountsOverview.totalBalance)
        assertEquals(
            TransactionsQuery(accountIds = setOf(1L)),
            transactionsRepository.requestedQueries.single()
        )
        assertEquals(
            listOf(3L, 1L),
            transactionsOverview.expenses.overview.transactions.map { transaction -> transaction.id }
        )
        assertEquals(
            Money(amountInMinorUnits = 350),
            transactionsOverview.expenses.overview.total
        )
        assertEquals(
            listOf(2L),
            transactionsOverview.income.overview.transactions.map { transaction -> transaction.id }
        )
        assertEquals(
            Money(amountInMinorUnits = 500),
            transactionsOverview.income.overview.total
        )
        assertEquals(listOf(10L), transactionsOverview.expenses.categories.map { category -> category.id })
        assertEquals(listOf(20L), transactionsOverview.income.categories.map { category -> category.id })
    }

    @Test
    fun invoke_preservesAccountsSuccessWhenTransactionsFail() = runTest {
        val useCase = createUseCase(
            accountsRepository = FakeFinancialAccountsRepository(
                accounts = listOf(account(id = 1, currency = Currency.RUB, balance = 1_000))
            ),
            categoriesRepository = FakeCategoriesRepository(
                categories = listOf(category(id = 10, type = TransactionType.EXPENSE))
            ),
            transactionsRepository = FakeTransactionsRepository(
                failure = IllegalStateException("Transactions failed")
            )
        )

        val result = useCase(
            MainOverviewFilter(currency = Currency.RUB)
        )

        assertTrue(result.accounts.isSuccess)
        assertTrue(result.transactions.isFailure)
    }

    @Test
    fun invoke_doesNotRequestTransactionsWhenAccountsFail() = runTest {
        val transactionsRepository = FakeTransactionsRepository()
        val useCase = createUseCase(
            accountsRepository = FakeFinancialAccountsRepository(
                failure = IllegalStateException("Accounts failed")
            ),
            categoriesRepository = FakeCategoriesRepository(),
            transactionsRepository = transactionsRepository
        )

        val result = useCase(
            MainOverviewFilter(currency = Currency.RUB)
        )

        assertTrue(result.accounts.isFailure)
        assertTrue(result.transactions.isFailure)
        assertTrue(transactionsRepository.requestedQueries.isEmpty())
    }

    private fun createUseCase(
        accountsRepository: FinancialAccountsRepository,
        categoriesRepository: CategoriesRepository,
        transactionsRepository: TransactionsRepository
    ): GetMainOverviewUseCase {
        return GetMainOverviewUseCase(
            getFinancialAccountsOverview = GetFinancialAccountsOverviewUseCase(
                repository = accountsRepository,
                defaultDispatcher = Dispatchers.Unconfined
            ),
            categoriesRepository = categoriesRepository,
            transactionsRepository = transactionsRepository,
            defaultDispatcher = Dispatchers.Unconfined
        )
    }

    private fun account(
        id: Long,
        currency: Currency,
        balance: Long
    ) = FinancialAccount(
        id = id,
        name = "Account $id",
        balance = Money(amountInMinorUnits = balance, currency = currency),
        emoji = "card",
        createdAt = Instant.parse("2026-07-20T00:00:00Z")
    )

    private fun category(
        id: Long,
        type: TransactionType
    ) = Category(
        id = id,
        name = "Category $id",
        emoji = "category",
        type = type
    )

    private fun transaction(
        id: Long,
        categoryId: Long,
        amount: Long,
        dateHour: Int
    ) = Transaction(
        id = id,
        amount = Money(amountInMinorUnits = amount),
        categoryId = categoryId,
        accountId = 1,
        transactionDate = Instant.parse("2026-07-20T${dateHour}:00:00Z")
    )

    private class FakeFinancialAccountsRepository(
        private val accounts: List<FinancialAccount> = emptyList(),
        private val failure: Throwable? = null
    ) : FinancialAccountsRepository {

        var loadCount: Int = 0
            private set

        override suspend fun getFinancialAccounts(): Result<List<FinancialAccount>> {
            loadCount += 1
            return failure?.let(Result.Companion::failure)
                ?: Result.success(accounts)
        }

        override suspend fun createFinancialAccount(
            payload: FinancialAccountPayload
        ): Result<FinancialAccount> = Result.success(accounts.first())

        override suspend fun getFinancialAccount(id: Long): Result<FinancialAccount> {
            return Result.success(accounts.first { account -> account.id == id })
        }

        override suspend fun updateFinancialAccount(
            id: Long,
            payload: FinancialAccountPayload
        ): Result<FinancialAccount> = getFinancialAccount(id)

        override suspend fun deleteFinancialAccount(id: Long): Result<Unit> = Result.success(Unit)
    }

    private class FakeCategoriesRepository(
        private val categories: List<Category> = emptyList(),
        private val failure: Throwable? = null
    ) : CategoriesRepository {

        override suspend fun getCategories(
            type: TransactionType?
        ): Result<List<Category>> {
            return failure?.let(Result.Companion::failure)
                ?: Result.success(
                    type?.let { requestedType ->
                        categories.filter { category -> category.type == requestedType }
                    } ?: categories
                )
        }
    }

    private class FakeTransactionsRepository(
        private val transactions: List<Transaction> = emptyList(),
        private val failure: Throwable? = null
    ) : TransactionsRepository {

        val requestedQueries = mutableListOf<TransactionsQuery>()

        override suspend fun getTransactions(
            query: TransactionsQuery
        ): Result<List<Transaction>> {
            requestedQueries += query
            return failure?.let(Result.Companion::failure)
                ?: Result.success(transactions)
        }

        override suspend fun createTransaction(
            payload: TransactionPayload
        ): Result<Transaction> = Result.success(transactions.first())

        override suspend fun getTransaction(id: Long): Result<Transaction> {
            return Result.success(transactions.first { transaction -> transaction.id == id })
        }

        override suspend fun updateTransaction(
            id: Long,
            payload: TransactionPayload
        ): Result<Transaction> = getTransaction(id)

        override suspend fun deleteTransaction(id: Long): Result<Unit> = Result.success(Unit)
    }
}
