package com.example.financeapp.domain.model

data class CategorySummary(
    val category: Category,
    val amount: Money,
    val percentage: Int
) {
    init {
        require(percentage in 0..100) {
            "Percentage must be between 0 and 100"
        }
    }
}
