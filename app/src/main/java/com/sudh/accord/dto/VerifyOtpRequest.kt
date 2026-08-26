package com.sudh.accord.dto

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)