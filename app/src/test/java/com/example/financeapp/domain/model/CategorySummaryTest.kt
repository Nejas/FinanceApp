package com.example.financeapp.domain.model

import org.junit.Assert.assertThrows
import org.junit.Test

class CategorySummaryTest {

    private val category = Category(
        id = 1,
        name = "Ремонт",
        emoji = "🔧",
        type = TransactionType.EXPENSE
    )

    @Test
    fun categorySummary_rejectsPercentageOutsideValidRange() {
        assertThrows(IllegalArgumentException::class.java) {
            CategorySummary(
                category = category,
                amount = Money(amountInMinorUnits = 8_020_000),
                percentage = 101
            )
        }
    }
}
