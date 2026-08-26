package com.sudh.accord.notifications

import android.Manifest
import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sudh.accord.AccordApplication
import com.sudh.accord.MainActivity

/**
 * Pragmatic V1 stand-in for a real server-push (FCM) flow, same rationale as
 * WalletPressureWorker: polls GET /api/v1/narrative/latest once daily and
 * fires a local notification the first time it sees a narrative for a week
 * it hasn't already notified about. Swap for FCM once push infra exists —
 * the backend generation/storage side doesn't need to change either way.
 */
class NarrativeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val app = applicationContext as AccordApplication
        if (!app.notificationTogglePrefs.sundayNarrativeEnabled) return Result.success()

        val token = app.tokenManager.getToken() ?: return Result.success() // signed out — nothing to check

        val narrative = app.narrativeRepository.getLatestNarrative("Bearer $token")
            .getOrElse { return Result.retry() }
            ?: return Result.success() // no narrative generated yet

        val prefs = NarrativePrefs(app)
        if (narrative.weekStartDate == prefs.lastNotifiedWeekStartDate) {
            return Result.success() // already notified for this week
        }

        if (!NotificationPermissionHelper.isGranted(app)) return Result.success()

        showNotification(app, narrative.narrative)
        prefs.lastNotifiedWeekStartDate = narrative.weekStartDate
        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, narrativeText: String) {
        val contentIntent = PendingIntent.getActivity(
            context,
            WEEKLY_NARRATIVE_REQUEST_CODE,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.WEEKLY_NARRATIVE_CHANNEL_ID
        )
            // TODO: same placeholder icon as TaskReminderWorker/WalletPressureWorker
            // — swap for a real monochrome notification asset before shipping.
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Your week, in one line")
            .setContentText(narrativeText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(narrativeText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        // Fixed ID — only one weekly-narrative notification should ever be
        // visible at a time.
        NotificationManagerCompat.from(context).notify(WEEKLY_NARRATIVE_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val WEEKLY_NARRATIVE_NOTIFICATION_ID = 9002
        private const val WEEKLY_NARRATIVE_REQUEST_CODE = 9002
    }
}