package com.sudh.accord.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String,
    val isNewUser: Boolean
)