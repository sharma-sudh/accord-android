package com.sudh.accord.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Mirrors the backend's TaskDto shape, plus [syncState]. While a row is
 * PENDING_CREATE, [id] is a locally-generated "local_<uuid>" placeholder —
 * SyncWorker swaps the whole row out for the server's real id once the
 * create call lands.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val value: Double,
    val type: String,
    val isCompleted: Boolean,
    val dueDate: String?,
    val lastCompletedAt: String?,
    val userId: String,
    val syncState: SyncState,
    // Server-side optimistic-concurrency counter (mirrors the backend
    // Task.version column). Sent back as `baseVersion` on the next
    // PENDING_UPDATE push so the server can tell whether this device's view
    // is stale. Rows still PENDING_CREATE carry version = 0 as a placeholder
    // — the real starting version arrives with the create response.
    val version: Int = 0,
    // Set only while syncState == CONFLICT: a JSON-encoded TaskDto (via
    // Gson) snapshot of the server's copy at the moment the version check
    // failed. Null in every other state. Lets a resolution screen show
    // "yours vs. theirs" without a second network round trip; cleared by
    // TaskRepository.resolveConflict once the user picks an outcome.
    val conflictServerSnapshot: String? = null
)