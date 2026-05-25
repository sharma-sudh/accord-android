package com.sudh.accord.repository

import com.sudh.accord.dto.CreateTaskRequest
import com.sudh.accord.dto.TaskDto
import com.sudh.accord.network.RetrofitClient

class TaskRepository {

    private val api = RetrofitClient.api

    suspend fun getTasks(token: String): Result<List<TaskDto>> = try {
        Result.success(api.getTasks(token))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createTask(token: String, request: CreateTaskRequest): Result<TaskDto> = try {
        Result.success(api.createTask(token, request))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteTask(token: String, id: String): Result<Unit> {
        return try {
            val response = api.deleteTask(token, id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeTask(token: String, id: String): Result<TaskDto> = try {
        Result.success(api.completeTask(token, id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getBalance(token: String): Result<Double> = try {
        Result.success(api.getBalance(token))
    } catch (e: Exception) {
        Result.failure(e)
    }
}