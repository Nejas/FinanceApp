package com.example.financeapp.presentation.transactionEditor

import com.example.financeapp.MainDispatcherRule
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Account
import com.example.financeapp.domain.model.AccountDraft
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TransactionDraft
import com.example.financeapp.domain.model.TransactionsQuery
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.repository.CategoriesRepository
import com.example.financeapp.domain.repository.FinancialAccountsRepository
import com.example.financeapp.domain.repository.TransactionsRepository
import com.example.financeapp.domain.usecase.CategoryUseCase
import com.example.financeapp.domain.usecase.AccountUseCases
import com.example.financeapp.domain.usecase.TransactionUseCases
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorEffect
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorIntent
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorMode
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorReducer
import com.example.financeapp.presentation.bottomSheets.transactionEditor.TransactionEditorViewModel
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TransactionEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val category = Category(
        id = 10,
        name = "Projects",
        emoji = "tag",
        type = TransactionType.INCOME
    )
    private val account = Account(
        id = 20,
        name = "Main",
        balance = Money(BigDecimal("1000"), Currency.RUB),
        emoji = "card",
        createdAt = Instant.parse("2026-07-01T00:00:00Z")
    )
    private val clock = Clock.fixed(
        Instant.parse("2026-07-25T06:41:00Z"),
        ZoneId.of("Europe/Moscow")
    )

    @Test
    fun openCreate_setsCurrentDateTimeAndFirstAccount() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(
            TransactionEditorIntent.Open(
                TransactionEditorMode.Create(TransactionType.INCOME)
            )
        )

        val form = requireNotNull(viewModel.state.value.form)
        assertFalse(form.isLoading)
        assertEquals(LocalDate.of(2026, 7, 25), form.date)
        assertEquals(LocalTime.of(9, 41), form.time)
        assertEquals(account, form.selectedAccount)
        assertEquals(listOf(category), form.availableCategories)
    }

    @Test
    fun confirmCreate_buildsPayloadAndEmitsSavedEffect() = runTest {
        val transactionsRepository = FakeTransactionsRepository()
        val viewModel = createViewModel(transactionsRepository)

        viewModel.onIntent(
            TransactionEditorIntent.Open(
                TransactionEditorMode.Create(TransactionType.INCOME)
            )
        )
        viewModel.onIntent(TransactionEditorIntent.AmountChanged("214"))
        viewModel.onIntent(TransactionEditorIntent.CategorySelected(category.id))
        viewModel.onIntent(TransactionEditorIntent.ConfirmClicked)

        val payload = requireNotNull(transactionsRepository.createdPayload)
        assertEquals(account.id, payload.accountId)
        assertEquals(category.id, payload.categoryId)
        assertEquals(Money(BigDecimal("214"), Currency.RUB), payload.amount)
        assertEquals(Instant.parse("2026-07-25T06:41:00Z"), payload.transactionDate)
        assertNull(viewModel.state.value.form)
        assertEquals(
            TransactionEditorEffect.Saved(NEW_TRANSACTION_ID),
            viewModel.effects.first()
        )
    }

    @Test
    fun openEdit_prefillsTransactionData() = runTest {
        val existingTransaction = Transaction(
            id = 42,
            amount = Money(BigDecimal("500"), Currency.RUB),
            categoryId = category.id,
            accountId = account.id,
            transactionDate = Instant.parse("2026-07-20T17:30:00Z"),
            comment = "Existing comment"
        )
        val viewModel = createViewModel(
            transactionsRepository = FakeTransactionsRepository(
                existingTransaction = existingTransaction
            )
        )

        viewModel.onIntent(
            TransactionEditorIntent.Open(
                TransactionEditorMode.Edit(
                    transactionId = existingTransaction.id,
                    transactionType = TransactionType.INCOME
                )
            )
        )

        val form = requireNotNull(viewModel.state.value.form)
        assertFalse(form.isLoading)
        assertEquals("500", form.amount)
        assertEquals(category, form.selectedCategory)
        assertEquals(account, form.selectedAccount)
        assertEquals(LocalDate.of(2026, 7, 20), form.date)
        assertEquals(LocalTime.of(20, 30), form.time)
        assertEquals(existingTransaction.comment, form.comment)
        assertTrue(form.canConfirm)
    }

    @Test
    fun confirmEdit_updatesExistingTransaction() = runTest {
        val existingTransaction = Transaction(
            id = 42,
            amount = Money(BigDecimal("500"), Currency.RUB),
            categoryId = category.id,
            accountId = account.id,
            transactionDate = Instant.parse("2026-07-20T17:30:00Z")
        )
        val transactionsRepository = FakeTransactionsRepository(
            existingTransaction = existingTransaction
        )
        val viewModel = createViewModel(transactionsRepository)

        viewModel.onIntent(
            TransactionEditorIntent.Open(
                TransactionEditorMode.Edit(
                    transactionId = existingTransaction.id,
                    transactionType = TransactionType.INCOME
                )
            )
        )
        viewModel.onIntent(TransactionEditorIntent.AmountChanged("750"))
        viewModel.onIntent(TransactionEditorIntent.ConfirmClicked)

        assertEquals(existingTransaction.id, transactionsRepository.updatedId)
        assertEquals(
            Money(BigDecimal("750"), Currency.RUB),
            transactionsRepository.updatedPayload?.amount
        )
        assertNull(viewModel.state.value.form)
        assertEquals(
            TransactionEditorEffect.Saved(existingTransaction.id),
            viewModel.effects.first()
        )
    }

    private fun createViewModel(
        transactionsRepository: FakeTransactionsRepository = FakeTransactionsRepository()
    ): TransactionEditorViewModel {
        return TransactionEditorViewModel(
            categoryUseCase = CategoryUseCase(
                FakeCategoriesRepository(listOf(category))
            ),
            accountUseCases = AccountUseCases(
                repository = FakeFinancialAccountsRepository(listOf(account)),
                defaultDispatcher = Dispatchers.Unconfined
            ),
            transactionUseCases = TransactionUseCases(
                transactionsRepository = transactionsRepository,
                accountUseCases = AccountUseCases(
                    repository = FakeFinancialAccountsRepository(listOf(account)),
                    defaultDispatcher = Dispatchers.Unconfined
                ),
                defaultDispatcher = Dispatchers.Unconfined
            ),
            reducer = TransactionEditorReducer(),
            networkMonitor = FakeNetworkMonitor(),
            clock = clock
        )
    }

    private class FakeNetworkMonitor : NetworkMonitor {
        override val isOnline = MutableStateFlow(true)
    }

    private class FakeCategoriesRepository(
        private val categories: List<Category>
    ) : CategoriesRepository {
        override suspend fun getCategories(
            type: TransactionType?
        ): Result<List<Category>> {
            return Result.success(
                type?.let { selectedType ->
                    categories.filter { category -> category.type == selectedType }
                } ?: categories
            )
        }
    }

    private class FakeFinancialAccountsRepository(
        private val accounts: List<Account>
    ) : FinancialAccountsRepository {
        override suspend fun getFinancialAccounts() = Result.success(accounts)

        override suspend fun createFinancialAccount(
            payload: AccountDraft
        ) = Result.success(accounts.first())

        override suspend fun getFinancialAccount(id: Long) = Result.success(
            accounts.first { account -> account.id == id }
        )

        override suspend fun updateFinancialAccount(
            id: Long,
            payload: AccountDraft
        ) = getFinancialAccount(id)

        override suspend fun deleteFinancialAccount(id: Long) = Result.success(Unit)
    }

    private class FakeTransactionsRepository(
        private val existingTransaction: Transaction? = null
    ) : TransactionsRepository {
        var createdPayload: TransactionDraft? = null
        var updatedId: Long? = null
        var updatedPayload: TransactionDraft? = null

        override suspend fun getTransactions(
            query: TransactionsQuery
        ): Result<List<Transaction>> {
            return Result.success(listOfNotNull(existingTransaction))
        }

        override suspend fun createTransaction(
            payload: TransactionDraft
        ): Result<Transaction> {
            createdPayload = payload
            return Result.success(
                Transaction(
                    id = NEW_TRANSACTION_ID,
                    amount = payload.amount,
                    categoryId = payload.categoryId,
                    accountId = payload.accountId,
                    transactionDate = payload.transactionDate,
                    comment = payload.comment
                )
            )
        }

        override suspend fun getTransaction(id: Long): Result<Transaction> {
            return existingTransaction
                ?.takeIf { transaction -> transaction.id == id }
                ?.let(Result.Companion::success)
                ?: Result.failure(NoSuchElementException("Transaction $id"))
        }

        override suspend fun updateTransaction(
            id: Long,
            payload: TransactionDraft
        ): Result<Transaction> {
            updatedId = id
            updatedPayload = payload
            return Result.success(
                Transaction(
                    id = id,
                    amount = payload.amount,
                    categoryId = payload.categoryId,
                    accountId = payload.accountId,
                    transactionDate = payload.transactionDate,
                    comment = payload.comment
                )
            )
        }

        override suspend fun deleteTransaction(id: Long) = Result.success(Unit)
    }

    private companion object {
        const val NEW_TRANSACTION_ID = 77L
    }
}
