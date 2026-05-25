package com.sudh.accord.viewmodel

import com.sudh.accord.model.Task

data class HomeUiState(
    val tasks: List<Task>     = emptyList(),
    val walletBalance: Double = 0.0,
    val amountSpent: Double   = 0.0,
    val monthlyBudget: Double = 0.0,
    val streakDays: Int       = 0,
    val isLoading: Boolean    = false,
    val error: String?        = null
)