package com.sudh.accord.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    navController: NavController,
    onQrDecoded: (merchantName: String, upiId: String) -> Unit
) {
    val scanLineY by rememberInfiniteTransition(label = "scan").animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
        ) {
            Text(
                text      = "Point your camera at a UPI QR code",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Viewfinder mockup
            val cornerColor = MaterialTheme.colorScheme.primary
            val scanColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .drawBehind {
                        // Animated scan line
                        val y = scanLineY * size.height
                        drawLine(
                            brush   = Brush.horizontalGradient(
                                listOf(Color.Transparent, scanColor, Color.Transparent)
                            ),
                            start   = Offset(0f, y),
                            end     = Offset(size.width, y),
                            strokeWidth = 3.dp.toPx(),
                            cap     = StrokeCap.Round
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Corner bracket decorations
                CornerBrackets(color = cornerColor, size = 260.dp)

                Icon(
                    imageVector  = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier     = Modifier.size(64.dp),
                    tint         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text  = "Camera permission required in production",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Button(
                    onClick = { onQrDecoded("Swiggy", "swiggy@upi") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simulate Scan — Swiggy")
                }

                OutlinedButton(
                    onClick  = { onQrDecoded("Campus Cafe", "campuscafe@okaxis") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simulate Scan — Campus Cafe")
                }
            }
        }
    }
}

/** Draws the four L-shaped corner brackets inside the viewfinder box. */
@Composable
private fun CornerBrackets(color: Color, size: Dp) {
    Box(modifier = Modifier.size(size)) {
        val strokeWidth = 3.dp
        val armLength   = 24.dp
        val inset       = 16.dp

        // Top-left
        CornerBracket(Modifier.align(Alignment.TopStart).padding(inset), color, strokeWidth, armLength, flipH = false, flipV = false)
        // Top-right
        CornerBracket(Modifier.align(Alignment.TopEnd).padding(inset), color, strokeWidth, armLength, flipH = true, flipV = false)
        // Bottom-left
        CornerBracket(Modifier.align(Alignment.BottomStart).padding(inset), color, strokeWidth, armLength, flipH = false, flipV = true)
        // Bottom-right
        CornerBracket(Modifier.align(Alignment.BottomEnd).padding(inset), color, strokeWidth, armLength, flipH = true, flipV = true)
    }
}

@Composable
private fun CornerBracket(
    modifier: Modifier,
    color: Color,
    strokeWidth: Dp,
    armLength: Dp,
    flipH: Boolean,
    flipV: Boolean
) {
    Box(
        modifier = modifier
            .size(armLength)
            .drawBehind {
                val sw  = strokeWidth.toPx()
                val arm = armLength.toPx()
                val cap = StrokeCap.Round

                val x1 = if (flipH) arm else 0f
                val x2 = if (flipH) 0f  else arm
                val y1 = if (flipV) arm else 0f
                val y2 = if (flipV) 0f  else arm

                // Horizontal arm
                drawLine(color = color, start = Offset(x1, y1), end = Offset(x2, y1), strokeWidth = sw, cap = cap)
                // Vertical arm
                drawLine(color = color, start = Offset(x1, y1), end = Offset(x1, y2), strokeWidth = sw, cap = cap)
            }
    )
}