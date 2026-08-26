package com.sudh.accord.repository

import com.sudh.accord.dto.AuthResponse
import com.sudh.accord.dto.ForgotPasswordRequest
import com.sudh.accord.dto.GoogleSignInRequest
import com.sudh.accord.dto.LoginRequest
import com.sudh.accord.dto.RegisterRequest
import com.sudh.accord.dto.ResetPasswordRequest
import com.sudh.accord.dto.VerifyOtpRequest
import com.sudh.accord.dto.VerifyOtpResponse
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

    suspend fun forgotPassword(email: String): Result<Unit> = try {
        val response = api.forgotPassword(ForgotPasswordRequest(email))
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Couldn't send code: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun verifyOtp(email: String, otp: String): Result<VerifyOtpResponse> = try {
        Result.success(api.verifyOtp(VerifyOtpRequest(email, otp)))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun resetPassword(resetToken: String, newPassword: String): Result<Unit> = try {
        val response = api.resetPassword(ResetPasswordRequest(resetToken, newPassword))
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Couldn't reset password: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}