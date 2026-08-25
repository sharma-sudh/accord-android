package com.sudh.accord.viewmodel

data class PaymentUiState(
    val isSubmitting: Boolean = false,
    val error: String? = null,
)