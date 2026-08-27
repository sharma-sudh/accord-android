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
    val syncState: SyncState
)