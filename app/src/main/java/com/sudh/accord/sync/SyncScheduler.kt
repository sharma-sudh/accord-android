package com.sudh.accord.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {

    private const val IMMEDIATE_WORK_NAME = "accord_sync_now"
    private const val PERIODIC_WORK_NAME = "accord_sync_periodic"

    private val syncConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    // Fired right after any local mutation (task/transaction create,
    // complete, delete). APPEND_OR_REPLACE is deliberate: several mutations
    // made in quick succession (e.g. completing three tasks back to back)
    // collapse into one worker run rather than queueing duplicates — the
    // worker always syncs whatever's pending at the time it actually runs,
    // whether offline now and deferred, or online and immediate.
    fun enqueueNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(syncConstraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    // Safety net for pending rows that could've been missed by the immediate
    // trigger (e.g. process death before WorkManager persisted the request).
    // KEEP so re-calling this on every app launch doesn't reset the timer.
    fun ensurePeriodicSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(syncConstraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}