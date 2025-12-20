
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@Composable
fun ScanPage(
    onClose: () -> Unit,
    onScanned: (pairCode: String, tvName: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasScanned by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    var granted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok ->
        granted = ok
        if (!ok) onClose()
    }

    LaunchedEffect(Unit) {
        launcher.launch(android.Manifest.permission.CAMERA)
    }

    // 🔹 ROOT (full bleed)
    Box(modifier = Modifier.fillMaxSize()) {

        // --- Camera (edge to edge) ---
        if (granted) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture =
                        ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)

                        val scanner = BarcodeScanning.getClient()

                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        analysis.setAnalyzer(
                            ContextCompat.getMainExecutor(ctx)
                        ) { imageProxy ->
                            val media = imageProxy.image
                            if (media != null && !hasScanned) {
                                val img = InputImage.fromMediaImage(
                                    media,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                scanner.process(img)
                                    .addOnSuccessListener { codes ->
                                        val raw = codes.firstOrNull()?.rawValue
                                        val code = extractPairCode(raw)
                                        if (!code.isNullOrBlank()) {
                                            hasScanned = true
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onScanned(code, "Smart TV")
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else imageProxy.close()
                        }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )
        }

        // 🔹 SAFE UI LAYER (respects system bars)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding() // ✅ handles status + nav bar
        ) {

            // Overlay (window + scan line)
            ScannerOverlay(
                modifier = Modifier.fillMaxSize(),
                windowWidthFraction = 0.82f,
                windowAspectRatio = 1f,
                cornerRadius = 22.dp,
                dimAlpha = 0.60f,
                borderWidth = 2.dp,
                showScanLine = true,
                instructionText = "Make sure your QR code is in the center."
            )

            // Back button (now safe)
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .size(34.dp)
                    .align(Alignment.TopStart)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable { onClose() }
                    .padding(6.dp)
            )
        }
    }
}

@Composable
private fun ScannerOverlay(
    modifier: Modifier = Modifier,
    windowWidthFraction: Float,
    windowAspectRatio: Float,
    cornerRadius: Dp,
    dimAlpha: Float,
    borderWidth: Dp,
    showScanLine: Boolean,
    instructionText: String
) {
    val density = LocalDensity.current

    // Infinite scan line animation (top -> bottom -> top)
    val transition = rememberInfiniteTransition(label = "scanLine")
    val anim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLinePos"
    )

    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val windowW = w * windowWidthFraction
            val windowH = windowW / windowAspectRatio

            val left = (w - windowW) / 2f
            val top = (h - windowH) / 2f
            val right = left + windowW
            val bottom = top + windowH

            val cr = with(density) { cornerRadius.toPx() }
            val bw = with(density) { borderWidth.toPx() }

            // 1) Draw dim over whole screen
            drawRect(
                color = Color.Black.copy(alpha = dimAlpha)
            )

            // 2) "Clear" the center window by drawing it with BlendMode.Clear
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(windowW, windowH),
                cornerRadius = CornerRadius(cr, cr),
                blendMode = BlendMode.Clear
            )

            // 3) Draw a white border around the window
            drawRoundRect(
                color = Color.White.copy(alpha = 0.65f),
                topLeft = Offset(left, top),
                size = Size(windowW, windowH),
                cornerRadius = CornerRadius(cr, cr),
                style = Stroke(width = bw)
            )

            // 4) Scan line
            if (showScanLine) {
                val y = top + (windowH * anim)
                drawLine(
                    color = Color.White.copy(alpha = 0.75f),
                    start = Offset(left + (windowW * 0.08f), y),
                    end = Offset(right - (windowW * 0.08f), y),
                    strokeWidth = with(density) { 2.dp.toPx() }
                )
            }
        }

        // Instruction text at bottom (like reference)
        Text(
            text = instructionText,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp)
        )
    }
}

// same helper as before
private fun extractPairCode(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val direct = raw.trim()
    // accept 7501-1915 in a plain string
    Regex("""\d{4}-\d{4}""").find(direct)?.let { return it.value }

    // accept URL like ...?code=7501-1915
    return try {
        Uri.parse(raw).getQueryParameter("code")?.trim()
    } catch (_: Throwable) {
        null
    }
}




