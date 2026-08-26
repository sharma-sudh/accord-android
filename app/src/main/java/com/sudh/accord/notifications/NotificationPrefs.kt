package com.sudh.accord.notifications

import android.content.Context

/**
 * Tracks the POST_NOTIFICATIONS runtime-permission flow per the design doc:
 * ask once, and if denied, show exactly one soft nudge on the home screen
 * after a week has passed — then never ask or nudge again. Plain (not
 * encrypted) prefs — nothing sensitive here, just booleans and a timestamp.
 */
class NotificationPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    /** Whether the runtime permission dialog has already been shown once, ever. */
    var hasRequestedOnce: Boolean
        get() = prefs.getBoolean(KEY_REQUESTED, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUESTED, value).apply()

    /** Epoch millis of the denial, or 0 if never denied (granted, or not yet asked). */
    var deniedAtMillis: Long
        get() = prefs.getLong(KEY_DENIED_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_DENIED_AT, value).apply()

    /** Whether the one-time soft nudge has already been shown, ever. */
    var nudgeShown: Boolean
        get() = prefs.getBoolean(KEY_NUDGE_SHOWN, false)
        set(value) = prefs.edit().putBoolean(KEY_NUDGE_SHOWN, value).apply()

    companion object {
        private const val FILE_NAME = "accord_notification_prefs"
        private const val KEY_REQUESTED = "requested_once"
        private const val KEY_DENIED_AT = "denied_at_millis"
        private const val KEY_NUDGE_SHOWN = "nudge_shown"
    }
}