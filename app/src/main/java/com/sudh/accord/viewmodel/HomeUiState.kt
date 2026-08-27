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
    // One-time soft nudge shown if the user denied POST_NOTIFICATIONS and a
    // week has passed — see HomeViewModel.evaluateNotificationNudge.
    val showNotificationNudge: Boolean = false,
    // Has this account ever created a task, ever — see TaskPrefs. Lets
    // HomeScreen tell "No tasks yet" apart from "All caught up" when
    // [tasks] is empty; independent of the current list so a recurring
    // task's cycle reset (list going empty -> non-empty -> empty again)
    // never flips the empty state back to the "yet" copy.
    val hasEverAddedTask: Boolean = false,
)