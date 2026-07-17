package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionFilter
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.model.common.TransactionPayload
import com.example.financeapp.domain.repository.TransactionsRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetTransactionsForDateUseCaseTest {

    @Test
    fun invoke_filtersTransactionsByDateAndCalculatesTotal() = runTest {
        val repository = FakeTransactionsRepository(
            transactions = listOf(
                transaction(id = 1, amount = 1_200, date = "2026-06-12T08:00:00Z"),
                transaction(id = 2, amount = 750, date = "2026-06-12T18:00:00Z"),
                transaction(id = 3, amount = 500, date = "2026-06-13T08:00:00Z")
            )
        )
        val useCase = GetTransactionsForDateUseCase(
            repository = repository,
            calculateMoneyTotal = CalculateMoneyTotalUseCase()
        )

        val result = useCase(
            date = LocalDate.of(2026, 6, 12),
            type = TransactionType.EXPENSE,
            zoneId = ZoneOffset.UTC
        ).getOrThrow()

        assertEquals(listOf(1L, 2L), result.transactions.map { it.id })
        assertEquals(Money(amountInMinorUnits = 1_950L * 100), result.total)
        assertEquals(TransactionType.EXPENSE, repository.requestedFilter?.type)
        assertEquals(LocalDate.of(2026, 6, 12), repository.requestedFilter?.startDate)
        assertEquals(LocalDate.of(2026, 6, 12), repository.requestedFilter?.endDate)
    }

    @Test
    fun invoke_propagatesRepositoryFailure() = runTest {
        val repository = FakeTransactionsRepository(
            failure = IllegalStateException("Repository failed")
        )
        val useCase = GetTransactionsForDateUseCase(
            repository = repository,
            calculateMoneyTotal = CalculateMoneyTotalUseCase()
        )

        val result = useCase(
            date = LocalDate.of(2026, 6, 12),
            type = TransactionType.INCOME,
            zoneId = ZoneOffset.UTC
        )

        assertTrue(result.isFailure)
    }

    private fun transaction(
        id: Long,
        amount: Long,
        date: String
    ) = Transaction(
        id = id,
        title = "Transaction $id",
        amount = Money(amountInMinorUnits = amount * 100),
        categoryId = id,
        accountId = 1,
        transactionDate = Instant.parse(date)
    )

    private class FakeTransactionsRepository(
        private val transactions: List<Transaction> = emptyList(),
        private val failure: Throwable? = null
    ) : TransactionsRepository {

        var requestedFilter: TransactionFilter? = null
            private set

        override suspend fun getTransactions(
            filter: TransactionFilter
        ): Result<List<Transaction>> {
            requestedFilter = filter
            return failure?.let(Result.Companion::failure)
                ?: Result.success(transactions)
        }

        override suspend fun createTransaction(
            payload: TransactionPayload
        ): Result<Transaction> {
            return failure?.let(Result.Companion::failure)
                ?: Result.success(transactions.first())
        }

        override suspend fun getTransaction(id: Long): Result<Transaction> {
            return failure?.let(Result.Companion::failure)
                ?: Result.success(transactions.first { transaction -> transaction.id == id })
        }

        override suspend fun updateTransaction(
            id: Long,
            payload: TransactionPayload
        ): Result<Transaction> {
            return getTransaction(id)
        }

        override suspend fun deleteTransaction(id: Long): Result<Unit> {
            return failure?.let(Result.Companion::failure)
                ?: Result.success(Unit)
        }
    }
}
