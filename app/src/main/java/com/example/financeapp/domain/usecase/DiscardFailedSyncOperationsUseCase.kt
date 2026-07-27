package com.example.financeapp.domain.usecase

import com.example.financeapp.domain.repository.SyncOperationsRepository
import javax.inject.Inject

class DiscardFailedSyncOperationsUseCase @Inject constructor(
    private val repository: SyncOperationsRepository
) {

    suspend operator fun invoke(): Result<Unit> = repository.discardFailedOperations()
}
