package com.sudh.accord.screens

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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sudh.accord.model.AnalyticsSeriesPoint
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

@Composable
fun AnalyticsScreen(
    selectedRange: String,
    totalEarned: Double,
    totalSpent: Double,
    completionRate: Float,
    streakDays: Int?,
    series: List<AnalyticsSeriesPoint>,
    taskBreakdown: Map<String, Long>,
    isEmptyState: Boolean,
    isLoading: Boolean,
    error: String?,
    onRangeSelect: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Loading ───────────────────────────────────────────────────────────────
    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // ── Load error ────────────────────────────────────────────────────────────
    if (error != null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

        item {
            StatCardsGrid(
                totalEarned = totalEarned,
                totalSpent = totalSpent,
                completionRate = completionRate,
                streakDays = streakDays,
            )
        }

        item {
            RangeToggle(
                selected = selectedRange,
                onSelect = onRangeSelect,
            )
        }

        item {
            if (isEmptyState) {
                EmptyAnalyticsChart()
            } else {
                SpendingCompletionChart(series = series)
            }
        }

        item {
            TaskBreakdown(taskBreakdown = taskBreakdown, isEmptyState = isEmptyState)
        }
    }
}

@Composable
private fun StatCardsGrid(
    totalEarned: Double,
    totalSpent: Double,
    completionRate: Float,
    streakDays: Int?,
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
            // streakDays is null until 0.4.0's streak logic exists server-side —
            // hide the card entirely rather than render a fake "0 days".
            if (streakDays != null) {
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

// Real chart, driven by the backend's already-bucketed, zero-filled series.
// Second line is raw completed-task count per day, not a percentage — the
// backend has no "total possible completions" concept to build a rate from.
@Composable
fun SpendingCompletionChart(series: List<AnalyticsSeriesPoint>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(series) {
        withContext(Dispatchers.Default) {
            val spending  = series.map { it.spent }
            val completed = series.map { it.completedCount.toFloat() }
            modelProducer.runTransaction {
                lineSeries {
                    series(spending)
                    series(completed)
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LegendDot(color = MaterialTheme.colorScheme.primary,   label = "Spending (₹)")
            LegendDot(color = MaterialTheme.colorScheme.tertiary,  label = "Tasks completed")
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
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(Fill(MaterialTheme.colorScheme.primary)),
                                areaFill = null,
                            ),
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(Fill(MaterialTheme.colorScheme.tertiary)),
                                areaFill = null,
                            ),
                        ),
                    ),
                    startAxis  = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                ),
                modelProducer = modelProducer,
            )
        }
    }
}

// New-user empty state (0.2.0 design doc, point 3): a blurred, non-interactive
// dummy chart with an overlay message, instead of a real chart full of zeros.
@Composable
private fun EmptyAnalyticsChart() {
    val dummySeries = remember {
        listOf(40f, 65f, 30f, 80f, 55f, 70f, 45f)
    }

    Box(contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.blur(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LegendDot(color = MaterialTheme.colorScheme.primary,   label = "Spending (₹)")
                LegendDot(color = MaterialTheme.colorScheme.tertiary,  label = "Tasks completed")
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                val modelProducer = remember { CartesianChartModelProducer() }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.Default) {
                        modelProducer.runTransaction {
                            lineSeries {
                                series(dummySeries)
                                series(dummySeries.reversed())
                            }
                        }
                    }
                }
                CartesianChartHost(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(12.dp),
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(
                            lineProvider = LineCartesianLayer.LineProvider.series(
                                LineCartesianLayer.rememberLine(
                                    fill = LineCartesianLayer.LineFill.single(Fill(MaterialTheme.colorScheme.primary)),
                                    areaFill = null,
                                ),
                                LineCartesianLayer.rememberLine(
                                    fill = LineCartesianLayer.LineFill.single(Fill(MaterialTheme.colorScheme.tertiary)),
                                    areaFill = null,
                                ),
                            ),
                        ),
                        startAxis  = VerticalAxis.rememberStart(),
                        bottomAxis = HorizontalAxis.rememberBottom(),
                    ),
                    modelProducer = modelProducer,
                )
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            ),
            elevation = CardDefaults.cardElevation(4.dp),
        ) {
            Text(
                text = "Complete a few tasks to unlock your analytics",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
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

// Replaces the old hardcoded taskCompletionRates 0-100% bars: the backend only
// gives raw completion counts per task (no per-task denominator to build a
// true rate from), so this ranks tasks by count with bars relative to the
// task with the most completions in range.
@Composable
private fun TaskBreakdown(
    taskBreakdown: Map<String, Long>,
    isEmptyState: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Task Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (taskBreakdown.isEmpty()) {
            Text(
                text = if (isEmptyState)
                    "No tasks yet — add tasks to see breakdown here."
                else
                    "No completions in this range yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val maxCount = taskBreakdown.values.max()
            val sorted = taskBreakdown.entries.sortedByDescending { it.value }
            sorted.forEach { (title, count) ->
                TaskProgressRow(
                    title = title,
                    count = count,
                    fraction = count.toFloat() / maxCount.toFloat(),
                )
            }
        }
    }
}

@Composable
private fun TaskProgressRow(title: String, count: Long, fraction: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue    = fraction,
        animationSpec  = tween(durationMillis = 600),
        label          = "task_progress_${title}",
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        LinearProgressIndicator(
            progress   = { animatedProgress },
            modifier   = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color      = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
