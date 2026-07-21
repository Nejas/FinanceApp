package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionFilter
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.model.TransactionsOverviewFilter
import com.example.financeapp.domain.model.common.FinancialAccountPayload
import com.example.financeapp.domain.model.common.TransactionPayload
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import com.example.financeapp.domain.repository.TransactionsRepository
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTransactionsOverviewUseCaseTest {

    @Test
    fun invoke_loadsTransactionsForMatchingCurrencyAccountsWithoutDefaultStartDate() = runTest {
        val accounts = listOf(
            financialAccount(id = 1, currency = Currency.RUB, createdAt = "2026-06-01T00:00:00Z"),
            financialAccount(id = 2, currency = Currency.USD, createdAt = "2026-06-10T00:00:00Z"),
            financialAccount(id = 3, currency = Currency.RUB, createdAt = "2026-07-01T00:00:00Z")
        )
        val transactionsRepository = FakeTransactionsRepository(
            transactionsByAccountId = mapOf(
                1L to listOf(
                    transaction(
                        id = 11,
                        accountId = 1,
                        categoryId = 100,
                        amount = 500,
                        date = "2026-07-20T12:00:00Z"
                    )
                ),
                3L to listOf(
                    transaction(
                        id = 31,
                        accountId = 3,
                        categoryId = 100,
                        amount = 700,
                        date = "2026-07-20T13:00:00Z"
                    ),
                    transaction(
                        id = 32,
                        accountId = 3,
                        categoryId = 200,
                        amount = 300,
                        date = "2026-07-20T14:00:00Z"
                    )
                )
            )
        )
        val useCase = GetTransactionsOverviewUseCase(
            transactionsRepository = transactionsRepository,
            getFinancialAccounts = GetFinancialAccountsUseCase(
                repository = FakeFinancialAccountsRepository(accounts),
                calculateMoneyTotal = CalculateMoneyTotalUseCase(),
                defaultDispatcher = Dispatchers.Unconfined
            ),
            calculateMoneyTotal = CalculateMoneyTotalUseCase(),
            defaultDispatcher = Dispatchers.Unconfined
        )

        val result = useCase(
            filter = TransactionsOverviewFilter(
                endDate = LocalDate.of(2026, 7, 20),
                type = TransactionType.EXPENSE,
                currency = Currency.RUB,
                categoryIds = setOf(100)
            )
        ).getOrThrow()

        assertEquals(listOf(1L, 3L), result.accounts.map { account -> account.id })
        assertEquals(listOf(31L, 11L), result.transactions.map { transaction -> transaction.id })
        assertEquals(Money(amountInMinorUnits = 1_200L * 100, currency = Currency.RUB), result.total)
        assertEquals(
            listOf(
                TransactionFilter(
                    accountId = 1,
                    startDate = null,
                    endDate = LocalDate.of(2026, 7, 20),
                    type = TransactionType.EXPENSE
                ),
                TransactionFilter(
                    accountId = 3,
                    startDate = null,
                    endDate = LocalDate.of(2026, 7, 20),
                    type = TransactionType.EXPENSE
                )
            ),
            transactionsRepository.requestedFilters
        )
    }

    private fun financialAccount(
        id: Long,
        currency: Currency,
        createdAt: String
    ) = FinancialAccount(
        id = id,
        name = "Account $id",
        balance = Money(amountInMinorUnits = 0, currency = currency),
        emoji = "💳",
        createdAt = Instant.parse(createdAt)
    )

    private fun transaction(
        id: Long,
        accountId: Long,
        categoryId: Long,
        amount: Long,
        date: String
    ) = Transaction(
        id = id,
        title = "Transaction $id",
        amount = Money(amountInMinorUnits = amount * 100, currency = Currency.RUB),
        categoryId = categoryId,
        accountId = accountId,
        transactionDate = Instant.parse(date)
    )

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
        private val transactionsByAccountId: Map<Long, List<Transaction>>
    ) : TransactionsRepository {

        val requestedFilters = mutableListOf<TransactionFilter>()

        override suspend fun getTransactions(
            filter: TransactionFilter
        ): Result<List<Transaction>> {
            requestedFilters += filter
            return Result.success(transactionsByAccountId[filter.accountId].orEmpty())
        }

        override suspend fun createTransaction(
            payload: TransactionPayload
        ): Result<Transaction> {
            return Result.success(transactionsByAccountId.values.flatten().first())
        }

        override suspend fun getTransaction(id: Long): Result<Transaction> {
            return Result.success(
                transactionsByAccountId.values.flatten().first { transaction -> transaction.id == id }
            )
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
