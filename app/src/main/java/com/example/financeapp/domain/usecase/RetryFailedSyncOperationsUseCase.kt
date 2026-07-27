package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.repository.SyncOperationsRepository
import javax.inject.Inject

class RetryFailedSyncOperationsUseCase @Inject constructor(
    private val repository: SyncOperationsRepository
) {

    suspend operator fun invoke(): Result<Unit> = repository.retryFailedOperations()
}
