package com.sudh.accord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.accord.AccordApplication
import com.sudh.accord.dto.CreateTaskRequest
import com.sudh.accord.dto.TaskDto
import com.sudh.accord.model.Task
import com.sudh.accord.notifications.NotificationPermissionHelper
import com.sudh.accord.notifications.TaskReminderScheduler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app            = getApplication<AccordApplication>()
    private val taskRepository = app.taskRepository
    private val tokenManager   = app.tokenManager
    private val notificationPrefs = app.notificationPrefs

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Reflects the app-open checkin (see MainActivity) as soon as it
        // resolves, independent of loadData()'s own network calls.
        viewModelScope.launch {
            app.currentStreak.collect { streak ->
                if (streak != null) {
                    _uiState.update { it.copy(streakDays = streak) }
                }
            }
        }
    }

    // Shows the one-time soft nudge only if: permission is still denied, it
    // was denied at least a week ago, and the nudge has never been shown
    // before. Re-checked on every loadData() so a Settings-page grant made
    // outside the app is picked up without needing a live observer.
    private fun evaluateNotificationNudge() {
        if (notificationPrefs.nudgeShown) return
        if (NotificationPermissionHelper.isGranted(app)) return
        val deniedAt = notificationPrefs.deniedAtMillis
        if (deniedAt == 0L) return

        val oneWeekMillis = TimeUnit.DAYS.toMillis(7)
        if (System.currentTimeMillis() - deniedAt >= oneWeekMillis) {
            _uiState.update { it.copy(showNotificationNudge = true) }
        }
    }

    fun dismissNotificationNudge() {
        notificationPrefs.nudgeShown = true
        _uiState.update { it.copy(showNotificationNudge = false) }
    }

    fun loadData() {
        evaluateNotificationNudge()
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.update { it.copy(error = "Session expired. Please sign in again.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // fire both calls in parallel — no reason to wait for tasks before fetching balance
                coroutineScope {
                    val tasksDeferred  = async { taskRepository.getTasks("Bearer $token") }
                    val balanceDeferred = async { taskRepository.getBalance("Bearer $token") }

                    val tasksResult   = tasksDeferred.await()
                    val balanceResult = balanceDeferred.await()

                    tasksResult.onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load tasks") }
                        return@coroutineScope
                    }

                    balanceResult.onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load balance") }
                        return@coroutineScope
                    }

                    _uiState.update { current ->
                        current.copy(
                            tasks         = tasksResult.getOrDefault(emptyList()).map { it.toTask() },
                            walletBalance = balanceResult.getOrDefault(0.0),
                            isLoading     = false,
                            error         = null,
                            hasEverAddedTask = taskRepository.hasEverAddedTask()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Something went wrong") }
            }
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch

            // optimistic update — remove from list immediately, revert on failure
            _uiState.update { current ->
                current.copy(
                    tasks         = current.tasks.filterNot { it.id == task.id },
                    walletBalance = current.walletBalance + task.value
                )
            }

            taskRepository.completeTask("Bearer $token", task.id)
                .onSuccess {
                    // Completed early — the due-date reminders are no longer relevant.
                    TaskReminderScheduler.cancel(app, task.id)
                }
                .onFailure { e ->
                    // revert and surface the error
                    _uiState.update { current ->
                        current.copy(
                            tasks = current.tasks + task,
                            walletBalance = current.walletBalance - task.value,
                            actionError = e.message ?: "Failed to complete task"
                        )
                    }
                }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch

            // optimistic update
            _uiState.update { current ->
                current.copy(tasks = current.tasks.filterNot { it.id == task.id })
            }

            taskRepository.deleteTask("Bearer $token", task.id)
                .onSuccess {
                    TaskReminderScheduler.cancel(app, task.id)
                }
                .onFailure { e ->
                    _uiState.update { current ->
                        current.copy(
                            tasks = current.tasks + task,
                            actionError = e.message ?: "Failed to delete task"
                        )
                    }
                }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch

            val request = CreateTaskRequest(
                title       = task.title,
                description = task.description,
                value       = task.value,
                type        = if (task.isRecurring) task.recurrenceType ?: "DAILY" else "ONE_OFF",
                dueDate     = task.dueDate
            )

            taskRepository.createTask("Bearer $token", request)
                .onSuccess { dto ->
                    _uiState.update { current ->
                        current.copy(
                            tasks = current.tasks + dto.toTask(),
                            // The very first task: flips the empty state
                            // (were it to show again) from "No tasks yet"
                            // to "All caught up" without waiting on a
                            // reload. Cheap to set unconditionally on every
                            // add — already true after the first task.
                            hasEverAddedTask = true
                        )
                    }
                    // Reminders only make sense for one-off tasks with a due date.
                    if (dto.type == "ONE_OFF" && dto.dueDate != null) {
                        TaskReminderScheduler.schedule(app, dto.id, dto.title, dto.dueDate)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(actionError = e.message ?: "Failed to create task") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearActionError() {
        _uiState.update { it.copy(actionError = null) }
    }
}

// ── TaskDto → Task mapping ────────────────────────────────────────────────────

private fun TaskDto.toTask(): Task = Task(
    id             = id,
    title          = title,
    description    = description,
    value          = value,
    isRecurring    = type != "ONE_OFF",
    recurrenceType = if (type != "ONE_OFF") type else null,
    dueDate        = dueDate,
    lastCompletedAt = lastCompletedAt
)