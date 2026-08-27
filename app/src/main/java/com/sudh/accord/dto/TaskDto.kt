package com.sudh.accord.dto

data class TaskDto(
    val id: String,
    val title: String,
    val description: String?,
    val value: Double,
    val type: String,
    val isCompleted: Boolean,
    val dueDate: String?,
    val lastCompletedAt: String? = null,
    val userId: String,
    // Optimistic-concurrency counter the backend bumps on every accepted
    // write to this task. Round-tripped as `baseVersion` in TaskSyncRequest
    // so the server can detect a stale client view. Defaults to 0 for call
    // sites (e.g. CreateTaskRequest-adjacent local placeholders) that predate
    // ever having a server-assigned version.
    val version: Int = 0
)