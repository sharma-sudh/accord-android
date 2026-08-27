package com.sudh.accord.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.sudh.accord.components.TaskDetailSheet
import com.sudh.accord.components.TaskRow
import com.sudh.accord.components.WalletWatchface
import com.sudh.accord.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    tasks: List<Task>,
    walletBalance: Double,
    amountSpent: Double,
    monthlyBudget: Double,
    streakDays: Int,
    isLoading: Boolean,
    error: String?,
    actionError: String?,
    showNotificationNudge: Boolean,
    hasEverAddedTask: Boolean,
    onDismissNotificationNudge: () -> Unit,
    onRetry: () -> Unit,
    onTaskComplete: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit,
    onActionErrorShown: () -> Unit,
) {
    var selectedTask      by remember { mutableStateOf<Task?>(null) }
    var isDetailSheetOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionError) {
        if (actionError != null) {
            snackbarHostState.showSnackbar(actionError)
            onActionErrorShown()
        }
    }

    val recurringTasks = remember(tasks) { tasks.filter { it.isRecurring } }
    val oneOffTasks    = remember(tasks) { tasks.filter { !it.isRecurring } }

    if (isDetailSheetOpen && selectedTask != null) {
        TaskDetailSheet(
            task      = selectedTask!!,
            onDismiss = {
                isDetailSheetOpen = false
                selectedTask = null
            },
            onDelete  = { task ->
                onTaskDelete(task)
                isDetailSheetOpen = false
                selectedTask = null
            }
        )
    }

    // ── Loading ───────────────────────────────────────────────────────────────
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // ── Load error ────────────────────────────────────────────────────────────
    if (error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text  = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(onClick = onRetry) { Text("Retry") }
            }
        }
        return
    }

    val isEmpty = tasks.isEmpty()

    // ── Content ───────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Hero wallet card ─────────────────────────────────────────────────
            item {
                WalletHeroCard(
                    walletBalance = walletBalance,
                    amountSpent   = amountSpent,
                    monthlyBudget = monthlyBudget,
                    streakDays    = streakDays
                )
            }

            if (showNotificationNudge) {
                item {
                    NotificationNudgeCard(onDismiss = onDismissNotificationNudge)
                }
            }

            if (isEmpty) {
                item {
                    // hasEverAddedTask is independent of the current list,
                    // so a recurring task's cycle reset (list emptying,
                    // then repopulating on the next cycle) never flips this
                    // back to the "yet" copy once the user has ever added
                    // a task — see HomeUiState.hasEverAddedTask.
                    if (hasEverAddedTask) AllCaughtUpState() else NoTasksYetState()
                }
            } else {
                if (recurringTasks.isNotEmpty()) {
                    item { SectionLabel(text = "Recurring") }
                    items(items = recurringTasks, key = { it.id }) { task ->
                        TaskRow(
                            task       = task,
                            onComplete = { onTaskComplete(task) },
                            onTap      = { selectedTask = task; isDetailSheetOpen = true }
                        )
                    }
                }

                if (oneOffTasks.isNotEmpty()) {
                    item { SectionLabel(text = "One-time") }
                    items(items = oneOffTasks, key = { it.id }) { task ->
                        TaskRow(
                            task       = task,
                            onComplete = { onTaskComplete(task) },
                            onTap      = { selectedTask = task; isDetailSheetOpen = true }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Hero card ────────────────────────────────────────────────────────────────

@Composable
private fun WalletHeroCard(
    walletBalance: Double,
    amountSpent: Double,
    monthlyBudget: Double,
    streakDays: Int,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape          = RoundedCornerShape(28.dp),
        color          = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
    ) {
        Box {
            // Soft ambient glow, single restrained accent, tucked in the corner.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 36.dp, y = (-36).dp)
                    .size(160.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "This month",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StreakPill(streakDays = streakDays)
                }

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    WalletWatchface(
                        walletBalance = walletBalance,
                        amountSpent   = amountSpent,
                        monthlyBudget = monthlyBudget,
                    )

                    Spacer(Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = "Wallet balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text       = "₹${"%.0f".format(walletBalance)}",
                            style      = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(12.dp))

                        LegendChip(
                            dotColor = MaterialTheme.colorScheme.primary,
                            label    = "Earned",
                            value    = "₹${"%.0f".format(walletBalance + amountSpent)}"
                        )
                        Spacer(Modifier.height(6.dp))
                        LegendChip(
                            dotColor = MaterialTheme.colorScheme.tertiary,
                            label    = "Spent",
                            value    = "₹${"%.0f".format(amountSpent)}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakPill(streakDays: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Whatshot,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text       = "$streakDays day streak",
            style      = MaterialTheme.typography.labelMedium,
            color      = MaterialTheme.colorScheme.onTertiaryContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LegendChip(dotColor: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Notification nudge ───────────────────────────────────────────────────────

@Composable
private fun NotificationNudgeCard(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "Turn on notifications",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text  = "Get a reminder before your tasks are due.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Section label ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text.uppercase(),
        style      = MaterialTheme.typography.labelSmall,
        color      = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(start = 28.dp, top = 20.dp, bottom = 8.dp)
    )
}

// ── Empty states ─────────────────────────────────────────────────────────────

@Composable
private fun NoTasksYetState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 32.dp)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Checklist,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "No tasks yet",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = "Tap + below to add something worth crediting.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// Shown instead of NoTasksYetState once the user has ever added a task
// (see HomeUiState.hasEverAddedTask) and the current list is empty because
// everything due right now is done — not because nothing was ever created.
@Composable
private fun AllCaughtUpState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 32.dp)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "All caught up",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = "Nothing due right now. Recurring tasks will reappear next cycle.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}