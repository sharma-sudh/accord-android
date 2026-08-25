package com.sudh.accord.dto

// Mirrors the backend's TransactionResponse record.
data class TransactionResponseDto(
    val id: String,
    val amount: Double,
    val type: String,
    val merchantName: String,
    val createdAt: String?
)