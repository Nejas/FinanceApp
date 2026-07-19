package com.example.financeapp.domain.model

enum class TransactionCategory(
    val id: Long,
    val emoji: String,
    val type: TransactionType
) {
    STATIONERY(1L, "✏️", TransactionType.EXPENSE),
    CAFE(2L, "☕", TransactionType.EXPENSE),
    FUEL(3L, "⛽", TransactionType.EXPENSE),
    SUBSCRIPTIONS(4L, "📱", TransactionType.EXPENSE),
    REPAIRS(5L, "🔧", TransactionType.EXPENSE),
    TICKETS(6L, "🎫", TransactionType.EXPENSE),
    INTERNET(7L, "🌐", TransactionType.EXPENSE),
    GROCERIES(8L, "🛒", TransactionType.EXPENSE),

    SALE(101L, "🛋️", TransactionType.INCOME),
    TAX_REFUND(102L, "📋", TransactionType.INCOME),
    BONUS(103L, "💼", TransactionType.INCOME),
    FREELANCE(104L, "💻", TransactionType.INCOME),
    RENT(105L, "🏠", TransactionType.INCOME),
    CASHBACK(106L, "💳", TransactionType.INCOME),
    GIFT(107L, "🎁", TransactionType.INCOME);

    companion object {
        fun fromId(id: Long): TransactionCategory? {
            return entries.firstOrNull { category -> category.id == id }
        }
    }
}

