package com.sudh.accord.dto

data class AnalyticsResponseDto(
    val totalEarned: Double,
    val totalSpent: Double,
    val completionRate: Double,
    val streakDays: Int?,
    val series: List<AnalyticsSeriesPointDto>,
    val taskBreakdown: Map<String, Long>,
    val isEmpty: Boolean
)

data class AnalyticsSeriesPointDto(
    val date: String,
    val earned: Double,
    val spent: Double,
    val completedCount: Long
)
