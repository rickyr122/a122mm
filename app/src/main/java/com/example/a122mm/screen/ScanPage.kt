
import android.content.res.Configuration
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
//            topInset = if (isTablet && isLandscape) insets.calculateTopPadding() else 0.dp,
//            bottomInset = if (isTablet && isLandscape) insets.calculateBottomPadding() else 0.dp,
//            limitHeight = isTablet && isLandscape,
            onClose = onClose
        )
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
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    val insets = WindowInsets.safeDrawing.asPaddingValues()

    val transition = rememberInfiniteTransition(label = "scanLine")
    val anim by transition.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Box(modifier.fillMaxSize()) {

        /* =========================
           CANVAS (FULLSCREEN DIM)
           ========================= */
        Canvas(Modifier.fillMaxSize()) {
            val screenW = size.width
            val screenH = size.height

            val topInsetPx =
                if (isTablet && isLandscape) with(density) { insets.calculateTopPadding().toPx() }
                else 0f

            val bottomInsetPx =
                if (isTablet && isLandscape) with(density) { insets.calculateBottomPadding().toPx() }
                else 0f

            val safeHeight = screenH - topInsetPx - bottomInsetPx

            val windowW = screenW * windowWidthFraction
            val desiredWindowH = windowW / windowAspectRatio
            val windowH = min(desiredWindowH, safeHeight * 0.95f)

            val left = (screenW - windowW) / 2f
            val top = topInsetPx + (safeHeight - windowH) / 2f

            val cr = with(density) { cornerRadius.toPx() }
            val bw = with(density) { borderWidth.toPx() }

            // Dim
            drawRect(Color.Black.copy(alpha = dimAlpha))

            // Clear window
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
                style = Stroke(bw)
            )

            // Scan line
            if (showScanLine) {
                val y = top + windowH * anim
                drawLine(
                    Color.White.copy(alpha = 0.75f),
                    Offset(left + windowW * 0.08f, y),
                    Offset(left + windowW * 0.92f, y),
                    strokeWidth = with(density) { 2.dp.toPx() }
                )
            }
        }

        /* =========================
           BACK ARROW
           ========================= */
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = insets.calculateTopPadding() +
                            if (isTablet && isLandscape) 8.dp else 16.dp
                )
                .size(if (isTablet) 44.dp else 34.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable(onClick = onClose)
                .padding(6.dp)
        )

        /* =========================
           INSTRUCTION TEXT
           ========================= */
        Text(
            text = instructionText,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = if (isTablet) 18.sp else 15.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = insets.calculateBottomPadding() +
                            if (isTablet && isLandscape) 48.dp else 24.dp
                )
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




