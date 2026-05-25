package com.sudh.accord.repository

import com.sudh.accord.dto.UpdateBudgetRequest
import com.sudh.accord.network.RetrofitClient

class UserRepository {

    private val api = RetrofitClient.api

    suspend fun updateBudget(token: String, budget: Double): Result<Unit> {
        return try {
            val response = api.updateBudget(token, UpdateBudgetRequest(budget))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update budget: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}