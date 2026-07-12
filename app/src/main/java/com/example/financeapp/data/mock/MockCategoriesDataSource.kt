package com.example.financeapp.data.mock

import com.example.financeapp.domain.model.Category
import com.example.financeapp.domain.model.TransactionCategory
import javax.inject.Inject

class MockCategoriesDataSource @Inject constructor() {

    fun getCategories(): List<Category> = expenseCategories + incomeCategories

    fun getExpenseCategories(): List<Category> = expenseCategories

    fun getIncomeCategories(): List<Category> = incomeCategories

    private companion object {
        val expenseCategories = listOf(
            TransactionCategory.STATIONERY.toCategory("Канцтовары"),
            TransactionCategory.CAFE.toCategory("Кафе"),
            TransactionCategory.FUEL.toCategory("Топливо"),
            TransactionCategory.SUBSCRIPTIONS.toCategory("Подписки"),
            TransactionCategory.REPAIRS.toCategory("Ремонт"),
            TransactionCategory.TICKETS.toCategory("Билеты"),
            TransactionCategory.INTERNET.toCategory("Интернет"),
            TransactionCategory.GROCERIES.toCategory("Продукты")
        )

        val incomeCategories = listOf(
            TransactionCategory.SALE.toCategory("Продажа"),
            TransactionCategory.TAX_REFUND.toCategory("Возврат налога"),
            TransactionCategory.BONUS.toCategory("Премия"),
            TransactionCategory.FREELANCE.toCategory("Фриланс"),
            TransactionCategory.RENT.toCategory("Аренда"),
            TransactionCategory.CASHBACK.toCategory("Кэшбэк"),
            TransactionCategory.GIFT.toCategory("Подарок")
        )

        private fun TransactionCategory.toCategory(name: String): Category {
            return Category(
                id = id,
                name = name,
                emoji = emoji,
                type = type,
                kind = this
            )
        }
    }
}
