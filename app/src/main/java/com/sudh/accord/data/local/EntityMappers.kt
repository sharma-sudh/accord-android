package com.sudh.accord.data.local

import com.sudh.accord.dto.TaskDto
import com.sudh.accord.dto.TransactionResponseDto

fun TaskDto.toEntity(state: SyncState): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    value = value,
    type = type,
    isCompleted = isCompleted,
    dueDate = dueDate,
    lastCompletedAt = lastCompletedAt,
    userId = userId,
    syncState = state
)

fun TaskEntity.toDto(): TaskDto = TaskDto(
    id = id,
    title = title,
    description = description,
    value = value,
    type = type,
    isCompleted = isCompleted,
    dueDate = dueDate,
    lastCompletedAt = lastCompletedAt,
    userId = userId
)

fun TransactionResponseDto.toEntity(state: SyncState): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type,
    merchantName = merchantName,
    createdAt = createdAt,
    syncState = state
)

fun TransactionEntity.toDto(): TransactionResponseDto = TransactionResponseDto(
    id = id,
    amount = amount,
    type = type,
    merchantName = merchantName,
    createdAt = createdAt
)