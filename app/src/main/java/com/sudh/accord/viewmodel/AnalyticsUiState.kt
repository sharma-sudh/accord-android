package com.sudh.accord.viewmodel

import com.sudh.accord.model.AnalyticsSeriesPoint

data class AnalyticsUiState(
    val selectedRange: String                  = "week",
    val totalEarned: Double                    = 0.0,
    val totalSpent: Double                     = 0.0,
    val completionRate: Float                  = 0f,
    // Null until 0.4.0's streak logic lands server-side — the screen hides the
    // streak card entirely on null rather than showing a fake "0 days".
    val streakDays: Int?                       = null,
    // One point per day, oldest first, zero-filled by the backend.
    val series: List<AnalyticsSeriesPoint>     = emptyList(),
    // Task title -> completions in range, only for tasks completed at least
    // once. No per-task percentage from the backend, so the breakdown UI ranks
    // by raw count instead of the old fake 0-100% bars.
    val taskBreakdown: Map<String, Long>       = emptyMap(),
    // True only when the user has zero transactions ever (all-time, not just
    // this range) — drives the blurred empty-state chart overlay.
    val isEmptyState: Boolean                  = false,
    // True by default: the screen is always backed by a fresh load on
    // composition, so the initial frame must show the spinner, not the
    // (still-empty) default state.
    val isLoading: Boolean                     = true,
    val error: String?                         = null,
)
