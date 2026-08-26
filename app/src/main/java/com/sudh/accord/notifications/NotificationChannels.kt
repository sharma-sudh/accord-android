package com.sudh.accord.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {

    const val TASK_REMINDERS_CHANNEL_ID = "task_reminders"
    const val WALLET_PRESSURE_CHANNEL_ID = "wallet_pressure"
    const val WEEKLY_NARRATIVE_CHANNEL_ID = "weekly_narrative"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val taskReminders = NotificationChannel(
            TASK_REMINDERS_CHANNEL_ID,
            "Task due-date reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders the day before and the day a task is due"
        }

        val walletPressure = NotificationChannel(
            WALLET_PRESSURE_CHANNEL_ID,
            "Wallet nudges",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "A nudge when your wallet's running low and you haven't logged a task in a while"
        }

        val weeklyNarrative = NotificationChannel(
            WEEKLY_NARRATIVE_CHANNEL_ID,
            "Weekly recap",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Your Sunday one-line recap of the past week"
        }

        manager.createNotificationChannel(taskReminders)
        manager.createNotificationChannel(walletPressure)
        manager.createNotificationChannel(weeklyNarrative)
    }
}