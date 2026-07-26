package com.example.financeapp.data.mapper

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.Money
import com.example.financeapp.domain.model.TransactionPayload
import java.math.BigDecimal
import java.time.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionMapperTest {

    @Test
    fun toRequestDto_formatsWholeAmountWithMinorUnits() {
        val request = transactionPayload(
            amount = BigDecimal("1000")
        ).toRequestDto()

        assertEquals("1000.00", request.amount)
    }

    @Test
    fun toRequestDto_keepsFractionalMinorUnits() {
        val request = transactionPayload(
            amount = BigDecimal("125.50")
        ).toRequestDto()

        assertEquals("125.50", request.amount)
    }

    @Test
    fun toRequestDto_serializesEmptyCommentWhenPayloadCommentIsNull() {
        val request = transactionPayload(
            amount = BigDecimal("1000"),
            comment = null
        ).toRequestDto()

        val encodedRequest = Json {
            explicitNulls = false
        }.encodeToString(request)

        assertEquals("", request.comment)
        assertTrue(encodedRequest.contains("\"comment\":\"\""))
    }

    private fun transactionPayload(
        amount: BigDecimal,
        comment: String? = null
    ): TransactionPayload {
        return TransactionPayload(
            accountId = 1740,
            categoryId = 9,
            amount = Money(
                amount = amount,
                currency = Currency.RUB
            ),
            transactionDate = Instant.parse("2026-07-26T21:46:00Z"),
            comment = comment
        )
    }
}
