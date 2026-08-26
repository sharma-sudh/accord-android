package com.sudh.accord.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * dueDate is the "yyyy-MM-dd" ISO string the backend expects (see
 * AddTaskSheet's toIsoDateString/LocalDate.parse comment) — parsed here in
 * the device's own zone since these are local wall-clock reminders.
 */
object TaskReminderScheduler {

    private val REMINDER_TIME = LocalTime.of(9, 0)

    fun schedule(context: Context, taskId: String, taskTitle: String, dueDate: String) {
        val dueLocalDate = try {
            LocalDate.parse(dueDate)
        } catch (e: Exception) {
            return // malformed/unexpected date — don't schedule against it
        }

        scheduleOne(context, taskId, taskTitle, dueLocalDate.minusDays(1), isDueToday = false, suffix = "day_before")
        scheduleOne(context, taskId, taskTitle, dueLocalDate, isDueToday = true, suffix = "day_of")
    }

    private fun scheduleOne(
        context: Context,
        taskId: String,
        taskTitle: String,
        triggerDate: LocalDate,
        isDueToday: Boolean,
        suffix: String
    ) {
        val triggerMillis = triggerDate.atTime(REMINDER_TIME)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val delayMillis = triggerMillis - System.currentTimeMillis()

        // Trigger time already passed (e.g. task created the same day it's
        // due, or the day after) — nothing meaningful to schedule.
        if (delayMillis < 0) return

        val data = Data.Builder()
            .putString(TaskReminderWorker.KEY_TASK_ID, taskId)
            .putString(TaskReminderWorker.KEY_TASK_TITLE, taskTitle)
            .putBoolean(TaskReminderWorker.KEY_IS_DUE_TODAY, isDueToday)
            .build()

        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName(taskId, suffix), ExistingWorkPolicy.REPLACE, request)
    }

    /** Cancels both the day-before and day-of reminders for a task. */
    fun cancel(context: Context, taskId: String) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(workName(taskId, "day_before"))
        workManager.cancelUniqueWork(workName(taskId, "day_of"))
    }

    private fun workName(taskId: String, suffix: String) = "task_reminder_${taskId}_$suffix"
}