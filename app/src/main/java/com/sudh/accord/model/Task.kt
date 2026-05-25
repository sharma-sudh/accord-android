package com.sudh.accord.model

data class Task(
    val id: String,
    val title: String,
    val value: Double,
    val isRecurring: Boolean,
    val recurrenceType: String? = null,
    val dueDate: String? = null,
    val description: String? = null
)