@file:OptIn(
    ExperimentalComposeUiApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.a122mm.screen

//import androidx.compose.ui.ExperimentalComposeUiApi
import android.app.Activity
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.example.a122mm.helper.getDriveUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import androidx.compose.material3.SliderDefaults.Thumb
import androidx.compose.ui.draw.scale


@Composable
fun CustomVideoPlayer(
    videoId: String,
    subtitleId: String?,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val surfaceView = remember { SurfaceView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }}

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(getDriveUrl(videoId)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            exoPlayer.release()
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(Unit) {
        activity?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        onDispose {
            activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    DisposableEffect(surfaceView) {
        exoPlayer.setVideoSurfaceView(surfaceView)
        onDispose {}
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
        AndroidView(factory = { surfaceView }, modifier = Modifier.fillMaxSize())

        // Subtitle renderer
        subtitleId?.let {
            SubtitleOverlay(exoPlayer, subtitleId)
        }

        // Controls overlay
        CustomControls(exoPlayer, navController)
    }
}

@Composable
fun SubtitleOverlay(player: ExoPlayer, subtitleId: String) {
    var subtitles by remember { mutableStateOf<List<SubtitleLine>>(emptyList()) }
    var currentText by remember { mutableStateOf("") }

    LaunchedEffect(subtitleId) {
        subtitles = parseSrt(getDriveUrl(subtitleId))
    }

    LaunchedEffect(player) {
        while (true) {
            delay(100)
            val positionMs = player.currentPosition
            currentText = subtitles.firstOrNull {
                positionMs in it.startMs..it.endMs
            }?.text ?: ""
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 48.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (currentText.isNotEmpty()) {
            BasicText(
                text = currentText,
                modifier = Modifier
                    .background(Color(0x80000000))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                style = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

data class SubtitleLine(val startMs: Long, val endMs: Long, val text: String)

suspend fun parseSrt(url: String): List<SubtitleLine> = withContext(Dispatchers.IO) {
    val lines = mutableListOf<SubtitleLine>()
    try {
        val reader = BufferedReader(InputStreamReader(URL(url).openStream()))
        var line: String? = reader.readLine()
        while (line != null) {
            reader.readLine()?.let { timeLine ->
                val (start, end) = timeLine.split(" --> ").map { parseSrtTime(it.trim()) }
                val textBuilder = StringBuilder()
                while (true) {
                    val next = reader.readLine() ?: break
                    if (next.isBlank()) break
                    textBuilder.appendLine(next)
                }
                lines.add(SubtitleLine(start, end, textBuilder.toString().trim()))
            }
            line = reader.readLine()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    lines
}

fun parseSrtTime(time: String): Long {
    val (hms, ms) = time.split(",", limit = 2)
    val parts = hms.split(":").map { it.toInt() }
    return (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000L + ms.toLong()
}

@Composable
fun CustomControls(player: ExoPlayer, navController: NavController) {
    var isPlaying by remember { mutableStateOf(player.isPlaying) }

    DisposableEffect(player) {
        val callback = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
        }
        player.addListener(callback)

        onDispose {
            player.removeListener(callback)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    isPlaying = !isPlaying
                    player.playWhenReady = isPlaying
                })
            }
    ) {
        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        // Play/Pause icon in center
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "PlayPause",
            tint = Color.White,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.Center)
        )

        // SeekBar (optional, simplified)
        val currentPosition by rememberUpdatedState(player.currentPosition)
        val duration by rememberUpdatedState(player.duration)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = currentPosition.coerceAtMost(duration).toFloat() / duration.coerceAtLeast(1).toFloat(),
                onValueChange = { newValue ->
                    val newPosition = (duration * newValue).toLong()
                    player.seekTo(newPosition)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(0.85f), // 🔍 Shrinks the slider overall
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray
                ),
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = remember { MutableInteractionSource() },
                        modifier = Modifier.size(10.dp) //  Smaller thumb
                    )
                }
            )
        }

    }
}



