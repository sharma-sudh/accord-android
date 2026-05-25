package com.sudh.accord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.accord.AccordApplication
import com.sudh.accord.dto.CreateTaskRequest
import com.sudh.accord.model.Task
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class OnboardingUiState {
    object Idle    : OnboardingUiState()
    object Loading : OnboardingUiState()
    data class Error(val message: String) : OnboardingUiState()
}

// ── Navigation Events ─────────────────────────────────────────────────────────

sealed class OnboardingEvent {
    object NavigateToHome : OnboardingEvent()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val app            = getApplication<AccordApplication>()
    private val userRepository = app.userRepository
    private val taskRepository = app.taskRepository
    private val tokenManager   = app.tokenManager

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    fun completeOnboarding(budget: Double, pendingTask: Task?) {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.value = OnboardingUiState.Error("Session expired. Please sign in again.")
                return@launch
            }

            _uiState.value = OnboardingUiState.Loading

            // Step 1 — save the budget
            userRepository.updateBudget("Bearer $token", budget)
                .onFailure { e ->
                    _uiState.value = OnboardingUiState.Error(
                        e.message ?: "Failed to save budget. Please try again."
                    )
                    return@launch
                }

            // Step 2 — create the first task if the user added one
            if (pendingTask != null) {
                val request = CreateTaskRequest(
                    title       = pendingTask.title,
                    description = pendingTask.description,
                    value       = pendingTask.value,
                    type        = if (pendingTask.isRecurring)
                        pendingTask.recurrenceType?.uppercase() ?: "DAILY"
                    else "ONE_OFF",
                    dueDate     = pendingTask.dueDate
                )

                taskRepository.createTask("Bearer $token", request)
                    .onFailure { e ->
                        _uiState.value = OnboardingUiState.Error(
                            e.message ?: "Failed to create task. Please try again."
                        )
                        return@launch
                    }
            }

            // Both steps succeeded
            _uiState.value = OnboardingUiState.Idle
            _events.emit(OnboardingEvent.NavigateToHome)
        }
    }
}