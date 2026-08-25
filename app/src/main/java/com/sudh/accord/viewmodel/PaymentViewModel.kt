package com.sudh.accord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.accord.AccordApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val app                = getApplication<AccordApplication>()
    private val paymentRepository  = app.paymentRepository
    private val tokenManager       = app.tokenManager

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    // Deducts the amount on the backend. onSuccess is only invoked once the
    // mutation has actually landed — the caller navigates home from there,
    // so we never leave the screen showing a stale "confirm" state for a
    // payment that didn't go through.
    fun confirmPayment(
        merchantName: String,
        upiId: String,
        amount: Double,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                _uiState.update { it.copy(error = "Session expired. Please sign in again.") }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true, error = null) }

            paymentRepository.spend(
                token        = "Bearer $token",
                merchantName = merchantName,
                upiId        = upiId,
                amount       = amount
            )
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, error = null) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, error = e.message ?: "Failed to confirm payment")
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}