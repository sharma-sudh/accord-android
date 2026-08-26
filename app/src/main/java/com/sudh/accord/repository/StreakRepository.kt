package com.sudh.accord.repository

import com.sudh.accord.dto.StreakDto
import com.sudh.accord.network.RetrofitClient

class StreakRepository {

    private val api = RetrofitClient.api

    suspend fun checkIn(token: String): Result<StreakDto> = try {
        Result.success(api.checkInStreak(token))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getStreak(token: String): Result<StreakDto> = try {
        Result.success(api.getStreak(token))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun checkWalletPressure(token: String): Result<Boolean> = try {
        Result.success(api.checkWalletPressure(token))
    } catch (e: Exception) {
        Result.failure(e)
    }
}