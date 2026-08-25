package com.sudh.accord.dto

// Mirrors the backend's PaymentRequest record — amount + merchantName only.
// No upiId: TransactionController.logPayment takes user from the JWT
// (@AuthenticationPrincipal) and doesn't accept a upiId at all.
data class PaymentRequest(
    val amount: Double,
    val merchantName: String
)