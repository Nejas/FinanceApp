package com.example.financeapp.data.network

import com.example.financeapp.data.network.api.FinanceApiService
import com.example.financeapp.data.network.auth.AuthTokenProvider
import com.example.financeapp.data.network.auth.BearerAuthInterceptor
import java.io.File
import java.util.Properties
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class FinanceApiRealServerSmokeTest {

    private val apiService: FinanceApiService by lazy {
        val token = readApiToken()
        assumeTrue("SHMR_API_TOKEN must be set in local.properties", token.isNotBlank())

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(
                BearerAuthInterceptor(
                    tokenProvider = object : AuthTokenProvider {
                        override fun getToken(): String = token
                    }
                )
            )
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(CONTENT_TYPE.toMediaType()))
            .build()
            .create(FinanceApiService::class.java)
    }

    @Test
    fun getCategories_returnsCategoriesFromRealServer() = runTest {
        val categories = apiService.getCategories()

        assertTrue(categories.isNotEmpty())
        categories.forEach { category ->
            assertTrue(category.id > 0)
            assertTrue(category.name.isNotBlank())
            assertTrue(category.emoji.isNotBlank())
        }
    }

    @Test
    fun getAccounts_returnsAccountsFromRealServer() = runTest {
        val accounts = apiService.getAccounts()

        assertNotNull(accounts)
        accounts.forEach { account ->
            assertTrue(account.id > 0)
            assertTrue(account.name.isNotBlank())
            assertTrue(account.emoji.isNotBlank())
            assertTrue(account.balance.isNotBlank())
            assertTrue(account.currency.isNotBlank())
        }
    }

    private fun readApiToken(): String {
        val propertiesFile = File("local.properties")
        if (!propertiesFile.exists()) {
            return ""
        }
        val properties = Properties()
        propertiesFile.inputStream().use(properties::load)
        return properties.getProperty("SHMR_API_TOKEN", "")
    }

    private companion object {
        const val BASE_URL = "https://shmr-finance.ru/api/v1/"
        const val CONTENT_TYPE = "application/json"
    }
}
