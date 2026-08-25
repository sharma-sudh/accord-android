package com.sudh.accord.repository

import com.sudh.accord.dto.PaymentRequest
import com.sudh.accord.dto.TransactionResponseDto
import com.sudh.accord.network.RetrofitClient

class PaymentRepository {

    private val api = RetrofitClient.api

    suspend fun logPayment(token: String, merchantName: String, amount: Double): Result<TransactionResponseDto> = try {
        Result.success(api.logPayment(token, PaymentRequest(amount, merchantName)))
    } catch (e: Exception) {
        Result.failure(e)
    }
}