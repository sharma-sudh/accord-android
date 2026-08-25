package com.sudh.accord.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import androidx.core.net.toUri
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    navController: NavController,
    onQrDecoded: (merchantName: String, upiId: String) -> Unit
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var scanError by remember { mutableStateOf<String?>(null) }

    // Errors are transient — clear automatically so the scanner recovers
    // on its own once the user moves off an invalid code.
    LaunchedEffect(scanError) {
        if (scanError != null) {
            delay(2000.milliseconds)
            scanError = null
        }
    }

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
                if (hasCameraPermission) {
                    CameraPreview(
                        onQrDecoded = onQrDecoded,
                        onInvalidQr = { scanError = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Icon(
                        imageVector  = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier     = Modifier.size(64.dp),
                        tint         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }

                // Corner bracket decorations sit above the preview
                CornerBrackets(color = cornerColor, size = 260.dp)
            }

            if (!hasCameraPermission) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text  = "Camera permission is needed to scan QR codes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick  = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Camera Permission")
                    }
                }
            } else if (scanError != null) {
                Text(
                    text      = scanError!!,
                    style     = MaterialTheme.typography.labelMedium,
                    color     = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    onQrDecoded: (merchantName: String, upiId: String) -> Unit,
    onInvalidQr: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Guards against firing onQrDecoded more than once per screen visit,
    // since ImageAnalysis will keep delivering frames while we navigate away.
    val hasDecoded = remember { AtomicBoolean(false) }
    // Guards against overlapping ML Kit calls — frames arrive faster than
    // a single decode round-trip, so only one is in flight at a time.
    val isAnalyzing = remember { AtomicBoolean(false) }
    // Dedupes the error callback so holding an invalid QR in frame doesn't
    // spam onInvalidQr (and Compose recomposition) on every analyzed frame.
    val lastInvalidValue = remember { AtomicReference<String?>(null) }
    val analysisExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage == null || hasDecoded.get() ||
                                !isAnalyzing.compareAndSet(false, true)
                            ) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    val rawValue = barcodes.firstOrNull()?.rawValue
                                        ?: return@addOnSuccessListener

                                    val parsed = parseUpiQr(rawValue)
                                    if (parsed != null) {
                                        if (hasDecoded.compareAndSet(false, true)) {
                                            val (merchantName, upiId) = parsed
                                            onQrDecoded(merchantName, upiId)
                                        }
                                    } else if (lastInvalidValue.getAndSet(rawValue) != rawValue) {
                                        onInvalidQr("Not a valid UPI QR code — try again")
                                    }
                                }
                                .addOnCompleteListener {
                                    isAnalyzing.set(false)
                                    imageProxy.close()
                                }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (_: Exception) {
                    // Camera binding can fail if the lifecycle is already
                    // destroyed by the time this listener runs; nothing to
                    // recover from here, the screen will just show no preview.
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

/**
 * Parses a UPI deep link — "upi://pay?pa=<vpa>&pn=<name>&am=<amount>&cu=INR..." —
 * into a (merchantName, upiId) pair.
 *
 * `pa` (payee address / VPA) is required; without it there's no UPI ID to pay
 * into, so the QR isn't usable regardless of what else it contains. `pn`
 * (payee name) is optional per the spec, so it falls back to the VPA itself.
 * `Uri.getQueryParameter` URL-decodes values automatically (e.g. "Campus+Cafe"
 * or "Campus%20Cafe" both come back as "Campus Cafe").
 *
 * Returns null for anything that isn't a well-formed UPI pay URI — a plain
 * website QR, a WiFi QR, random text, etc. — so the caller can surface an
 * error instead of routing garbage into the payment flow.
 */
private fun parseUpiQr(rawValue: String): Pair<String, String>? {
    val uri = runCatching { rawValue.toUri() }.getOrNull() ?: return null

    if (uri.scheme?.lowercase() != "upi" || uri.host?.lowercase() != "pay") return null

    val payeeAddress = uri.getQueryParameter("pa")?.takeIf { it.isNotBlank() } ?: return null
    val payeeName    = uri.getQueryParameter("pn")?.takeIf { it.isNotBlank() } ?: payeeAddress

    return payeeName to payeeAddress
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