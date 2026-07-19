package com.example.financeapp.data.mock

import com.example.financeapp.domain.model.FinancialAccount
import com.example.financeapp.domain.model.Money
import javax.inject.Inject

class MockFinancialAccountsDataSource @Inject constructor() {

    fun getFinancialAccounts(): List<FinancialAccount> = financialAccounts

    private companion object {
        val financialAccounts = listOf(
            FinancialAccount(
                id = MockDataIds.FinancialAccounts.YANDEX_PAY,
                name = "Яндекс Pay",
                balance = Money(amountInMinorUnits = 123_322L * 100),
                emoji = "💳"
            ),
            FinancialAccount(
                id = MockDataIds.FinancialAccounts.GAZPROMBANK,
                name = "Газпромбанк",
                balance = Money(amountInMinorUnits = 122_322L * 100),
                emoji = "🏦"
            ),
            FinancialAccount(
                id = MockDataIds.FinancialAccounts.SBERBANK,
                name = "Сбербанк",
                balance = Money(amountInMinorUnits = 122_322L * 100),
                emoji = "🏦"
            )
        )
    }
}
