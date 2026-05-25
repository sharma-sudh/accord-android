package com.sudh.accord.dto

data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val isNewUser: Boolean
)