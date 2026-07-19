package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.repository.FinancialAccountsRepository
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
            financialAccount(id = 3, balance = 122_322)
        )
        val useCase = GetFinancialAccountsUseCase(
            repository = FakeFinancialAccountsRepository(accounts),
            calculateMoneyTotal = CalculateMoneyTotalUseCase()
        )

        val result = useCase().getOrThrow()

        assertEquals(accounts, result.accounts)
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
            calculateMoneyTotal = CalculateMoneyTotalUseCase()
        )

        val result = useCase()

        assertTrue(result.isFailure)
    }

    private fun financialAccount(
        id: Long,
        balance: Long
    ) = FinancialAccount(
        id = id,
        name = "Account $id",
        balance = Money(amountInMinorUnits = balance * 100),
        emoji = "💳"
    )

    private class FakeFinancialAccountsRepository(
        private val accounts: List<FinancialAccount> = emptyList(),
        private val failure: Throwable? = null
    ) : FinancialAccountsRepository {

        override suspend fun getFinancialAccounts(): Result<List<FinancialAccount>> {
            return failure?.let(Result.Companion::failure)
                ?: Result.success(accounts)
        }
    }
}
