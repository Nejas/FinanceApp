package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.model.Currency
import com.example.financeapp.domain.model.TransactionFilter
import com.example.financeapp.domain.model.TransactionType
import com.example.financeapp.domain.model.TransactionsOverview
import com.example.financeapp.domain.repository.TransactionsRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetTransactionsForDateUseCase @Inject constructor(
    private val repository: TransactionsRepository,
    private val calculateMoneyTotal: CalculateMoneyTotalUseCase
) {

    suspend operator fun invoke(
        date: LocalDate,
        type: TransactionType,
        accountId: Long = DEFAULT_ACCOUNT_ID,
        zoneId: ZoneId = ZoneId.systemDefault(),
        fallbackCurrency: Currency = Currency.RUB
    ): Result<TransactionsOverview> {
        val filter = TransactionFilter(
            accountId = accountId,
            startDate = date,
            endDate = date,
            type = type
        )
        return repository.getTransactions(filter).mapCatching { transactions ->
            val transactionsForDate = transactions.filter { transaction ->
                transaction.transactionDate
                    .atZone(zoneId)
                    .toLocalDate() == date
            }
            TransactionsOverview(
                transactions = transactionsForDate,
                total = calculateMoneyTotal(
                    amounts = transactionsForDate.map { transaction ->
                        transaction.amount
                    },
                    fallbackCurrency = fallbackCurrency
                )
            )
        }
    }

    private companion object {
        const val DEFAULT_ACCOUNT_ID = 1L
    }
}
