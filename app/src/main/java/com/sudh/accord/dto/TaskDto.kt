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
    val userId: String
)