package com.sudh.accord.dto

data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val value: Double,
    val type: String,
    val dueDate: String?
)