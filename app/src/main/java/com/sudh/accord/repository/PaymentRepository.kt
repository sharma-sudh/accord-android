package com.sudh.accord.repository

import android.content.Context
import com.sudh.accord.data.local.AccordDatabase
import com.sudh.accord.data.local.SyncState
import com.sudh.accord.data.local.TransactionEntity
import com.sudh.accord.data.local.toDto
import com.sudh.accord.dto.TransactionResponseDto
import com.sudh.accord.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

/**
 * Local-first: a logged payment lands in Room immediately (works with zero
 * connectivity) and is queued for SyncWorker to push via
 * POST /api/v1/transactions/payment once a connection exists. PaymentViewModel
 * is unchanged — confirmPayment() calling logPayment() now simply never
 * blocks on the network.
 */
class PaymentRepository(context: Context) {

    private val appContext = context.applicationContext
    private val transactionDao = AccordDatabase.getInstance(appContext).transactionDao()

    fun observeTransactions(): Flow<List<TransactionResponseDto>> =
        transactionDao.observeTransactions().map { list -> list.map { it.toDto() } }

    suspend fun logPayment(token: String, merchantName: String, amount: Double): Result<TransactionResponseDto> {
        val entity = TransactionEntity(
            id = "local_${UUID.randomUUID()}",
            amount = amount,
            type = "PAYMENT",
            merchantName = merchantName,
            createdAt = Instant.now().toString(),
            syncState = SyncState.PENDING_CREATE
        )
        transactionDao.upsert(entity)
        SyncScheduler.enqueueNow(appContext)
        return Result.success(entity.toDto())
    }
}