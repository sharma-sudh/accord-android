package com.sudh.accord.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Mirrors the backend's TransactionResponseDto shape, plus [syncState]. There
 * is no GET-transactions endpoint on the backend, so unlike tasks this table
 * is never reconciled against a server list — rows only ever move
 * PENDING_CREATE -> SYNCED via SyncWorker's logPayment call.
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