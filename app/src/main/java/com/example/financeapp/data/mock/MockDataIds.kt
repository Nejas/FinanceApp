package com.example.financeapp.data.mock

import com.example.financeapp.domain.model.TransactionCategory

internal object MockDataIds {

    object FinancialAccounts {
        const val YANDEX_PAY = 1L
        const val GAZPROMBANK = 2L
        const val SBERBANK = 3L
    }

    object Categories {
        val STATIONERY = TransactionCategory.STATIONERY.id
        val CAFE = TransactionCategory.CAFE.id
        val FUEL = TransactionCategory.FUEL.id
        val SUBSCRIPTIONS = TransactionCategory.SUBSCRIPTIONS.id
        val REPAIRS = TransactionCategory.REPAIRS.id
        val TICKETS = TransactionCategory.TICKETS.id
        val INTERNET = TransactionCategory.INTERNET.id
        val GROCERIES = TransactionCategory.GROCERIES.id

        val SALE = TransactionCategory.SALE.id
        val TAX_REFUND = TransactionCategory.TAX_REFUND.id
        val BONUS = TransactionCategory.BONUS.id
        val FREELANCE = TransactionCategory.FREELANCE.id
        val RENT = TransactionCategory.RENT.id
        val CASHBACK = TransactionCategory.CASHBACK.id
        val GIFT = TransactionCategory.GIFT.id
    }
}
