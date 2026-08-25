package com.sudh.accord.network

import com.sudh.accord.dto.AnalyticsResponseDto
import com.sudh.accord.dto.AuthResponse
import com.sudh.accord.dto.CreateTaskRequest
import com.sudh.accord.dto.GoogleSignInRequest
import com.sudh.accord.dto.PaymentRequest
import com.sudh.accord.dto.RefreshRequest
import com.sudh.accord.dto.TaskDto
import com.sudh.accord.dto.TransactionResponseDto
import com.sudh.accord.dto.UpdateBudgetRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AccordApi {

    // ── Auth ─────────────────────────────────────────────────────────────────

    @POST("auth/google")
    suspend fun googleSignIn(
        @Body body: GoogleSignInRequest
    ): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(
        @Body body: RefreshRequest
    ): AuthResponse

    // ── Tasks ─────────────────────────────────────────────────────────────────

    @GET("api/v1/tasks")
    suspend fun getTasks(
        @Header("Authorization") token: String
    ): List<TaskDto>

    @POST("api/v1/tasks")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Body request: CreateTaskRequest
    ): TaskDto

    @DELETE("api/v1/tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @PATCH("api/v1/tasks/{id}/complete")
    suspend fun completeTask(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): TaskDto

    // ── Balance ───────────────────────────────────────────────────────────────

    @GET("api/v1/transactions/balance")
    suspend fun getBalance(
        @Header("Authorization") token: String
    ): Double

    @POST("api/v1/transactions/payment")
    suspend fun logPayment(
        @Header("Authorization") token: String,
        @Body request: PaymentRequest
    ): TransactionResponseDto

    @PATCH("api/v1/users")
    suspend fun updateBudget(
        @Header("Authorization") token: String,
        @Body request: UpdateBudgetRequest
    ): Response<Unit>

    // ── Analytics ────────────────────────────────────────────────────────────

    @GET("api/v1/analytics")
    suspend fun getAnalytics(
        @Header("Authorization") token: String,
        @Query("range") range: String
    ): AnalyticsResponseDto
}