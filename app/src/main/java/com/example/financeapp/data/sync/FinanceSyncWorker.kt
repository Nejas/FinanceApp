package com.example.financeapp.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FinanceSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val syncCoordinator: SyncCoordinator
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        return syncCoordinator.sync()
    }
}
