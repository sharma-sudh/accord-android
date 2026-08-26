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
 * Pragmatic V1 stand-in for a real server-push (FCM) flow: since "wallet low
 * AND no task logged in 3+ days" is computed server-side (see
 * TransactionService.isWalletUnderPressure on the backend), this worker just
 * polls that result once daily and fires a local notification when it's
 * true. Swap for FCM once push infra exists — the backend query doesn't
 * need to change either way.
 */
class WalletPressureWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val app = applicationContext as AccordApplication
        if (!app.notificationTogglePrefs.walletPressureEnabled) return Result.success()

        val token = app.tokenManager.getToken() ?: return Result.success() // signed out — nothing to check

        val underPressure = app.streakRepository.checkWalletPressure("Bearer $token")
            .getOrElse { return Result.retry() }

        if (!underPressure) return Result.success()
        if (!NotificationPermissionHelper.isGranted(app)) return Result.success()

        showNotification(app)
        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context) {
        val contentIntent = PendingIntent.getActivity(
            context,
            WALLET_PRESSURE_REQUEST_CODE,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.WALLET_PRESSURE_CHANNEL_ID
        )
            // TODO: same placeholder icon as TaskReminderWorker — swap for a
            // real monochrome notification asset before shipping.
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Your wallet's running low")
            .setContentText("Your wallet's running low — done anything worth crediting?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        // Fixed ID — only one wallet-pressure nudge should ever be visible
        // at a time; a fresh daily check replaces rather than stacks it.
        NotificationManagerCompat.from(context).notify(WALLET_PRESSURE_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val WALLET_PRESSURE_NOTIFICATION_ID = 9001
        private const val WALLET_PRESSURE_REQUEST_CODE = 9001
    }
}