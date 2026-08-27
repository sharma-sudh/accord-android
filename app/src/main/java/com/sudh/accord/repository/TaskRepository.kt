package com.sudh.accord.repository

import android.content.Context
import com.sudh.accord.data.local.AccordDatabase
import com.sudh.accord.data.local.SyncState
import com.sudh.accord.data.local.TaskEntity
import com.sudh.accord.data.local.toDto
import com.sudh.accord.data.local.toEntity
import com.sudh.accord.dto.CreateTaskRequest
import com.sudh.accord.dto.TaskDto
import com.sudh.accord.network.RetrofitClient
import com.sudh.accord.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

/**
 * Local-first: every read comes from Room, every write lands in Room first
 * and is queued for the background SyncWorker to push once connected (see
 * SyncScheduler). Existing call sites (HomeViewModel, OnboardingViewModel)
 * are unchanged — getTasks()/createTask()/etc. keep their old signatures and
 * now simply never fail for lack of connectivity.
 */
class TaskRepository(context: Context) {

    private val appContext = context.applicationContext
    private val api = RetrofitClient.api
    private val taskDao = AccordDatabase.getInstance(appContext).taskDao()

    fun observeTasks(): Flow<List<TaskDto>> =
        taskDao.observeTasks().map { list -> list.map { it.toDto() } }

    // Best-effort network refresh merged into Room. Failures (offline, 5xx,
    // expired token) are swallowed here on purpose — refreshFromServer()
    // never surfaces an error, because a stale local cache beats an error
    // screen for an "offline-first" app.
    suspend fun refreshFromServer(token: String) {
        try {
            val fresh = api.getTasks(token)
            taskDao.replaceSyncedTasks(fresh.map { it.toEntity(SyncState.SYNCED) })
        } catch (_: Exception) {
            // keep whatever's already in Room
        }
    }

    suspend fun getTasks(token: String): Result<List<TaskDto>> {
        refreshFromServer(token)
        return Result.success(taskDao.observeTasks().first().map { it.toDto() })
    }

    suspend fun createTask(token: String, request: CreateTaskRequest): Result<TaskDto> {
        val entity = TaskEntity(
            id = "local_${UUID.randomUUID()}",
            title = request.title,
            description = request.description,
            value = request.value,
            type = request.type,
            isCompleted = false,
            dueDate = request.dueDate,
            lastCompletedAt = null,
            userId = "",
            syncState = SyncState.PENDING_CREATE
        )
        taskDao.upsert(entity)
        SyncScheduler.enqueueNow(appContext)
        return Result.success(entity.toDto())
    }

    suspend fun completeTask(token: String, id: String): Result<TaskDto> {
        val existing = taskDao.getById(id) ?: return Result.failure(Exception("Task not found"))
        val updated = existing.copy(
            isCompleted = true,
            lastCompletedAt = Instant.now().toString(),
            // A task never synced yet stays PENDING_CREATE — SyncWorker
            // folds the completion into the same create call. Otherwise it
            // needs its own push to the server.
            syncState = if (existing.syncState == SyncState.PENDING_CREATE) {
                SyncState.PENDING_CREATE
            } else {
                SyncState.PENDING_UPDATE
            }
        )
        taskDao.upsert(updated)
        SyncScheduler.enqueueNow(appContext)
        return Result.success(updated.toDto())
    }

    suspend fun deleteTask(token: String, id: String): Result<Unit> {
        val existing = taskDao.getById(id) ?: return Result.success(Unit)
        if (existing.syncState == SyncState.PENDING_CREATE) {
            taskDao.deleteById(id) // never reached the server — nothing to sync
        } else {
            taskDao.upsert(existing.copy(syncState = SyncState.PENDING_DELETE))
        }
        SyncScheduler.enqueueNow(appContext)
        return Result.success(Unit)
    }

    // Unchanged: balance is server-computed (see backend TransactionService)
    // and there's no local equivalent to fall back to, so this still
    // requires connectivity.
    suspend fun getBalance(token: String): Result<Double> = try {
        Result.success(api.getBalance(token))
    } catch (e: Exception) {
        Result.failure(e)
    }
}