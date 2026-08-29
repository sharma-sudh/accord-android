package com.sudh.accord.viewmodel

import com.sudh.accord.dto.TaskDto

/**
 * One row in the conflict resolution sheet: this device's pending edit next
 * to the server's copy at the moment the version check failed (see
 * TaskEntity.conflictServerSnapshot). [local] and [server] share an id.
 */
data class ConflictPair(
    val local: TaskDto,
    val server: TaskDto,
)

data class ConflictUiState(
    val conflicts: List<ConflictPair> = emptyList(),
    // True only until the first emission from observeConflicts() lands —
    // there's no network call backing this screen, so this is brief and
    // exists mainly to avoid a flash of "no conflicts" before Room's first
    // read completes.
    val isLoading: Boolean = true,
    // Id currently being resolved, so its row can show a spinner and both
    // of its buttons can be disabled without blocking the rest of the list.
    val resolvingId: String? = null,
    val actionError: String? = null,
)