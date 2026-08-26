package com.sudh.accord.notifications

import android.Manifest
import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sudh.accord.AccordApplication
import com.sudh.accord.MainActivity

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val app = applicationContext as AccordApplication
        if (!app.notificationTogglePrefs.taskRemindersEnabled) return Result.success()

        val taskId    = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: "Task"
        val isDueToday = inputData.getBoolean(KEY_IS_DUE_TODAY, false)

        // Permission may have been revoked (or never granted) since this work
        // was scheduled — skip rather than let NotificationManagerCompat throw.
        if (!NotificationPermissionHelper.isGranted(applicationContext)) {
            return Result.success()
        }

        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            taskId.hashCode(),
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.TASK_REMINDERS_CHANNEL_ID
        )
            // TODO: swap for a proper monochrome notification icon asset —
            // this system drawable is a placeholder so notifications compile
            // and render correctly out of the box.
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(if (isDueToday) "Due today: $taskTitle" else "Due tomorrow: $taskTitle")
            .setContentText(if (isDueToday) "This task is due today." else "This task is due tomorrow.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        // Distinct notification IDs per task+moment so day-before and day-of
        // reminders for the same task don't overwrite each other.
        val notificationId = (taskId + if (isDueToday) "_today" else "_tomorrow").hashCode()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)

        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_IS_DUE_TODAY = "is_due_today"
    }
}