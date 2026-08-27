package com.sudh.accord.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sudh.accord.AccordApplication
import com.sudh.accord.data.local.AccordDatabase
import com.sudh.accord.data.local.SyncState
import com.sudh.accord.data.local.TaskDao
import com.sudh.accord.data.local.TaskEntity
import com.sudh.accord.data.local.TransactionDao
import com.sudh.accord.data.local.TransactionEntity
import com.sudh.accord.data.local.toEntity
import com.sudh.accord.dto.CreateTaskRequest
import com.sudh.accord.dto.PaymentRequest
import com.sudh.accord.network.AccordApi
import com.sudh.accord.network.RetrofitClient

/**
 * Reconciles every row still marked pending in Room against the backend.
 * Runs only under a CONNECTED network constraint (see SyncScheduler), so a
 * thrown exception here almost always means a real server error rather than
 * "offline" — those rows are simply left pending and retried on the next run.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as AccordApplication
        val token = app.tokenManager.getToken() ?: return Result.success() // signed out — nothing to push

        val authHeader = "Bearer $token"
        val api = RetrofitClient.api
        val db = AccordDatabase.getInstance(applicationContext)
        val taskDao = db.taskDao()
        val transactionDao = db.transactionDao()

        var allSucceeded = true

        for (task in taskDao.getPending()) {
            if (!syncTask(api, taskDao, authHeader, task)) allSucceeded = false
        }

        for (transaction in transactionDao.getPending()) {
            if (!syncTransaction(api, transactionDao, authHeader, transaction)) allSucceeded = false
        }

        return if (allSucceeded) Result.success() else Result.retry()
    }

    private suspend fun syncTask(
        api: AccordApi,
        dao: TaskDao,
        authHeader: String,
        task: TaskEntity
    ): Boolean = try {
        when (task.syncState) {
            SyncState.PENDING_CREATE -> {
                val request = CreateTaskRequest(
                    title = task.title,
                    description = task.description,
                    value = task.value,
                    type = task.type,
                    dueDate = task.dueDate
                )
                var created = api.createTask(authHeader, request)
                // Completed locally before it ever reached the server —
                // createTask can't express that, so finish the round trip
                // with an explicit complete call.
                if (task.isCompleted) {
                    created = api.completeTask(authHeader, created.id)
                }
                dao.deleteById(task.id) // drop the local_ placeholder row
                dao.upsert(created.toEntity(SyncState.SYNCED))
                true
            }

            SyncState.PENDING_UPDATE -> {
                val updated = api.completeTask(authHeader, task.id)
                dao.upsert(updated.toEntity(SyncState.SYNCED))
                true
            }

            SyncState.PENDING_DELETE -> {
                val response = api.deleteTask(authHeader, task.id)
                if (!response.isSuccessful) error("delete failed: ${response.code()}")
                dao.deleteById(task.id)
                true
            }

            SyncState.SYNCED -> true // shouldn't be in the pending set, but a no-op is safe
        }
    } catch (e: Exception) {
        false
    }

    private suspend fun syncTransaction(
        api: AccordApi,
        dao: TransactionDao,
        authHeader: String,
        transaction: TransactionEntity
    ): Boolean = try {
        val response = api.logPayment(
            authHeader,
            PaymentRequest(transaction.amount, transaction.merchantName)
        )
        dao.deleteById(transaction.id) // drop the local_ placeholder row
        dao.upsert(response.toEntity(SyncState.SYNCED))
        true
    } catch (e: Exception) {
        false
    }
}