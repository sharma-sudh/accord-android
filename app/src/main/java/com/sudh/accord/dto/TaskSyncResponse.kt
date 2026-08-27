package com.sudh.accord.dto

/**
 * Result of a [TaskSyncRequest]. [status] is one of [TaskSyncStatus]:
 *  - APPLIED  — no conflicting server-side change; the write went through
 *               as sent. [task] is the new authoritative row.
 *  - MERGED   — the server also had a change since [TaskSyncRequest.baseVersion],
 *               but it touched different fields than [TaskSyncRequest.changes];
 *               the server merged both sets. [task] is the combined result.
 *  - CONFLICT — the same field(s) changed on both sides and couldn't be
 *               auto-merged; nothing was written. [serverTask] is the
 *               server's current copy and [conflictingFields] names what
 *               collided, for a resolution UI to diff against the local row.
 *
 * A plain String (not a Kotlin enum) on the wire deliberately, matching how
 * this codebase already represents other backend-defined categories (e.g.
 * TaskDto.type) — it degrades to an unrecognized value instead of a
 * deserialization crash if the backend adds a status this client predates.
 */
data class TaskSyncResponse(
    val status: String,
    val task: TaskDto? = null,
    val serverTask: TaskDto? = null,
    val conflictingFields: List<String>? = null
)

object TaskSyncStatus {
    const val APPLIED = "APPLIED"
    const val MERGED = "MERGED"
    const val CONFLICT = "CONFLICT"
}