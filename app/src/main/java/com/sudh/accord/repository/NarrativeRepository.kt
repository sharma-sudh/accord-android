package com.sudh.accord.repository

import com.sudh.accord.dto.NarrativeDto
import com.sudh.accord.network.RetrofitClient

class NarrativeRepository {

    private val api = RetrofitClient.api

    /**
     * Returns the latest narrative, or null (as a success, not a failure) when
     * the backend has none yet — that's a 204, not an error. Result.failure is
     * reserved for actual network/HTTP failures so NarrativeWorker can tell
     * "nothing to show" apart from "should retry".
     */
    suspend fun getLatestNarrative(token: String): Result<NarrativeDto?> = try {
        val response = api.getLatestNarrative(token)
        if (response.isSuccessful) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("getLatestNarrative failed: HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}