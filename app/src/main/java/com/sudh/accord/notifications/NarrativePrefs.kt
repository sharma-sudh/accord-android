package com.sudh.accord.notifications

import android.content.Context

/**
 * Separate from NotificationPrefs (which tracks the POST_NOTIFICATIONS
 * permission flow specifically) since this is unrelated state: just which
 * weekStartDate's narrative NarrativeWorker has already surfaced, so a daily
 * periodic check doesn't re-notify for the same week every time it runs.
 */
class NarrativePrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    /** weekStartDate (ISO "yyyy-MM-dd") of the last narrative shown as a notification, or null. */
    var lastNotifiedWeekStartDate: String?
        get() = prefs.getString(KEY_LAST_NOTIFIED_WEEK, null)
        set(value) = prefs.edit().putString(KEY_LAST_NOTIFIED_WEEK, value).apply()

    companion object {
        private const val FILE_NAME = "accord_narrative_prefs"
        private const val KEY_LAST_NOTIFIED_WEEK = "last_notified_week_start_date"
    }
}