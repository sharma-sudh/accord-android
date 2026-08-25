package com.sudh.accord.dto

data class SpendRequest(
    val merchantName: String,
    val upiId: String,
    val amount: Double
)