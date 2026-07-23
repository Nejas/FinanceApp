package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.FinancialAccountsFilter
import com.example.financeapp.domain.model.FinancialAccountPayload
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFinancialAccountsOverviewUseCaseTest {

    @Test
    fun invoke_withoutCurrencyFilter_returnsAllAccountsAndRequestedCurrencyTotal() = runTest {
        val accounts = listOf(
            financialAccount(id = 1, balance = 123_322),
            financialAccount(id = 2, balance = 122_322),
            financialAccount(id = 3, balance = 999_999, currency = Currency.USD)
        )
        val useCase = createUseCase(accounts)

        val result = useCase(
            filter = FinancialAccountsFilter(),
            totalCurrency = Currency.RUB
        ).getOrThrow()

        assertEquals(accounts, result.accounts)
        assertEquals(
            Money(amountInMinorUnits = 245_644L * 100, currency = Currency.RUB),
            result.totalBalance
        )
    }

    @Test
    fun invoke_withCurrencyFilter_returnsOnlyMatchingAccounts() = runTest {
        val accounts = listOf(
            financialAccount(id = 1, balance = 123_322),
            financialAccount(id = 2, balance = 122_322),
            financialAccount(id = 3, balance = 999_999, currency = Currency.USD)
        )
        val useCase = createUseCase(accounts)

        val result = useCase(
            filter = FinancialAccountsFilter(currency = Currency.RUB),
            totalCurrency = Currency.RUB
        ).getOrThrow()

        assertEquals(listOf(1L, 2L), result.accounts.map { account -> account.id })
        assertEquals(
            Money(amountInMinorUnits = 245_644L * 100, currency = Currency.RUB),
            result.totalBalance
        )
    }

    @Test
    fun invoke_propagatesRepositoryFailure() = runTest {
        val useCase = GetFinancialAccountsOverviewUseCase(
            repository = FakeFinancialAccountsRepository(
                failure = IllegalStateException("Repository failed")
            ),
            defaultDispatcher = Dispatchers.Unconfined
        )

        val result = useCase(
            filter = FinancialAccountsFilter(),
            totalCurrency = Currency.RUB
        )

        assertTrue(result.isFailure)
    }

    private fun createUseCase(
        accounts: List<FinancialAccount>
    ): GetFinancialAccountsOverviewUseCase {
        return GetFinancialAccountsOverviewUseCase(
            repository = FakeFinancialAccountsRepository(accounts),
            defaultDispatcher = Dispatchers.Unconfined
        )
    }

    private fun financialAccount(
        id: Long,
        balance: Long,
        currency: Currency = Currency.RUB
    ) = FinancialAccount(
        id = id,
        name = "Account $id",
        balance = Money(amountInMinorUnits = balance * 100, currency = currency),
        emoji = "card",
        createdAt = Instant.parse("2026-07-20T00:00:00Z")
    )

    private class FakeFinancialAccountsRepository(
        private val accounts: List<FinancialAccount> = emptyList(),
        private val failure: Throwable? = null
    ) : FinancialAccountsRepository {

        override suspend fun getFinancialAccounts(): Result<List<FinancialAccount>> {
            return failure?.let(Result.Companion::failure)
                ?: Result.success(accounts)
        }

        override suspend fun createFinancialAccount(
            payload: FinancialAccountPayload
        ): Result<FinancialAccount> {
            return failure?.let(Result.Companion::failure)
                ?: Result.success(accounts.first())
        }

        override suspend fun getFinancialAccount(id: Long): Result<FinancialAccount> {
            return failure?.let(Result.Companion::failure)
                ?: Result.success(accounts.first { account -> account.id == id })
        }

        override suspend fun updateFinancialAccount(
            id: Long,
            payload: FinancialAccountPayload
        ): Result<FinancialAccount> {
            return getFinancialAccount(id)
        }

        override suspend fun deleteFinancialAccount(id: Long): Result<Unit> {
            return failure?.let(Result.Companion::failure)
                ?: Result.success(Unit)
        }
    }
}
