package com.sudh.accord.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // PENDING_DELETE rows are hidden from every read — from the user's
    // perspective the task is already gone the moment deleteTask() runs
    // locally; the row itself sticks around only so SyncWorker knows to
    // tell the server about it. CONFLICT rows are hidden too — a task stuck
    // in CONFLICT shouldn't silently reappear in the normal list with
    // whichever value happened to be on the row; it's surfaced separately
    // via observeConflicts() until the user resolves it.
    @Query("SELECT * FROM tasks WHERE syncState NOT IN ('PENDING_DELETE', 'CONFLICT') ORDER BY dueDate IS NULL, dueDate ASC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TaskEntity?

    // CONFLICT is excluded here too: it's a terminal state until the user
    // resolves it, not something SyncWorker should keep retrying every run.
    @Query("SELECT * FROM tasks WHERE syncState NOT IN ('SYNCED', 'CONFLICT')")
    suspend fun getPending(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE syncState = 'CONFLICT' ORDER BY title ASC")
    fun observeConflicts(): Flow<List<TaskEntity>>

    @Upsert
    suspend fun upsert(task: TaskEntity)

    @Upsert
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM tasks WHERE syncState = 'SYNCED'")
    suspend fun deleteAllSynced()

    @Query("DELETE FROM tasks WHERE syncState = 'SYNCED' AND id NOT IN (:keepIds)")
    suspend fun deleteSyncedNotIn(keepIds: List<String>)

    // Replaces the server-authoritative view of the task list: every row the
    // server still knows about is upserted as SYNCED, and any row that was
    // SYNCED locally but is no longer in the server's list (deleted
    // elsewhere) is dropped. Rows with a pending local mutation are never
    // touched here, since PENDING_* ids never match a fresh server id.
    @Transaction
    suspend fun replaceSyncedTasks(fresh: List<TaskEntity>) {
        if (fresh.isEmpty()) {
            deleteAllSynced()
        } else {
            deleteSyncedNotIn(fresh.map { it.id })
        }
        upsertAll(fresh)
    }
}