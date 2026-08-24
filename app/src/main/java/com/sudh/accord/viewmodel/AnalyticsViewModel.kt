package com.sudh.accord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.accord.AccordApplication
import com.sudh.accord.model.AnalyticsSeriesPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val app                 = getApplication<AccordApplication>()
    private val analyticsRepository = app.analyticsRepository
    private val tokenManager        = app.tokenManager

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun loadAnalytics(range: String = _uiState.value.selectedRange) {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.update { it.copy(error = "Session expired. Please sign in again.") }
                return@launch
            }

            _uiState.update { it.copy(selectedRange = range, isLoading = true, error = null) }

            analyticsRepository.getAnalytics("Bearer $token", range)
                .onSuccess { dto ->
                    _uiState.update {
                        it.copy(
                            totalEarned    = dto.totalEarned,
                            totalSpent     = dto.totalSpent,
                            completionRate = dto.completionRate.toFloat(),
                            streakDays     = dto.streakDays,
                            series         = dto.series.map { point ->
                                AnalyticsSeriesPoint(
                                    date           = point.date,
                                    earned         = point.earned.toFloat(),
                                    spent          = point.spent.toFloat(),
                                    completedCount = point.completedCount.toInt(),
                                )
                            },
                            taskBreakdown = dto.taskBreakdown,
                            isEmptyState  = dto.isEmpty,
                            isLoading     = false,
                            error         = null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load analytics") }
                }
        }
    }

    // No-op if the range hasn't actually changed — the toggle can fire while
    // a load for the same range is still in flight.
    fun selectRange(range: String) {
        if (range == _uiState.value.selectedRange) return
        loadAnalytics(range)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
