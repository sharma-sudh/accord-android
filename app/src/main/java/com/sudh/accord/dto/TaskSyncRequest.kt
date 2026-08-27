package com.sudh.accord.dto

/**
 * Sync payload for pushing a local mutation on an *existing* (already-synced)
 * task — i.e. what SyncWorker sends for a PENDING_UPDATE row. [baseVersion]
 * is the server version this device last saw; the server applies the write
 * only if its current Task.version still matches, otherwise it's a conflict
 * (see [TaskSyncResponse]).
 *
 * [changes] is field-level on purpose: only the fields this device actually
 * touched are sent (e.g. just `isCompleted`/`lastCompletedAt` for a task
 * completion), so the server can attempt a per-field merge against whatever
 * changed server-side instead of one write blindly clobbering the other.
 * Values are raw field values (Boolean, String, Double, null) as they'd
 * appear in [TaskDto] — Gson serializes them as-is.
 *
 * PENDING_CREATE rows never go through this path: a task that hasn't
 * reached the server yet has no server version to conflict with.
 */
data class TaskSyncRequest(
    val recordId: String,
    val baseVersion: Int,
    val changes: Map<String, @JvmSuppressWildcards Any?>,
    val clientTimestamp: String
)