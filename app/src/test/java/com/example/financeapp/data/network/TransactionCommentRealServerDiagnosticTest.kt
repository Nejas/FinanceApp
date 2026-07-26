package com.example.financeapp.data.network

import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Properties
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assume.assumeTrue
import org.junit.Test

class TransactionCommentRealServerDiagnosticTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val client = OkHttpClient()

    @Test
    fun createTransaction_comparesMissingNullAndEmptyCommentOnRealServer() {
        val token = readApiToken()
        assumeTrue("SHMR_API_TOKEN must be set in local.properties or environment", token.isNotBlank())

        var accountId: Long? = null
        val createdTransactionIds = mutableListOf<Long>()

        try {
            val categories = getCategories(token)
            val expenseCategoryId = categories
                .first { category -> !category["isIncome"]!!.jsonPrimitive.boolean }["id"]!!
                .jsonPrimitive
                .content
                .toLong()
            accountId = createAccount(token)
            val transactionDate = Instant.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .toString()

            listOf(
                CommentCase(
                    name = "missing",
                    body = transactionBody(
                        accountId = accountId,
                        categoryId = expenseCategoryId,
                        transactionDate = transactionDate,
                        commentJson = null
                    )
                ),
                CommentCase(
                    name = "explicit-null",
                    body = transactionBody(
                        accountId = accountId,
                        categoryId = expenseCategoryId,
                        transactionDate = transactionDate,
                        commentJson = "null"
                    )
                ),
                CommentCase(
                    name = "empty-string",
                    body = transactionBody(
                        accountId = accountId,
                        categoryId = expenseCategoryId,
                        transactionDate = transactionDate,
                        commentJson = "\"\""
                    )
                )
            ).forEach { commentCase ->
                val response = postTransaction(token, commentCase.body)
                println(
                    "comment=${commentCase.name}; " +
                        "code=${response.code}; body=${response.body}"
                )
                response.createdTransactionIdOrNull()?.let { transactionId ->
                    createdTransactionIds += transactionId
                }
            }
        } finally {
            createdTransactionIds.forEach { transactionId ->
                delete(token, "transactions/$transactionId")
            }
            accountId?.let { id ->
                delete(token, "accounts/$id")
            }
        }
    }

    private fun getCategories(token: String): List<JsonObject> {
        val response = execute(
            token = token,
            path = "categories",
            method = Method.Get
        )
        check(response.code in SUCCESS_CODES) {
            "Failed to load categories: code=${response.code}; body=${response.body}"
        }
        return (json.parseToJsonElement(response.body) as JsonArray)
            .map { element -> element.jsonObject }
    }

    private fun createAccount(token: String): Long {
        val response = execute(
            token = token,
            path = "accounts",
            method = Method.Post(
                body = """
                    {
                      "name": "Codex comment diagnostic ${System.currentTimeMillis()}",
                      "emoji": "C",
                      "balance": "10000.00",
                      "currency": "RUB"
                    }
                """.trimIndent()
            )
        )
        check(response.code in SUCCESS_CODES) {
            "Failed to create account: code=${response.code}; body=${response.body}"
        }
        return json.parseToJsonElement(response.body)
            .jsonObject["id"]!!
            .jsonPrimitive
            .content
            .toLong()
    }

    private fun postTransaction(
        token: String,
        body: String
    ): ResponseSnapshot {
        return execute(
            token = token,
            path = "transactions",
            method = Method.Post(body)
        )
    }

    private fun transactionBody(
        accountId: Long,
        categoryId: Long,
        transactionDate: String,
        commentJson: String?
    ): String {
        val commentProperty = commentJson?.let { value ->
            """,
              "comment": $value"""
        }.orEmpty()
        return """
            {
              "accountId": $accountId,
              "categoryId": $categoryId,
              "amount": "1000.00",
              "transactionDate": "$transactionDate"$commentProperty
            }
        """.trimIndent()
    }

    private fun delete(
        token: String,
        path: String
    ) {
        val response = execute(
            token = token,
            path = path,
            method = Method.Delete
        )
        println("cleanup DELETE /$path; code=${response.code}; body=${response.body}")
    }

    private fun execute(
        token: String,
        path: String,
        method: Method
    ): ResponseSnapshot {
        val requestBuilder = Request.Builder()
            .url(BASE_URL + path)
            .header("Authorization", "Bearer $token")

        when (method) {
            Method.Get -> requestBuilder.get()
            Method.Delete -> requestBuilder.delete()
            is Method.Post -> requestBuilder.post(
                method.body.toRequestBody(CONTENT_TYPE.toMediaType())
            )
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            return ResponseSnapshot(
                code = response.code,
                body = response.body?.string().orEmpty()
            )
        }
    }

    private fun ResponseSnapshot.createdTransactionIdOrNull(): Long? {
        if (code !in SUCCESS_CODES) return null
        return runCatching {
            json.parseToJsonElement(body)
                .jsonObject["id"]!!
                .jsonPrimitive
                .content
                .toLong()
        }.getOrNull()
    }

    private fun readApiToken(): String {
        val environmentToken = System.getenv("SHMR_API_TOKEN").orEmpty()
        if (environmentToken.isNotBlank()) {
            return environmentToken
        }

        val propertiesFile = listOf(
            File("local.properties"),
            File("../local.properties")
        ).firstOrNull { file -> file.exists() } ?: return ""
        val properties = Properties()
        propertiesFile.inputStream().use(properties::load)
        return properties.getProperty("SHMR_API_TOKEN", "")
    }

    private data class CommentCase(
        val name: String,
        val body: String
    )

    private data class ResponseSnapshot(
        val code: Int,
        val body: String
    )

    private sealed interface Method {
        data object Get : Method
        data object Delete : Method
        data class Post(val body: String) : Method
    }

    private companion object {
        const val BASE_URL = "https://shmr-finance.ru/api/v1/"
        const val CONTENT_TYPE = "application/json"
        val SUCCESS_CODES = 200..299
    }
}
