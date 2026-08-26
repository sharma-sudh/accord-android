package com.sudh.accord.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WalletPressureScheduler {

    private const val UNIQUE_WORK_NAME = "wallet_pressure_check"

    /**
     * KEEP (not REPLACE) is deliberate: this is called on every app launch
     * from AccordApplication.onCreate, and re-enqueuing with REPLACE would
     * reset the periodic timer each time, so it could get pushed out
     * indefinitely for a user who opens the app daily anyway.
     */
    fun ensureScheduled(context: Context) {
        val request = PeriodicWorkRequestBuilder<WalletPressureWorker>(1, TimeUnit.DAYS)
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