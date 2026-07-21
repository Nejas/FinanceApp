package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.common.FinancialAccountPayload
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFinancialAccountsUseCaseTest {

    @Test
    fun invoke_returnsAccountsAndTotalBalance() = runTest {
        val accounts = listOf(
            financialAccount(id = 1, balance = 123_322),
            financialAccount(id = 2, balance = 122_322),
            financialAccount(id = 3, balance = 122_322),
            financialAccount(id = 4, balance = 999_999, currency = Currency.USD)
        )
        val rubAccounts = accounts.filter { account -> account.balance.currency == Currency.RUB }
        val useCase = GetFinancialAccountsUseCase(
            repository = FakeFinancialAccountsRepository(accounts),
            calculateMoneyTotal = CalculateMoneyTotalUseCase(),
            defaultDispatcher = Dispatchers.Unconfined
        )

        val result = useCase(Currency.RUB).getOrThrow()

        assertEquals(rubAccounts, result.accounts)
        assertEquals(
            Money(amountInMinorUnits = 367_966L * 100),
            result.totalBalance
        )
    }

    @Test
    fun invoke_propagatesRepositoryFailure() = runTest {
        val useCase = GetFinancialAccountsUseCase(
            repository = FakeFinancialAccountsRepository(
                failure = IllegalStateException("Repository failed")
            ),
            calculateMoneyTotal = CalculateMoneyTotalUseCase(),
            defaultDispatcher = Dispatchers.Unconfined
        )

        val result = useCase(Currency.RUB)

        assertTrue(result.isFailure)
    }

    private fun financialAccount(
        id: Long,
        balance: Long,
        currency: Currency = Currency.RUB
    ) = FinancialAccount(
        id = id,
        name = "Account $id",
        balance = Money(amountInMinorUnits = balance * 100, currency = currency),
        emoji = "💳",
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
