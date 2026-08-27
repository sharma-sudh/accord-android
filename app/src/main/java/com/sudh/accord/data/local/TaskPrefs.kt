package com.sudh.accord.data.local

import android.content.Context

/**
 * One durable flag: has this device's user ever successfully created a
 * task? Needed to tell "No tasks yet" (never created one) apart from "All
 * caught up" (created some, all of them are done for now) when the visible
 * tasks list is empty — list emptiness alone can't distinguish the two,
 * since a fully-cleared recurring cycle looks identical to a brand new
 * account.
 *
 * Set once, from TaskRepository.createTask(), and never unset — a task
 * created and later deleted shouldn't demote the app back to the "yet"
 * copy. Plain (not encrypted) prefs — nothing sensitive here, just a bool.
 */
class TaskPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var hasEverAddedTask: Boolean
        get() = prefs.getBoolean(KEY_HAS_EVER_ADDED_TASK, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_EVER_ADDED_TASK, value).apply()

    companion object {
        private const val FILE_NAME = "accord_task_prefs"
        private const val KEY_HAS_EVER_ADDED_TASK = "has_ever_added_task"
    }
}