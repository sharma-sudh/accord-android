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
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class ForgotPasswordUiState {
    object Idle    : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

// ── Navigation Events ─────────────────────────────────────────────────────────

sealed class ForgotPasswordEvent {
    object OtpSent             : ForgotPasswordEvent()
    object OtpVerified         : ForgotPasswordEvent()
    object PasswordResetDone   : ForgotPasswordEvent()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────
//
// Scoped once at the NavGraph level (like homeViewModel/onboardingViewModel)
// so it survives across ForgotPasswordScreen -> OtpVerifyScreen ->
// ResetPasswordScreen. The reset token never touches a nav route/back-stack
// argument — it's held in memory here only, between verifyOtp() succeeding
// and resetPassword() consuming it.

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val app            = getApplication<AccordApplication>()
    private val authRepository = app.authRepository

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events: SharedFlow<ForgotPasswordEvent> = _events.asSharedFlow()

    var email: String = ""
        private set

    private var resetToken: String? = null

    fun sendOtp(email: String) {
        this.email = email
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading

            authRepository.forgotPassword(email)
                .onSuccess {
                    _uiState.value = ForgotPasswordUiState.Idle
                    _events.emit(ForgotPasswordEvent.OtpSent)
                }
                .onFailure { e ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        e.message ?: "Couldn't send code. Please try again."
                    )
                }
        }
    }

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading

            authRepository.verifyOtp(email, otp)
                .onSuccess { response ->
                    resetToken = response.resetToken
                    _uiState.value = ForgotPasswordUiState.Idle
                    _events.emit(ForgotPasswordEvent.OtpVerified)
                }
                .onFailure { e ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        e.message ?: "Invalid or expired code."
                    )
                }
        }
    }

    fun resetPassword(newPassword: String) {
        val token = resetToken
        if (token == null) {
            _uiState.value = ForgotPasswordUiState.Error("Please verify the code again.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading

            authRepository.resetPassword(token, newPassword)
                .onSuccess {
                    _uiState.value = ForgotPasswordUiState.Idle
                    _events.emit(ForgotPasswordEvent.PasswordResetDone)
                }
                .onFailure { e ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        e.message ?: "Couldn't reset password. Please try again."
                    )
                }
        }
    }

    fun resendOtp() {
        if (email.isNotBlank()) sendOtp(email)
    }
}