package com.sudh.accord.notifications

import android.content.Context
import androidx.core.content.edit

/**
 * Per-notification-type opt-outs, set from SettingsSheet. Deliberately plain
 * (not encrypted) SharedPreferences and not server-synced — these are local
 * display preferences, not account data, and WalletPressureWorker /
 * NarrativeWorker / TaskReminderWorker each check the relevant flag here
 * before firing. All three default to enabled so existing installs (and any
 * device that hasn't opened Settings yet) keep today's behavior.
 */
class NotificationTogglePrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var walletPressureEnabled: Boolean
        get() = prefs.getBoolean(KEY_WALLET_PRESSURE, true)
        set(value) = prefs.edit { putBoolean(KEY_WALLET_PRESSURE, value) }

    var sundayNarrativeEnabled: Boolean
        get() = prefs.getBoolean(KEY_SUNDAY_NARRATIVE, true)
        set(value) = prefs.edit { putBoolean(KEY_SUNDAY_NARRATIVE, value) }

    var taskRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_TASK_REMINDERS, true)
        set(value) = prefs.edit { putBoolean(KEY_TASK_REMINDERS, value) }

    companion object {
        private const val FILE_NAME = "accord_notification_toggle_prefs"
        private const val KEY_WALLET_PRESSURE = "wallet_pressure_enabled"
        private const val KEY_SUNDAY_NARRATIVE = "sunday_narrative_enabled"
        private const val KEY_TASK_REMINDERS = "task_reminders_enabled"
    }
}