package com.example.a122mm.screen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Subtitles
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.a122mm.helper.CustomSlider
import com.example.a122mm.helper.formatTime
import com.example.a122mm.helper.getCFlareUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainPlayerScreen(
    videoUrl: String,
    subtitleUrl: String?,
    progress: Int,
    tTitle: String,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val externalSubUrl = subtitleUrl?.trim().orEmpty()
    val hasExternalSrt = externalSubUrl.isNotEmpty()

    DisposableEffect(Unit) {
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

    // TrackSelector: external SRT -> do NOT auto-pick embedded; otherwise allow embedded
    val trackSelector = remember(hasExternalSrt) {
        DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters().apply {
                    if (hasExternalSrt) {
                        // 1) External SRT present → don't auto-pick any embedded text
                        setSelectUndeterminedTextLanguage(false)
                        setPreferredTextLanguage(null)
                        setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true) // disable text until we force-select ours
                    } else {
                        // 2) No external → allow Exo to pick embedded (default/forced/undetermined)
                        setSelectUndeterminedTextLanguage(true)
                        setPreferredTextLanguage(null)
                        setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    }
                }
            )
        }
    }

    val renderersFactory = remember {
        DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
    }

    val audioAttrs = remember {
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    }

    Log.d("VideoUrl", "vidURL: '$videoUrl'")

    // Build player + MediaItem (attach external SRT with stable ID)
    val exoPlayer = remember {
        ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .build().apply {
                setAudioAttributes(audioAttrs, /* handleAudioFocus = */ true)
                volume = 1.0f

                val startMs = (progress.coerceAtLeast(0)).toLong() * 1_000L - 5_000L

                val mediaItemBuilder = MediaItem.Builder()
                    .setUri(Uri.parse(getCFlareUrl(videoUrl)))

                if (hasExternalSrt) {
                    val subCfg = MediaItem.SubtitleConfiguration.Builder(
                        Uri.parse(getCFlareUrl(externalSubUrl))
                    )
                        .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                        .setLanguage("en")        // optional
                        .setId("ext_srt")         // stable ID to target later
                        .setLabel("External SRT") // helpful label
                        .setSelectionFlags(0)     // don't rely on DEFAULT flag
                        .build()
                    mediaItemBuilder.setSubtitleConfigurations(listOf(subCfg))
                }

                val mediaItem = mediaItemBuilder.build()
                setMediaItem(mediaItem, startMs)

                // If we have external SRT, keep text disabled BEFORE prepare() so embedded can’t win
                if (hasExternalSrt) {
                    trackSelectionParameters = trackSelectionParameters
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                        .build()
                }

                prepare()
                playWhenReady = true
            }
    }

    LaunchedEffect(exoPlayer.audioSessionId) {
        // place to (re)create FX if needed
    }

    RememberAndAttachAudioEffects(exoPlayer)

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

    // Subtitle selection logic: 1) external SRT; 2) else best embedded; 3) else none
    exoPlayer.addListener(object : Player.Listener {
        override fun onTracksChanged(tracks: Tracks) {
            val textGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }

            if (hasExternalSrt) {
                // Find our attached external SRT by ID/label + MIME
                for ((gIdx, g) in textGroups.withIndex()) {
                    for (i in 0 until g.length) {
                        if (!g.isTrackSupported(i)) continue
                        val f = g.getTrackFormat(i)
                        val isSrt = f.sampleMimeType == MimeTypes.APPLICATION_SUBRIP
                        val isOurExt = (f.id == "ext_srt") || (f.label == "External SRT")
                        if (isSrt && isOurExt) {
                            val override = TrackSelectionOverride(g.mediaTrackGroup, listOf(i))
                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                .buildUpon()
                                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false) // re-enable text
                                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                .addOverride(override) // force our external
                                .build()
                            return
                        }
                    }
                }
                // If not visible yet, do nothing; callback will fire again
                return
            }

            // No external SRT → try to select embedded (Default > Forced > Any). If none, leave off.
            if (textGroups.isEmpty()) return

            data class Candidate(val g: Int, val t: Int, val score: Int)

            val candidates = buildList {
                textGroups.forEachIndexed { gIdx, g ->
                    for (i in 0 until g.length) {
                        if (!g.isTrackSupported(i)) continue
                        val f = g.getTrackFormat(i)
                        val isText = f.sampleMimeType?.startsWith("text/") == true ||
                                f.sampleMimeType == MimeTypes.TEXT_VTT ||
                                f.sampleMimeType == MimeTypes.APPLICATION_SUBRIP ||
                                f.sampleMimeType == MimeTypes.APPLICATION_MP4VTT ||
                                f.sampleMimeType == MimeTypes.APPLICATION_TTML
                        if (!isText) continue

                        val sel = f.selectionFlags
                        val score = when {
                            (sel and C.SELECTION_FLAG_DEFAULT) != 0 -> 3
                            (sel and C.SELECTION_FLAG_FORCED)  != 0 -> 2
                            else -> 1
                        }
                        add(Candidate(gIdx, i, score))
                    }
                }
            }

            val best = candidates.maxByOrNull { it.score } ?: return
            val group = textGroups[best.g]
            val override = TrackSelectionOverride(group.mediaTrackGroup, listOf(best.t))
            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .addOverride(override)
                .build()
        }
    })

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

    // Tap-to-toggle + auto-hide after 4s
    val isControlsVisible = remember { mutableStateOf(false) }
    LaunchedEffect(isControlsVisible.value) {
        if (isControlsVisible.value) {
            delay(4000)
            isControlsVisible.value = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                isControlsVisible.value = !isControlsVisible.value
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
                                android.graphics.Color.WHITE,
                                android.graphics.Color.TRANSPARENT,
                                android.graphics.Color.TRANSPARENT,
                                CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,
                                android.graphics.Color.BLACK,
                                null
                            )
                        )
                        setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24f)
                        setBottomPaddingFraction(0.05f)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                color = Color.White,
                strokeWidth = 4.dp
            )
        }

        // Title
        AnimatedVisibility(
            visible = isControlsVisible.value,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -100 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -100 })
        ) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    text = tTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .shadow(
                            elevation = 4.dp,
                            spotColor = Color.Black,
                            ambientColor = Color.Black
                        )
                )
            }
        }

        // Back
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

        // Center controls
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
                    // Replay
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

                    // Play/Pause
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
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(1.2f)
                        )
                    }

                    Spacer(Modifier.width(96.dp))

                    // Forward
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

        val configuration = LocalConfiguration.current
        val isTablet = configuration.smallestScreenWidthDp >= 600
        val bottomLift = if (isTablet) 54.dp else 12.dp

        // Bottom: slider + timer (+ buttons under seekbar) — always at screen bottom
        AnimatedVisibility(
            visible = isControlsVisible.value,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 100 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 100 })
        ) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.navigationBars)
                        .padding(start = 64.dp, end = 64.dp, bottom = bottomLift),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: [ seekbar (weight=1) ] [ 12.dp ] [ remaining time ]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
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

                        val totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                        val position = currentPosition.value
                        val remaining = (totalDuration - position).coerceAtLeast(0L)

                        Text(
                            text = formatTime(remaining),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    val menuIconSz = if (isTablet) 32.dp else 20.dp
                    val menuTextSz = if (isTablet) 18.sp else 14.sp
                    val spacerHeight = if (isTablet) 24.dp else 8.dp
                    Spacer(Modifier.height(spacerHeight))

                    // Row 2: Buttons — constrained to same left width as seekbar
                    val hasEpisodes = remember(videoUrl) { videoUrl.contains("Channel-") }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasEpisodes) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.clickable { /* TODO: Episodes */ },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.PlaylistPlay, contentDescription = "Episodes",
                                        tint = Color.White, modifier = Modifier.size(menuIconSz))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Episodes", color = Color.White, fontSize = menuTextSz)
                                }

                                Row(
                                    modifier = Modifier.clickable { /* TODO: Subtitles */ },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Subtitles, contentDescription = "Subtitles",
                                        tint = Color.White, modifier = Modifier.size(menuIconSz))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Subtitles", color = Color.White, fontSize = menuTextSz)
                                }

                                Row(
                                    modifier = Modifier.clickable { /* TODO: Next Episode */ },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.SkipNext, contentDescription = "Next Episode",
                                        tint = Color.White, modifier = Modifier.size(menuIconSz))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Next Episode", color = Color.White, fontSize = menuTextSz)
                                }
                            }
                        } else {
                            // Only Subtitles → align to RIGHT end of seekbar
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(end = 32.dp), // tweak if needed to flush to seekbar end
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.clickable { /* TODO: Subtitles */ },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Subtitles, contentDescription = "Subtitles",
                                        tint = Color.White, modifier = Modifier.size(menuIconSz))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Subtitles", color = Color.White, fontSize = menuTextSz)
                                }
                            }
                        }

                        // Right area mirrors Row 1’s trailing space:
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = formatTime(
                                exoPlayer.duration.coerceAtLeast(0L) - currentPosition.value
                            ),
                            color = Color.Transparent,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        BackHandler { navController.popBackStack() }
    }
}

@Composable
fun RememberAndAttachAudioEffects(exoPlayer: ExoPlayer) {
    var enhancer: LoudnessEnhancer? by remember { mutableStateOf(null) }
    var eq: Equalizer? by remember { mutableStateOf(null) }
    var dyn: DynamicsProcessing? by remember { mutableStateOf(null) }

    LaunchedEffect(exoPlayer.audioSessionId) {
        try { enhancer?.release() } catch (_: Throwable) {}
        try { eq?.release() } catch (_: Throwable) {}
        try { dyn?.release() } catch (_: Throwable) {}

        val session = exoPlayer.audioSessionId
        if (session == C.AUDIO_SESSION_ID_UNSET) return@LaunchedEffect

        try {
            enhancer = LoudnessEnhancer(session).apply {
                setTargetGain(1200)
                enabled = true
            }
        } catch (_: Throwable) {}

        try {
            eq = Equalizer(0, session).apply {
                enabled = true
                val bands = numberOfBands.toInt()
                val lower = bandLevelRange[0]
                val upper = bandLevelRange[1]
                for (b in 0 until bands) {
                    val centerHz = getCenterFreq(b.toShort()) / 1000
                    val boostMb = when (centerHz) {
                        in 800..1500 -> (upper * 0.35f).toInt()
                        in 2500..4500 -> (upper * 0.25f).toInt()
                        else -> 0
                    }
                    if (boostMb != 0) setBandLevel(b.toShort(), (lower + boostMb).toShort())
                }
            }
        } catch (_: Throwable) {}

        if (Build.VERSION.SDK_INT >= 28) {
            try {
                val cfg = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_TIME_RESOLUTION,
                    2, false, 0, true, 1, false, 0, true
                ).build()

                dyn = DynamicsProcessing(0, session, cfg).apply {
                    for (ch in 0 until 2) {
                        val channel = getChannelByChannelIndex(ch)
                        val mbc = channel.mbc
                        mbc.isEnabled = true
                        val band = mbc.getBand(0)
                        band.attackTime = 8f
                        band.releaseTime = 80f
                        band.ratio = 4.5f
                        band.threshold = -20f
                        band.kneeWidth = 3f
                        band.postGain = 9f
                        mbc.setBand(0, band)
                        channel.mbc = mbc
                        setChannelTo(ch, channel)
                    }
                    val limiter = DynamicsProcessing.Limiter(
                        true, true, 0, 1f, 50f, 12f, -1.5f, 0f
                    )
                    setLimiterByChannelIndex(0, limiter)
                    setLimiterByChannelIndex(1, limiter)
                    enabled = true
                }
            } catch (_: Throwable) { /* ignore */ }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { enhancer?.release() } catch (_: Throwable) {}
            try { eq?.release() } catch (_: Throwable) {}
            try { dyn?.release() } catch (_: Throwable) {}
        }
    }
}
