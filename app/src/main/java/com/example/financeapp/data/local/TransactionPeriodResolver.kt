package com.example.financeapp.data.local

import com.example.financeapp.domain.model.TransactionsQuery
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class TransactionPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startEpochMillis: Long,
    val endEpochMillis: Long
)

class TransactionPeriodResolver @Inject constructor(
    private val clock: Clock
) {

    fun resolve(query: TransactionsQuery): TransactionPeriod {
        val today = LocalDate.now(clock)
        val startDate = query.startDate ?: today.with(TemporalAdjusters.firstDayOfMonth())
        val endDate = query.endDate ?: today.with(TemporalAdjusters.lastDayOfMonth())

        return TransactionPeriod(
            startDate = startDate,
            endDate = endDate,
            startEpochMillis = startDate.atStartOfDay(clock.zone).toInstant().toEpochMilli(),
            endEpochMillis = endDate
                .plusDays(1)
                .atStartOfDay(clock.zone)
                .toInstant()
                .minusMillis(1)
                .toEpochMilli()
        )
    }

    fun contains(period: TransactionPeriod, instant: Instant): Boolean {
        val epochMillis = instant.toEpochMilli()
        return epochMillis in period.startEpochMillis..period.endEpochMillis
    }
}
