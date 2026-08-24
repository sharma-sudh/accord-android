package com.sudh.accord.viewmodel

import com.sudh.accord.model.Task

data class HomeUiState(
    val tasks: List<Task>     = emptyList(),
    val walletBalance: Double = 0.0,
    val amountSpent: Double   = 0.0,
    val monthlyBudget: Double = 0.0,
    val streakDays: Int       = 0,
    val isLoading: Boolean    = false,
    // Gates the full-screen error view — only ever set by the initial load.
    val error: String?        = null,
    // Transient result of addTask/completeTask/deleteTask — shown as a
    // snackbar, never hides the already-loaded task list.
    val actionError: String?  = null,
)