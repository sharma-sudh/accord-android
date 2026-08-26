package com.sudh.accord.dto

data class ResetPasswordRequest(
    val resetToken: String,
    val newPassword: String
)