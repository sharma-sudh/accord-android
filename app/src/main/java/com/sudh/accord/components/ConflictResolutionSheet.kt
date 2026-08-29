package com.sudh.accord.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudh.accord.viewmodel.ConflictPair
import com.sudh.accord.viewmodel.ConflictViewModel

/**
 * Surfaces rows SyncWorker flagged CONFLICT (see TaskDao.observeConflicts) —
 * these never show up in the normal task list, so this sheet is the only
 * place a user can see or clear one. Same pattern as SettingsSheet /
 * TaskDetailSheet: a plain ModalBottomSheet, default-constructed viewModel().
 *
 * The real-world surface for a conflict is narrow today (SyncWorker's only
 * PENDING_UPDATE trigger is task completion), so this only ever needs to
 * compare isCompleted/lastCompletedAt. If the merge scheme grows to cover
 * more editable fields later, each ConflictCard is the place to add
 * per-field pickers instead of the current all-or-nothing pick-a-side.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictResolutionSheet(
    onDismiss: () -> Unit,
    viewModel: ConflictViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionError) {
        val message = uiState.actionError
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearActionError()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Resolve conflicts", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "These tasks changed on another device before this one's edit " +
                            "reached the server. Pick which version to keep.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.conflicts.isEmpty() && !uiState.isLoading) {
                    EmptyConflictsState()
                } else {
                    uiState.conflicts.forEach { pair ->
                        ConflictCard(
                            pair = pair,
                            isResolving = uiState.resolvingId == pair.local.id,
                            onKeepLocal = { viewModel.keepLocal(pair) },
                            onKeepServer = { viewModel.keepServer(pair) }
                        )
                    }
                }

                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ConflictCard(
    pair: ConflictPair,
    isResolving: Boolean,
    onKeepLocal: () -> Unit,
    onKeepServer: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = pair.local.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConflictSideColumn(
                    modifier = Modifier.weight(1f),
                    label = "Yours",
                    task = pair.local
                )
                ConflictSideColumn(
                    modifier = Modifier.weight(1f),
                    label = "Theirs",
                    task = pair.server
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onKeepLocal,
                    enabled = !isResolving,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isResolving) ButtonSpinner() else Text("Keep mine")
                }
                Button(
                    onClick = onKeepServer,
                    enabled = !isResolving,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    if (isResolving) ButtonSpinner() else Text("Keep theirs")
                }
            }
        }
    }
}

@Composable
private fun ConflictSideColumn(
    modifier: Modifier = Modifier,
    label: String,
    task: com.sudh.accord.dto.TaskDto,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (task.isCompleted) "Completed" else "Not completed",
            style = MaterialTheme.typography.bodyMedium
        )
        // lastCompletedAt is a full ISO instant; a date-only slice is enough
        // to tell the two sides apart without a formatting dependency (same
        // shorthand TaskDetailSheet already uses).
        task.lastCompletedAt?.let {
            Text(
                text = it.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ButtonSpinner() {
    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
}

@Composable
private fun EmptyConflictsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text("All resolved", style = MaterialTheme.typography.titleSmall)
    }
}