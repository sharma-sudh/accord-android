package com.sudh.accord.repository

import com.sudh.accord.dto.AnalyticsResponseDto
import com.sudh.accord.network.RetrofitClient

class AnalyticsRepository {

    private val api = RetrofitClient.api

    suspend fun getAnalytics(token: String, range: String): Result<AnalyticsResponseDto> = try {
        Result.success(api.getAnalytics(token, range))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
