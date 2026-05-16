// HomeScreen.kt
package com.sudh.accord.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onTaskComplete: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit,
) {
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var isDetailSheetOpen by remember { mutableStateOf(false) }

    val recurringTasks = remember(tasks) { tasks.filter { it.isRecurring } }
    val oneOffTasks = remember(tasks) { tasks.filter { !it.isRecurring } }

    if (isDetailSheetOpen && selectedTask != null) {
        TaskDetailSheet(
            task = selectedTask!!,
            onDismiss = {
                isDetailSheetOpen = false
                selectedTask = null
            },
            onDelete = { task ->
                onTaskDelete(task)
                isDetailSheetOpen = false
                selectedTask = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape  = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WalletWatchface(
                        walletBalance = walletBalance,
                        amountSpent   = amountSpent,
                        monthlyBudget = monthlyBudget,
                        modifier      = Modifier.weight(1f)
                    )

                    VerticalDivider(
                        modifier  = Modifier
                            .height(64.dp)
                            .padding(horizontal = 12.dp),
                        thickness = 1.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant
                    )

                    StreakBadge(streakDays = streakDays)
                }
            }
        }

        // ── Recurring tasks ──────────────────────────────────────────────────
        items(
            items = recurringTasks,
            key = { it.id }
        ) { task ->
            TaskRow(
                task = task,
                onComplete = { onTaskComplete(task) },
                onTap = {
                    selectedTask = task
                    isDetailSheetOpen = true
                }
            )
        }

        // ── Divider ──────────────────────────────────────────────────────────
        item {
            OneTimeDivider()
        }

        // ── One-off tasks ────────────────────────────────────────────────────
        items(
            items = oneOffTasks,
            key = { it.id }
        ) { task ->
            TaskRow(
                task = task,
                onComplete = { onTaskComplete(task) },
                onTap = {
                    selectedTask = task
                    isDetailSheetOpen = true
                }
            )
        }
    }
}

// ── Small private composables ────────────────────────────────────────────────

@Composable
private fun StreakBadge(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(min = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text  = "🔥",
            fontSize = 28.sp  // was headlineSmall, explicitly larger now
        )
        Text(
            text       = "$streakDays",
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text  = "day streak",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OneTimeDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "one-time",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}