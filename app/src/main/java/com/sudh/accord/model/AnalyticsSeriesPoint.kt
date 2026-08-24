package com.sudh.accord.model

data class AnalyticsSeriesPoint(
    val date: String,
    val earned: Float,
    val spent: Float,
    val completedCount: Int
)
