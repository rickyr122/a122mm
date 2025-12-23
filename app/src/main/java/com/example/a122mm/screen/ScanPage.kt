
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.a122mm.helper.setScreenOrientation
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlin.math.min

@Composable
fun ScanPage(
    onClose: () -> Unit,
    onScanned: (pairCode: String, tvName: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasScanned by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    val haptic = LocalHapticFeedback.current
    var camera: Camera? by remember { mutableStateOf(null) }
    var torchOn by remember { mutableStateOf(false) }


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

    val insets = WindowInsets.safeDrawing.asPaddingValues()

    LaunchedEffect(Unit) {
        if (!isTablet) context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ───────── Camera (edge-to-edge, untouched) ─────────
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
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.LongPress
                                            )
                                            onScanned(code, "Smart TV")
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else imageProxy.close()
                        }



                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
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

        // ───────── Overlay + UI (safe-aware) ─────────
        ScannerOverlay(
            modifier = Modifier.fillMaxSize(),
            windowWidthFraction = 0.82f,
            windowAspectRatio = 1f,
            cornerRadius = 22.dp,
            dimAlpha = 0.60f,
            borderWidth = 2.dp,
            showScanLine = true,
            instructionText = "Make sure your QR code is in the center.",
            topInset = if (isTablet && isLandscape) insets.calculateTopPadding() else 0.dp,
            bottomInset = if (isTablet && isLandscape) insets.calculateBottomPadding() else 0.dp,
            limitHeight = isTablet && isLandscape,
            torchOn = torchOn,
            onToggleFlash = {
                torchOn = !torchOn
                camera?.cameraControl?.enableTorch(torchOn)
            },
            onClose = onClose
        )

    }

    DisposableEffect(Unit) {
        onDispose {
            camera?.cameraControl?.enableTorch(false)
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
    instructionText: String,
    topInset: Dp,
    bottomInset: Dp,
    limitHeight: Boolean,
    torchOn: Boolean,
    onToggleFlash: () -> Unit,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    val transition = rememberInfiniteTransition(label = "scanLine")
    val anim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLinePos"
    )

    Box(modifier) {

        // =======================
        // CAMERA OVERLAY CANVAS
        // =======================
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val topInsetPx = with(density) { topInset.toPx() }
            val bottomInsetPx = with(density) { bottomInset.toPx() }

            val safeHeight = h - topInsetPx - bottomInsetPx

            val windowW = w * windowWidthFraction
            val desiredWindowH = windowW / windowAspectRatio

            val windowH =
                if (limitHeight) min(desiredWindowH, safeHeight * 0.95f)
                else desiredWindowH

            val left = (w - windowW) / 2f
            val top = topInsetPx + (safeHeight - windowH) / 2f
            val right = left + windowW
            val bottom = top + windowH

            val cr = with(density) { cornerRadius.toPx() }
            val bw = with(density) { borderWidth.toPx() }

            // Dim background
            drawRect(Color.Black.copy(alpha = dimAlpha))

            // Clear scan window
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(windowW, windowH),
                cornerRadius = CornerRadius(cr, cr),
                blendMode = BlendMode.Clear
            )

            // Border
            drawRoundRect(
                color = Color.White.copy(alpha = 0.65f),
                topLeft = Offset(left, top),
                size = Size(windowW, windowH),
                cornerRadius = CornerRadius(cr, cr),
                style = Stroke(width = bw)
            )

            // Scan line
            if (showScanLine) {
                val y = top + windowH * anim
                drawLine(
                    color = Color.White.copy(alpha = 0.75f),
                    start = Offset(left + windowW * 0.08f, y),
                    end = Offset(right - windowW * 0.08f, y),
                    strokeWidth = with(density) { 2.dp.toPx() }
                )
            }
        }

        // =======================
        // TOP LEFT — BACK BUTTON
        // =======================
        val iconSize = if (isTablet) 42.dp else 34.dp

        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .systemBarsPadding()
                .padding(start = 16.dp, top = 16.dp)
                .size(iconSize)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { onClose() }
                .padding(6.dp)
        )

        // =======================
        // TOP RIGHT — FLASH BUTTON
        // =======================
        Icon(
            imageVector = if (torchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
            contentDescription = "Flash",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(end = 16.dp, top = 16.dp)
                .size(iconSize)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { onToggleFlash() }
                .padding(6.dp)
        )

        // =======================
        // BOTTOM — INSTRUCTION TEXT
        // =======================
        Text(
            text = instructionText,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = if (isTablet) 18.sp else 15.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(bottom = 24.dp)
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




