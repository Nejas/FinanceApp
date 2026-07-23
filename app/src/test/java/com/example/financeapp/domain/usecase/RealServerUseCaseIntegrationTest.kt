package com.example.financeapp.domain.usecase

import com.example.financeapp.data.network.api.FinanceApiService
import com.example.financeapp.data.network.auth.AuthTokenProvider
import com.example.financeapp.data.network.auth.BearerAuthInterceptor
import com.example.financeapp.data.network.executor.NetworkRequestExecutor
import com.example.financeapp.data.remote.datasource.FinanceRemoteDataSource
import com.example.financeapp.data.remote.datasource.RetrofitFinanceRemoteDataSource
import com.example.financeapp.data.network.result.NetworkResult
import com.example.financeapp.data.network.result.RetryPolicy
import com.example.financeapp.core.network.NetworkMonitor
import com.example.financeapp.data.repository.CategoriesDataRepository
import com.example.financeapp.data.repository.FinancialAccountsDataRepository
import com.example.financeapp.data.repository.TransactionsDataRepository
import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.FinancialAccountPayload
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.TransactionPayload
import com.example.financeapp.domain.model.TransactionType
import java.io.File
import java.math.BigDecimal
import java.time.Instant
import java.util.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RealServerUseCaseIntegrationTest {

    private fun createDependencies(token: String): TestDependencies {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
        val apiService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(
                        BearerAuthInterceptor(
                            tokenProvider = object : AuthTokenProvider {
                                override fun getToken(): String = token
                            }
                        )
                    )
                    .build()
            )
            .addConverterFactory(json.asConverterFactory(CONTENT_TYPE.toMediaType()))
            .build()
            .create(FinanceApiService::class.java)

        val retryPolicy = RetryPolicy(
            maxRetries = 0,
            requestTimeoutMillis = 15_000,
            retryDelayMillis = 0
        )
        val networkDataSource = RetrofitFinanceRemoteDataSource(
            apiService = apiService,
            requestExecutor = NetworkRequestExecutor(
                retryPolicy = retryPolicy,
                networkMonitor = AlwaysOnlineNetworkMonitor,
                ioDispatcher = Dispatchers.IO
            )
        )

        val accountsRepository = FinancialAccountsDataRepository(
            networkDataSource = networkDataSource,
            defaultDispatcher = Dispatchers.Default
        )
        val categoriesRepository = CategoriesDataRepository(
            networkDataSource = networkDataSource,
            defaultDispatcher = Dispatchers.Default
        )
        val transactionsRepository = TransactionsDataRepository(
            networkDataSource = networkDataSource,
            defaultDispatcher = Dispatchers.Default
        )

        return TestDependencies(
            remoteDataSource = networkDataSource,
            createAccount = CreateFinancialAccountUseCase(accountsRepository),
            getAccount = GetFinancialAccountUseCase(accountsRepository),
            deleteAccount = DeleteFinancialAccountUseCase(accountsRepository),
            getCategories = GetCategoriesUseCase(categoriesRepository),
            createTransaction = CreateTransactionUseCase(transactionsRepository),
            getTransaction = GetTransactionUseCase(transactionsRepository),
            deleteTransaction = DeleteTransactionUseCase(transactionsRepository)
        )
    }

    @Test
    fun createGetCreateTransactionAndDelete_flowWorksOnRealServer() = runTest {
        val token = readApiToken()
        println("Token loaded: ${token.isNotBlank()}")
        assumeTrue("SHMR_API_TOKEN must be set in local.properties or environment", token.isNotBlank())

        val dependencies = createDependencies(token)
        var createdAccountId: Long? = null
        val createdTransactionIds = mutableListOf<Long>()

        try {
            printlnHeader("Initial server categories")
            val allCategoriesResult = dependencies.remoteDataSource.getCategories()
            println(allCategoriesResult.prettyString())
            val allCategories = (allCategoriesResult as? NetworkResult.Success)?.data.orEmpty()
            println("Parsed categories count: ${allCategories.size}")
            allCategories.forEach { category ->
                println(
                    "Category id=${category.id}, name=${category.name}, " +
                        "emoji=${category.emoji}, isIncome=${category.isIncome}"
                )
            }

            printlnHeader("Create account request")
            val accountPayload = FinancialAccountPayload(
                name = "Яндекс Банк",
                emoji = null,
                balance = Money(
                    amount = BigDecimal("1000.00"),
                    currency = Currency.RUB
                )
            )
            println(accountPayload)

            val account = dependencies.createAccount(
                accountPayload
            ).getOrThrow()
            createdAccountId = account.id
            printlnHeader("Created account domain result")
            println(account)

            val loadedAccount = dependencies.getAccount(account.id).getOrThrow()
            printlnHeader("Loaded account domain result")
            println(loadedAccount)
            assertEquals(account.id, loadedAccount.id)
            assertEquals(account.name, loadedAccount.name)

            val incomeCategories = dependencies.getCategories(TransactionType.INCOME).getOrThrow()
            val expenseCategories = dependencies.getCategories(TransactionType.EXPENSE).getOrThrow()
            val incomeCategory = incomeCategories.first()
            val expenseCategory = expenseCategories.first()

            printlnHeader("Income categories domain result")
            incomeCategories.forEach(::println)
            printlnHeader("Expense categories domain result")
            expenseCategories.forEach(::println)

            printlnHeader("Create income transaction")
            val incomeTransaction = dependencies.createTransaction(
                TransactionPayload(
                    accountId = account.id,
                    categoryId = incomeCategory.id,
                    amount = Money(
                        amount = BigDecimal("500.00"),
                        currency = Currency.RUB
                    ),
                    transactionDate = Instant.now(),
                    comment = "Codex integration test income"
                )
            ).getOrThrow()
            createdTransactionIds += incomeTransaction.id
            println(incomeTransaction)

            printlnHeader("Create expense transaction")
            val expenseTransaction = dependencies.createTransaction(
                TransactionPayload(
                    accountId = account.id,
                    categoryId = expenseCategory.id,
                    amount = Money(
                        amount = BigDecimal("125.50"),
                        currency = Currency.RUB
                    ),
                    transactionDate = Instant.now(),
                    comment = "Codex integration test expense"
                )
            ).getOrThrow()
            createdTransactionIds += expenseTransaction.id
            println(expenseTransaction)

            printlnHeader("Raw server transactions without filtering")
            val rawTransactionsResult = dependencies.remoteDataSource.getTransactionsByPeriod(
                accountId = account.id
            )
            println(rawTransactionsResult.prettyString())

            val allTransactions = (rawTransactionsResult as? NetworkResult.Success)?.data.orEmpty()
            printlnHeader("Transactions grouped by category")
            allTransactions
                .groupBy { transaction -> transaction.category.name }
                .forEach { (categoryName, transactions) ->
                    println("Category: $categoryName")
                    transactions.forEach { transaction ->
                        println(
                            "  id=${transaction.id}, amount=${transaction.amount}, " +
                                "isIncome=${transaction.category.isIncome}, comment=${transaction.comment}"
                        )
                    }
                }

            val loadedIncomeTransaction = dependencies.getTransaction(incomeTransaction.id).getOrThrow()
            val loadedExpenseTransaction = dependencies.getTransaction(expenseTransaction.id).getOrThrow()
            printlnHeader("Loaded transactions domain result")
            println(loadedIncomeTransaction)
            println(loadedExpenseTransaction)

            assertEquals(incomeTransaction.id, loadedIncomeTransaction.id)
            assertEquals(account.id, loadedIncomeTransaction.accountId)
            assertEquals(incomeCategory.id, loadedIncomeTransaction.categoryId)
            assertEquals(expenseTransaction.id, loadedExpenseTransaction.id)
            assertEquals(account.id, loadedExpenseTransaction.accountId)
            assertEquals(expenseCategory.id, loadedExpenseTransaction.categoryId)

            dependencies.deleteTransaction(incomeTransaction.id).getOrThrow()
            createdTransactionIds -= incomeTransaction.id
            dependencies.deleteTransaction(expenseTransaction.id).getOrThrow()
            createdTransactionIds -= expenseTransaction.id
            assertTrue(dependencies.getTransaction(incomeTransaction.id).isFailure)
            assertTrue(dependencies.getTransaction(expenseTransaction.id).isFailure)

            dependencies.deleteAccount(account.id).getOrThrow()
            createdAccountId = null
            assertTrue(dependencies.getAccount(account.id).isFailure)
        } finally {
            createdTransactionIds.toList().forEach { transactionId ->
                dependencies.deleteTransaction(transactionId)
            }
            createdAccountId?.let { accountId ->
                dependencies.deleteAccount(accountId)
            }
        }
    }

    private fun printlnHeader(title: String) {
        println("")
        println("===== $title =====")
    }

    private fun NetworkResult<*>.prettyString(): String {
        return when (this) {
            is NetworkResult.Success<*> -> "Success(data=$data)"
            is NetworkResult.HttpError -> {
                "HttpError(code=$code, message=$message, errorBody=$errorBody)"
            }
            is NetworkResult.NetworkError -> "NetworkError(throwable=$throwable)"
            is NetworkResult.TimeoutError -> "TimeoutError(throwable=$throwable)"
            is NetworkResult.SerializationError -> "SerializationError(throwable=$throwable)"
            is NetworkResult.UnknownError -> "UnknownError(throwable=$throwable)"
        }
    }

    private fun readApiToken(): String {
        val environmentToken = System.getenv("SHMR_API_TOKEN").orEmpty()
        if (environmentToken.isNotBlank()) {
            return environmentToken
        }

        val propertiesFile = File("local.properties")
        if (!propertiesFile.exists()) {
            return ""
        }
        val properties = Properties()
        propertiesFile.inputStream().use(properties::load)
        return properties.getProperty("SHMR_API_TOKEN", "")
    }

    private data class TestDependencies(
        val remoteDataSource: FinanceRemoteDataSource,
        val createAccount: CreateFinancialAccountUseCase,
        val getAccount: GetFinancialAccountUseCase,
        val deleteAccount: DeleteFinancialAccountUseCase,
        val getCategories: GetCategoriesUseCase,
        val createTransaction: CreateTransactionUseCase,
        val getTransaction: GetTransactionUseCase,
        val deleteTransaction: DeleteTransactionUseCase
    )

    private object AlwaysOnlineNetworkMonitor : NetworkMonitor {
        override val isOnline = MutableStateFlow(true)
    }

    private companion object {
        const val BASE_URL = "https://shmr-finance.ru/api/v1/"
        const val CONTENT_TYPE = "application/json"
    }
}
