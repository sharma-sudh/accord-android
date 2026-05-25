package com.sudh.accord.viewmodel

import com.sudh.accord.model.Task

data class AnalyticsUiState(
    val totalEarned: Double              = 0.0,
    val totalSpent: Double               = 0.0,
    val completionRate: Float            = 0f,
    val streakDays: Int                  = 0,
    val tasks: List<Task>                = emptyList(),
    val taskCompletionRates: Map<String, Float> = emptyMap(),
    val weekSpending: List<Float>        = emptyList(),
    val weekCompletion: List<Float>      = emptyList(),
    val monthSpending: List<Float>       = emptyList(),
    val monthCompletion: List<Float>     = emptyList(),
)