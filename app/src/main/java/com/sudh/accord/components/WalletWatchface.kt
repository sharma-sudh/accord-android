package com.sudh.accord.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WalletWatchface(
    walletBalance: Double,
    amountSpent: Double,
    monthlyBudget: Double,
    modifier: Modifier = Modifier
) {
    val earnedProgress = if (monthlyBudget > 0)
        ((walletBalance + amountSpent) / monthlyBudget).coerceIn(0.0, 1.0) else 0.0
    val spentProgress = if (monthlyBudget > 0)
        (amountSpent / monthlyBudget).coerceIn(0.0, 1.0) else 0.0

    val earnedSweep = (earnedProgress * 360f).toFloat()
    val spentSweep  = (spentProgress  * 360f).toFloat()

    val trackColor  = MaterialTheme.colorScheme.surfaceVariant
    val earnedColor = MaterialTheme.colorScheme.primary
    val spentColor  = MaterialTheme.colorScheme.tertiary
    val strokeWidth = 20f

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Dual-arc ──────────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(110.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val diameter = size.minDimension
                val inset    = strokeWidth / 2f
                val topLeft  = Offset(
                    x = (size.width  - diameter) / 2f + inset,
                    y = (size.height - diameter) / 2f + inset
                )
                val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)

                // Track
                drawArc(
                    color      = trackColor,
                    startAngle = 0f, sweepAngle = 360f,
                    useCenter  = false,
                    topLeft    = topLeft, size = arcSize,
                    style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                // Earned arc (behind)
                drawArc(
                    color      = earnedColor,
                    startAngle = -90f, sweepAngle = earnedSweep,
                    useCenter  = false,
                    topLeft    = topLeft, size = arcSize,
                    style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                // Spent arc (on top)
                drawArc(
                    color      = spentColor,
                    startAngle = -90f, sweepAngle = spentSweep,
                    useCenter  = false,
                    topLeft    = topLeft, size = arcSize,
                    style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Text(
                text       = "₹${"%.0f".format(walletBalance)}",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }

        // ── Labels ────────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text  = "wallet balance",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text       = "spent ₹${"%.0f".format(amountSpent)}",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = "of ₹${"%.0f".format(monthlyBudget)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}