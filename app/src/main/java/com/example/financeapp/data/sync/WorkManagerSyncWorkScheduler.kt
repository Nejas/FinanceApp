package com.example.financeapp.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerSyncWorkScheduler @Inject constructor(
    @ApplicationContext context: Context
) : SyncWorkScheduler {

    private val workManager = WorkManager.getInstance(context)
    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun enqueueOneTimeSync() {
        val request = OneTimeWorkRequestBuilder<FinanceSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                Duration.ofSeconds(BACKOFF_SECONDS)
            )
            .build()

        workManager.enqueueUniqueWork(
            ONE_TIME_SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun enqueuePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<FinanceSyncWorker>(
            repeatInterval = Duration.ofHours(PERIODIC_SYNC_HOURS)
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                Duration.ofSeconds(BACKOFF_SECONDS)
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private companion object {
        const val ONE_TIME_SYNC_WORK_NAME = "finance_one_time_sync"
        const val PERIODIC_SYNC_WORK_NAME = "finance_periodic_sync"
        const val PERIODIC_SYNC_HOURS = 2L
        const val BACKOFF_SECONDS = 10L
    }
}
