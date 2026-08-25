package com.sudh.accord.repository

import com.sudh.accord.dto.SpendRequest
import com.sudh.accord.network.RetrofitClient

class PaymentRepository {

    private val api = RetrofitClient.api

    // Returns the updated wallet balance on success.
    suspend fun spend(token: String, merchantName: String, upiId: String, amount: Double): Result<Double> = try {
        Result.success(api.spend(token, SpendRequest(merchantName, upiId, amount)))
    } catch (e: Exception) {
        Result.failure(e)
    }
}