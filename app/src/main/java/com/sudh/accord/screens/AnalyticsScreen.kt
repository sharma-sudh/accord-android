package com.sudh.accord.screens

// ──────────────────────────────────────────────────────────────────────────────
//  Imports
// ──────────────────────────────────────────────────────────────────────────────
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sudh.accord.model.Task
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ──────────────────────────────────────────────────────────────────────────────
//  Screen
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Analytics screen for Accord.
 *
 * @param totalEarned   Cumulative wallet credits earned from completed tasks.
 * @param totalSpent    Cumulative amount spent from the wallet.
 * @param completionRate Overall task completion rate (0f–1f).
 * @param streakDays    Current daily streak count.
 * @param tasks         Full task list used for per-task breakdown.
 */
@Composable
fun AnalyticsScreen(
    totalEarned: Double,
    totalSpent: Double,
    completionRate: Float,
    streakDays: Int,
    tasks: List<Task>,
    modifier: Modifier = Modifier,
) {
    // Toggle lives here — purely UI state, not hoisted
    var selectedRange by remember { mutableStateOf("week") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
    ) {

        item {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // ── 1. Stat cards ────────────────────────────────────────────────────
        item {
            StatCardsGrid(
                totalEarned = totalEarned,
                totalSpent = totalSpent,
                completionRate = completionRate,
                streakDays = streakDays,
            )
        }

        // ── 2. Week / Month toggle ───────────────────────────────────────────
        item {
            RangeToggle(
                selected = selectedRange,
                onSelect = { selectedRange = it },
            )
        }

        // ── 3. Chart ─────────────────────────────────────────────────────────
        item {
            SpendingCompletionChart(selectedRange = selectedRange)
        }

        // ── 4. Task breakdown ────────────────────────────────────────────────
        item {
            TaskBreakdown(tasks = tasks)
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  1 · Stat cards — 2×2 grid via two Rows (matches HomeScreen header pattern)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatCardsGrid(
    totalEarned: Double,
    totalSpent: Double,
    completionRate: Float,
    streakDays: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Earned",
                value = "₹${totalEarned.toInt()}",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Spent",
                value = "₹${totalSpent.toInt()}",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Completion",
                value = "${(completionRate * 100).toInt()}%",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Streak",
                value = "${streakDays} days",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.7f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  2 · Week / Month toggle
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun RangeToggle(
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
    ) {
        listOf("week", "month").forEach { range ->
            val isSelected = selected == range
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface
                        else Color.Transparent
                    )
                    .height(36.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(
                    onClick = { onSelect(range) },
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = range.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  3 · Vico line chart — spending vs completion rate
// ──────────────────────────────────────────────────────────────────────────────

//  Hardcoded sample data — swap with real ViewModel data later.
private val weekSpending = listOf(120f, 80f, 200f, 60f, 150f, 90f, 170f)
private val weekCompletion = listOf(60f, 75f, 50f, 90f, 70f, 85f, 65f)   // % (0–100)

private val monthSpending = listOf(
    800f, 650f, 1100f, 400f, 950f, 1200f, 700f,
    600f, 900f, 1050f, 450f, 750f, 870f, 300f,
    980f, 660f, 1150f, 820f, 500f, 730f, 1010f,
    590f, 880f, 970f, 410f, 760f, 1080f, 630f, 490f, 840f,
)
private val monthCompletion = listOf(
    65f, 70f, 55f, 80f, 72f, 60f, 78f,
    68f, 74f, 58f, 83f, 69f, 75f, 90f,
    62f, 77f, 53f, 71f, 86f, 67f, 73f,
    79f, 64f, 57f, 88f, 76f, 61f, 82f, 70f, 66f,
)

@Composable
fun SpendingCompletionChart(selectedRange: String) {
    val modelProducer = remember { CartesianChartModelProducer() }

    // Re-populate the producer whenever the toggle changes
    LaunchedEffect(selectedRange) {
        withContext(Dispatchers.Default) {
            val spending = if (selectedRange == "week") weekSpending else monthSpending
            val completion = if (selectedRange == "week") weekCompletion else monthCompletion
            modelProducer.runTransaction {
                // Both series live in ONE lineSeries block — they map to the two
                // rememberLine() entries inside the single LineCartesianLayer.
                lineSeries {
                    series(spending)    // index 0 → spending line
                    series(completion)  // index 1 → completion % line
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // Chart legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LegendDot(color = MaterialTheme.colorScheme.primary, label = "Spending (₹)")
            LegendDot(color = MaterialTheme.colorScheme.tertiary, label = "Completion %")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            CartesianChartHost(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(12.dp),
                chart = rememberCartesianChart(
                    // Single layer, two lines — matches the two series() calls above.
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(Fill(Color(0xFF6750A4))),
                                areaFill = null,
                            ),
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(Fill(Color(0xFF7D5260))),
                                areaFill = null,
                            ),
                        ),
                    ),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                ),
                modelProducer = modelProducer,
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//  4 · Task breakdown — completion rate per task as animated progress bars
// ──────────────────────────────────────────────────────────────────────────────

// Hardcoded per-task completion rates for now — replace with real data later.
// Key is task name, value is 0f–1f.
private val hardcodedTaskCompletion = mapOf(
    "Morning workout"   to 0.85f,
    "Read 20 pages"     to 0.60f,
    "No social media"   to 0.40f,
    "Cook at home"      to 0.90f,
    "LeetCode daily"    to 0.70f,
)

@Composable
private fun TaskBreakdown(tasks: List<Task>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            text = "Task Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (tasks.isEmpty()) {
            Text(
                text = "No tasks yet — add tasks to see breakdown here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            tasks.forEach { task ->
                // Look up hardcoded rate; fall back to 0 until real data is wired
                val rate = hardcodedTaskCompletion[task.title] ?: 0f
                TaskProgressRow(task = task, completionRate = rate)
            }
        }
    }
}

@Composable
private fun TaskProgressRow(task: Task, completionRate: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(durationMillis = 600),
        label = "task_progress_${task.title}",
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${(completionRate * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}