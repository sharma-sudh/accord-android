package com.sudh.accord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.accord.AccordApplication
import com.sudh.accord.dto.TaskDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs both the conflict badge (just needs the count) and
 * ConflictResolutionSheet (needs the full yours-vs-theirs pairs). Hoisted
 * once in NavGraph like HomeViewModel/AnalyticsViewModel so both call sites
 * share the same instance and the same underlying Room flow.
 */
class ConflictViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository = getApplication<AccordApplication>().taskRepository

    private val _resolvingId = MutableStateFlow<String?>(null)
    private val _actionError = MutableStateFlow<String?>(null)

    // observeConflicts() only carries this device's side of each row — the
    // server snapshot is a separate suspend read per id (see
    // TaskRepository.getConflictServerSnapshot), so each emission is paired
    // up here before either consumer sees it.
    private val conflictPairs = taskRepository.observeConflicts()
        .map { locals ->
            locals.mapNotNull { local ->
                taskRepository.getConflictServerSnapshot(local.id)?.let { server ->
                    ConflictPair(local = local, server = server)
                }
            }
        }

    val uiState: StateFlow<ConflictUiState> =
        combine(conflictPairs, _resolvingId, _actionError) { pairs, resolvingId, actionError ->
            ConflictUiState(
                conflicts = pairs,
                isLoading = false,
                resolvingId = resolvingId,
                actionError = actionError
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConflictUiState())

    // Keeps this device's edit, but still adopts the server's version as the
    // new baseVersion — TaskRepository.resolveConflict applies whatever
    // version is on the TaskDto it's given, and only the server snapshot
    // knows the version that was actually rejected.
    fun keepLocal(pair: ConflictPair) = resolve(pair.local.copy(version = pair.server.version))

    // Discards this device's edit entirely and accepts the server's copy —
    // its version is already the correct new baseVersion.
    fun keepServer(pair: ConflictPair) = resolve(pair.server)

    private fun resolve(resolved: TaskDto) {
        viewModelScope.launch {
            _resolvingId.value = resolved.id
            try {
                taskRepository.resolveConflict(resolved.id, resolved)
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Couldn't resolve that conflict"
            } finally {
                _resolvingId.value = null
            }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }
}