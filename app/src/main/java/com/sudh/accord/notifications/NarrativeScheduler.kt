package com.sudh.accord.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Degraded-but-workable fallback for Sunday narrative delivery: no FCM infra
 * exists yet (see backend's WeeklyNarrativeScheduler), so this polls once
 * daily for a narrative the user hasn't seen and fires a local notification
 * the first time one appears — same pattern as WalletPressureScheduler.
 */
object NarrativeScheduler {

    private const val UNIQUE_WORK_NAME = "weekly_narrative_check"

    /**
     * KEEP (not REPLACE) for the same reason as WalletPressureScheduler:
     * called on every app launch, and REPLACE would keep resetting the
     * periodic timer for a user who opens the app daily.
     */
    fun ensureScheduled(context: Context) {
        val request = PeriodicWorkRequestBuilder<NarrativeWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}