package com.example.financeapp.data.local

import java.time.Clock
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

interface LocalTransactionIdGenerator {

    fun nextId(): Long
}

interface LocalAccountIdGenerator {

    fun nextId(): Long
}

@Singleton
class NegativeLocalTransactionIdGenerator @Inject constructor(
    clock: Clock
) : LocalTransactionIdGenerator {

    private val counter = AtomicLong(clock.millis())

    override fun nextId(): Long {
        return -counter.incrementAndGet()
    }
}

@Singleton
class NegativeLocalAccountIdGenerator @Inject constructor(
    clock: Clock
) : LocalAccountIdGenerator {

    private val counter = AtomicLong(clock.millis())

    override fun nextId(): Long {
        return -counter.incrementAndGet()
    }
}
