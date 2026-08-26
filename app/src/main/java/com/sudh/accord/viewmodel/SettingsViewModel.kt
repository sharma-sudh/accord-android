package com.sudh.accord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.accord.AccordApplication
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app                    = getApplication<AccordApplication>()
    private val userRepository         = app.userRepository
    private val tokenManager           = app.tokenManager
    private val notificationTogglePrefs = app.notificationTogglePrefs

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            walletPressureEnabled  = notificationTogglePrefs.walletPressureEnabled,
            sundayNarrativeEnabled = notificationTogglePrefs.sundayNarrativeEnabled,
            taskRemindersEnabled   = notificationTogglePrefs.taskRemindersEnabled,
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.update { it.copy(isLoadingProfile = false, profileError = "Session expired. Please sign in again.") }
                return@launch
            }

            _uiState.update { it.copy(isLoadingProfile = true, profileError = null) }

            userRepository.getProfile("Bearer $token")
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            name             = profile.name,
                            email            = profile.email,
                            budgetInput      = formatBudget(profile.monthlyBudget),
                            isLoadingProfile = false,
                            profileError     = null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoadingProfile = false, profileError = e.message ?: "Couldn't load your account")
                    }
                }
        }
    }

    fun onBudgetInputChange(value: String) {
        _uiState.update { it.copy(budgetInput = value, budgetSaveError = null, budgetSaved = false) }
    }

    fun saveBudget() {
        val budget = _uiState.value.budgetInput.toDoubleOrNull()
        if (budget == null || budget < 0) {
            _uiState.update { it.copy(budgetSaveError = "Enter a valid amount") }
            return
        }

        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            _uiState.update { it.copy(isSavingBudget = true, budgetSaveError = null, budgetSaved = false) }

            userRepository.updateBudget(token = "Bearer $token", budget = budget)
                .onSuccess {
                    _uiState.update { it.copy(isSavingBudget = false, budgetSaved = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSavingBudget = false, budgetSaveError = e.message ?: "Couldn't save budget")
                    }
                }
        }
    }

    fun setWalletPressureEnabled(enabled: Boolean) {
        notificationTogglePrefs.walletPressureEnabled = enabled
        _uiState.update { it.copy(walletPressureEnabled = enabled) }
    }

    fun setSundayNarrativeEnabled(enabled: Boolean) {
        notificationTogglePrefs.sundayNarrativeEnabled = enabled
        _uiState.update { it.copy(sundayNarrativeEnabled = enabled) }
    }

    fun setTaskRemindersEnabled(enabled: Boolean) {
        notificationTogglePrefs.taskRemindersEnabled = enabled
        _uiState.update { it.copy(taskRemindersEnabled = enabled) }
    }

    fun signOut() {
        tokenManager.clearAll()
        viewModelScope.launch {
            _events.emit(SettingsEvent.SignedOut)
        }
    }
}

// Drops a trailing ".0" so a whole-number budget doesn't show as "5000.0"
// in the edit field, without rounding fractional values.
private fun formatBudget(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()