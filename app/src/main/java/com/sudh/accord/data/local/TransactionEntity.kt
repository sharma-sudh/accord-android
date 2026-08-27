package com.sudh.accord.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Mirrors the backend's TransactionResponseDto shape, plus [syncState]. There
 * is no GET-transactions endpoint on the backend, so unlike tasks this table
 * is never reconciled against a server list — rows only ever move
 * PENDING_CREATE -> SYNCED via SyncWorker's logPayment call.
 *
 * Deliberately exempt from the version-vector conflict scheme used for
 * tasks (see TaskEntity.version / TaskSyncRequest): wallet/transaction
 * writes are server-wins, always — no `version` column, no merge, no
 * CONFLICT state. logPayment's response simply replaces the local row.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val type: String,
    val merchantName: String,
    val createdAt: String?,
    val syncState: SyncState
)