package com.example.a122mm.screen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.a122mm.helper.getDriveUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    subtitleUrl: String?,
    tTitle: String,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
//        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (context as Activity).window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.BLACK
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
    }

    Log.d("VideoUrl", "Google: '${videoUrl}'")
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(getDriveUrl(videoUrl)))
            volume = 1.0f
            subtitleUrl?.let {
                val subtitleUri = Uri.parse(getDriveUrl(it))
                val subtitle = MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                    .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                    .setLanguage("en")
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()
                mediaItemBuilder.setSubtitleConfigurations(listOf(subtitle))
            }
            setMediaItem(mediaItemBuilder.build())
            prepare()
            playWhenReady = true
        }
    }

    val currentPosition = remember { mutableStateOf(0L) }
    val duration = remember { mutableStateOf(1L) }
    val isSeeking = remember { mutableStateOf(false) }
    val seekPosition = remember { mutableStateOf(0f) }

    LaunchedEffect(exoPlayer) {
        while (duration.value <= 1L) {
            duration.value = exoPlayer.duration.takeIf { it > 0 } ?: 1L
            delay(100)
        }
        while (true) {
            if (!isSeeking.value) {
                currentPosition.value = exoPlayer.currentPosition
                seekPosition.value =
                    currentPosition.value.toFloat() / duration.value.toFloat()
            }
            delay(200)
        }
    }

    val isLoading = remember { mutableStateOf(true) }
    val isPlayingState = remember { mutableStateOf(exoPlayer.isPlaying) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> isLoading.value = true
                    Player.STATE_READY -> isLoading.value = false
                    Player.STATE_ENDED -> navController.popBackStack()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPlayingState.value = isPlaying
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    val isControlsVisible = remember { mutableStateOf(false) }
    val interactionTrigger = remember { mutableStateOf(0) }
    val hasUserInteracted = remember { mutableStateOf(false) }

// Only show/hide controls *after* first tap
    if (hasUserInteracted.value) {
        LaunchedEffect(interactionTrigger.value) {
            isControlsVisible.value = true
            delay(4000)
            isControlsVisible.value = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,             // 👈 No ripple
                interactionSource = remember { MutableInteractionSource() } // 👈 No pressed state
            ) {
                if (!hasUserInteracted.value) {
                    hasUserInteracted.value = true
                }
                interactionTrigger.value++
            }

    ) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    setShutterBackgroundColor(android.graphics.Color.BLACK)

                    subtitleView?.apply {
                        setStyle(
                            CaptionStyleCompat(
                                android.graphics.Color.WHITE,                // textColor
                                android.graphics.Color.TRANSPARENT,          // backgroundColor
                                android.graphics.Color.TRANSPARENT,          // windowColor
                                CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,    // edgeType
                                android.graphics.Color.BLACK,                // edgeColor
                                null                                          // typeface
                            )
                        )
                        setPadding(0, 0, 0, 120) // ✅ move subtitle higher from bottom
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )


        // Loading spinner
        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                color = Color.White,
                strokeWidth = 4.dp
            )
        }

        // 🔼 Title
        AnimatedVisibility(
            visible = isControlsVisible.value,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -100 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -100 })
        ) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    text = tTitle,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                )
            }
        }

        // 🔼 Back button
        AnimatedVisibility(
            visible = isControlsVisible.value,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -100 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -100 })
        ) {
            Box(Modifier.fillMaxSize()) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        val coroutineScope = rememberCoroutineScope()

        var isPlayPressed by remember { mutableStateOf(false) }
        val playScale by animateFloatAsState(
            targetValue = if (isPlayPressed) 1.2f else 1f,
            label = "playScale"
        )

        var isReplayPressed by remember { mutableStateOf(false) }
        val replayScale by animateFloatAsState(
            targetValue = if (isReplayPressed) 1.2f else 1f,
            label = "replayScale"
        )

        var isForwardPressed by remember { mutableStateOf(false) }
        val forwardScale by animateFloatAsState(
            targetValue = if (isForwardPressed) 1.2f else 1f,
            label = "forwardScale"
        )
        // ⏯️ Center controls
        AnimatedVisibility(
            visible = isControlsVisible.value,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // ⏪ Replay Button
                    IconButton(
                        onClick = {
                            isReplayPressed = true
                            exoPlayer.seekBack()
                            coroutineScope.launch {
                                delay(100)
                                isReplayPressed = false
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .scale(replayScale)
                    ) {
                        Icon(
                            Icons.Default.Replay10,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.width(96.dp))

                    // ▶️ / ⏸ Play/Pause Button
                    IconButton(
                        onClick = {
                            isPlayPressed = true
                            val newState = !exoPlayer.isPlaying
                            exoPlayer.playWhenReady = newState
                            isPlayingState.value = newState
                            coroutineScope.launch {
                                delay(100)
                                isPlayPressed = false
                            }
                        },
                        modifier = Modifier
                            .size(96.dp)
                            .scale(playScale)
                            .padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingState.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                                .scale(1.2f)
                        )
                    }

                    Spacer(Modifier.width(96.dp))

                    // ⏩ Forward Button
                    IconButton(
                        onClick = {
                            isForwardPressed = true
                            exoPlayer.seekForward()
                            coroutineScope.launch {
                                delay(100)
                                isForwardPressed = false
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .scale(forwardScale)
                    ) {
                        Icon(
                            Icons.Default.Forward10,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }


        // 🔽 Bottom: slider + timer
        AnimatedVisibility(
            visible = isControlsVisible.value,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 100 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 100 })
        ) {
            Box(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 64.dp, end = 64.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomSlider(
                        progress = seekPosition.value,
                        onSeekChanged = {
                            isSeeking.value = true
                            seekPosition.value = it
                        },
                        onSeekFinished = {
                            val newPos = (seekPosition.value * duration.value).toLong()
                            exoPlayer.seekTo(newPos)
                            currentPosition.value = newPos
                            isSeeking.value = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formatTime(currentPosition.value),
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }

        BackHandler { navController.popBackStack() }
    }


}
//
@Composable
fun CustomSlider(
    progress: Float,
    onSeekChanged: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = progress.coerceIn(0f, 1f),
        onValueChange = onSeekChanged,
        onValueChangeFinished = onSeekFinished,
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp), // preserve round shape
        colors = SliderDefaults.colors(
            thumbColor = Color.Red,
            activeTrackColor = Color.Red,
            inactiveTrackColor = Color.Gray
        )
    )
}


fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
