package com.sudh.accord.repository

import com.sudh.accord.dto.AuthResponse
import com.sudh.accord.dto.GoogleSignInRequest
import com.sudh.accord.dto.LoginRequest
import com.sudh.accord.dto.RegisterRequest
import com.sudh.accord.network.RetrofitClient

class AuthRepository {

    private val api = RetrofitClient.api

    suspend fun googleSignIn(idToken: String): Result<AuthResponse> = try {
        Result.success(api.googleSignIn(GoogleSignInRequest(idToken)))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> = try {
        Result.success(api.register(RegisterRequest(name, email, password)))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> = try {
        Result.success(api.login(LoginRequest(email, password)))
    } catch (e: Exception) {
        Result.failure(e)
    }
}