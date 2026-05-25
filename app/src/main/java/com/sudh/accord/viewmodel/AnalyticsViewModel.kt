package com.sudh.accord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sudh.accord.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        AnalyticsUiState(
            totalEarned    = 2450.0,
            totalSpent     = 160.0,
            completionRate = 0.72f,
            streakDays     = 7,
            tasks = listOf(
                Task(id = "1", title = "Morning workout",    value = 50.0,  isRecurring = true,  recurrenceType = "daily"),
                Task(id = "2", title = "Read for 30 mins",   value = 30.0,  isRecurring = true,  recurrenceType = "daily"),
                Task(id = "3", title = "No junk food today", value = 20.0,  isRecurring = true,  recurrenceType = "daily"),
                Task(id = "4", title = "Submit assignment",   value = 100.0, isRecurring = false,
                    dueDate = "2025-05-20", description = "DAA assignment, upload on vtop"),
                Task(id = "5", title = "Call home",           value = 25.0,  isRecurring = false),
            ),
            taskCompletionRates = mapOf(
                "Morning workout"    to 0.85f,
                "Read for 30 mins"  to 0.60f,
                "No junk food today" to 0.40f,
                "Submit assignment" to 0.90f,
                "Call home"         to 0.70f,
            ),
            weekSpending   = listOf(120f, 80f, 200f, 60f, 150f, 90f, 170f),
            weekCompletion = listOf(60f, 75f, 50f, 90f, 70f, 85f, 65f),
            monthSpending  = listOf(
                800f, 650f, 1100f, 400f, 950f, 1200f, 700f,
                600f, 900f, 1050f, 450f, 750f, 870f, 300f,
                980f, 660f, 1150f, 820f, 500f, 730f, 1010f,
                590f, 880f, 970f, 410f, 760f, 1080f, 630f, 490f, 840f,
            ),
            monthCompletion = listOf(
                65f, 70f, 55f, 80f, 72f, 60f, 78f,
                68f, 74f, 58f, 83f, 69f, 75f, 90f,
                62f, 77f, 53f, 71f, 86f, 67f, 73f,
                79f, 64f, 57f, 88f, 76f, 61f, 82f, 70f, 66f,
            ),
        )
    )
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
}