package com.example.a122mm.screen

import com.example.a122mm.helper.CustomSlider
import com.example.a122mm.helper.formatTime
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.example.a122mm.helper.getDriveUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.a122mm.helper.getCFlareUrl

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

    val trackSelector = remember(hasExternalSrt) {
        DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters().apply {
                    // your audio prefs can stay here (E-AC3/AC3/AAC) if you had them
                    if (hasExternalSrt) {
                        // We will attach our own SRT, do NOT auto-pick embedded/forced
                        setSelectUndeterminedTextLanguage(false)
                        setPreferredTextLanguage(null)
                    } else {
                        // No external → allow embedded/forced selection
                        setSelectUndeterminedTextLanguage(true)
                        setPreferredTextLanguage("en") // or null if you don’t want a lang bias
                    }
                }
            )
        }
    }

    val renderersFactory = remember {
        DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true) // critical: use SW decode when HW missing
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
    }

    val audioAttrs = remember {
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    }

    Log.d("VideoUrl", "vidURL: '${videoUrl}'")
    // ---- UPDATED: build Exo with the renderersFactory + trackSelector ----
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
                        .setLanguage("en")
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT) // prefer THIS subtitle
                        .build()
                    mediaItemBuilder.setSubtitleConfigurations(listOf(subCfg))
                }

                val mediaItem = mediaItemBuilder.build()
                setMediaItem(mediaItem, startMs)
                prepare()
                playWhenReady = true

            }
    }

    LaunchedEffect(exoPlayer.audioSessionId) {
        // release old effects if any, then
        // create LoudnessEnhancer and DynamicsProcessing with exoPlayer.audioSessionId
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

    // (optional but handy) show a warning if no supported audio tracks
    exoPlayer.addListener(object : Player.Listener {
        override fun onTracksChanged(tracks: Tracks) {
            val hasSupportedAudio = tracks.groups.any { g ->
                g.type == C.TRACK_TYPE_AUDIO &&
                        (0 until g.length).any { g.isTrackSupported(it) }
            }
            if (!hasSupportedAudio) {
                Log.w("AUDIO", "No supported audio tracks (likely DD+ only, no decoder).")
            }

            if (hasExternalSrt) {
                // Force-pick the external SRT (application/x-subrip) and ignore embedded ones
                val textGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
                for (g in textGroups) {
                    for (i in 0 until g.length) {
                        val f = g.getTrackFormat(i)
                        if (f.sampleMimeType == MimeTypes.APPLICATION_SUBRIP) {
                            val override = TrackSelectionOverride(g.mediaTrackGroup, listOf(i))
                            val params = exoPlayer.trackSelectionParameters
                                .buildUpon()
                                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                .addOverride(override)
                                .build()
                            exoPlayer.trackSelectionParameters = params
                            return
                        }
                    }
                }
            }
        }

    }
    )

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

// Auto-hide after 4s of inactivity
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
                indication = null,             // 👈 No ripple
                interactionSource = remember { MutableInteractionSource() } // 👈 No pressed state
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
                                android.graphics.Color.WHITE,                // textColor
                                android.graphics.Color.TRANSPARENT,          // backgroundColor
                                android.graphics.Color.TRANSPARENT,          // windowColor
                                CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,    // edgeType
                                android.graphics.Color.BLACK,                // edgeColor
                                null                                          // typeface
                            )
                        )
                        setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24f)
                        setBottomPaddingFraction(0.05f)
//                        setPadding(0, 0, 0, 5) // ✅ move subtitle higher from bottom
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

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isTablet =  configuration.smallestScreenWidthDp >= 600
        val bottomLift = if (isTablet) 54.dp else 12.dp   // raise controls higher on tablet

        // 🔽 Bottom: slider + timer (+ buttons under seekbar) — ALWAYS at screen bottom
        AnimatedVisibility(
            visible = isControlsVisible.value,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 100 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 100 })
        ) {
            Box(Modifier.fillMaxSize()) {

                // We pin the whole cluster to the screen bottom and respect nav-bar insets
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.navigationBars) // ⬅️ avoid floating above bottom on tablets
                        .padding(start = 64.dp, end = 64.dp, bottom = bottomLift), // small visual gap
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: [ seekbar (weight=1) ]  [ 12.dp ]  [ remaining time ]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Left: seekbar (defines the width we’ll align buttons to)
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

                        // Right: remaining time
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

                    // Row 2: Buttons — constrained to the SAME left width as the seekbar
                    val hasEpisodes = remember(videoUrl) { videoUrl.contains("Channel-") }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left area: same width as the seekbar (weight=1)
                        if (hasEpisodes) {
                            // 3 buttons spaced evenly across seekbar width
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Episodes
                                Row(
                                    modifier = Modifier.clickable { /* TODO: open episodes */ },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlaylistPlay,
                                        contentDescription = "Episodes",
                                        tint = Color.White,
                                        modifier = Modifier.size(menuIconSz)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Episodes", color = Color.White, fontSize = menuTextSz)
                                }

                                // Caption
                                Row(
                                    modifier = Modifier.clickable { /* TODO: captions */ },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Subtitles,
                                        contentDescription = "Subtitles",
                                        tint = Color.White,
                                        modifier = Modifier.size(menuIconSz)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Subtitles", color = Color.White, fontSize = menuTextSz)
                                }

                                // Next Episode
                                Row(
                                    modifier = Modifier.clickable { /* TODO: next episode */ },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipNext,
                                        contentDescription = "Next Episode",
                                        tint = Color.White,
                                        modifier = Modifier.size(menuIconSz)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Next Episode", color = Color.White, fontSize = menuTextSz)
                                }
                            }
                        } else {
                            // Only Caption → align to the RIGHT end of the seekbar
                            Row(
                                modifier = Modifier
                                    .weight(1f)                 // match seekbar width
                                    .fillMaxWidth()
                                    .padding(end = 32.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.clickable { /* TODO: captions */ },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Subtitles,
                                        contentDescription = "Subtitles",
                                        tint = Color.White,
                                        modifier = Modifier.size(menuIconSz)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Subtitles", color = Color.White, fontSize = menuTextSz)
                                }
                            }
                        }

                        // Right area mirrors Row 1’s trailing space:
                        Spacer(modifier = Modifier.width(12.dp))
                        // Invisible timer to reserve identical width as Row 1's time text
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

    // Recreate effects whenever the audio session changes
    LaunchedEffect(exoPlayer.audioSessionId) {
        // Cleanup old
        try { enhancer?.release() } catch (_: Throwable) {}
        try { eq?.release() } catch (_: Throwable) {}
        try { dyn?.release() } catch (_: Throwable) {}

        val session = exoPlayer.audioSessionId
        if (session == C.AUDIO_SESSION_ID_UNSET) return@LaunchedEffect

        // 1) Loudness boost (+6–10 dB) — helps perceived volume across the board
        try {
            enhancer = LoudnessEnhancer(session).apply {
                setTargetGain(1200) // 800 mB ≈ +8 dB (tune 600..1200)
                enabled = true
            }
        } catch (_: Throwable) {}

        // 2) (Optional) small mid lift for dialog (defensive; may be no-op on some devices)
        try {
            eq = Equalizer(0, session).apply {
                enabled = true
                val bands = numberOfBands.toInt()
                val lower = bandLevelRange[0] // mB
                val upper = bandLevelRange[1] // mB
                for (b in 0 until bands) {
                    val centerHz = getCenterFreq(b.toShort()) / 1000 // Hz
                    val boostMb = when (centerHz) {
                        in 800..1500 -> (upper * 0.35f).toInt()  // ~+3.5 dB around 1 kHz
                        in 2500..4500 -> (upper * 0.25f).toInt() // ~+2.5 dB around 3 kHz
                        else -> 0
                    }
                    if (boostMb != 0) setBandLevel(b.toShort(), (lower + boostMb).toShort())
                }
            }
        } catch (_: Throwable) {}

        // 3) API 28+: gentle single-band compressor using DynamicsProcessing (true DRC)
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                // Stereo, no pre/post EQ, 1 MBC band, limiter ON
                val cfg = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_TIME_RESOLUTION, // or _FREQUENCY_RESOLUTION
                    /* channelCount    */ 2,
                    /* preEqInUse      */ false, /* preEqBands  */ 0,
                    /* mbcInUse        */ true,  /* mbcBands    */ 1,
                    /* postEqInUse     */ false, /* postEqBands */ 0,
                    /* limiterInUse    */ true
                ).build()

                dyn = DynamicsProcessing(/*priority*/0, /*audioSession*/session, cfg).apply {
                    // ----- Single-band compressor settings on both channels -----
                    for (ch in 0 until 2) {
                        val channel = getChannelByChannelIndex(ch)
                        val mbc = channel.mbc
                        mbc.isEnabled = true

                        val band = mbc.getBand(0) // band 0 (only band)
                        band.attackTime = 8f        // a touch slower, smoother
                        band.releaseTime = 80f      // relax a bit
                        band.ratio = 4.5f           // gentler compression
                        band.threshold = -20f       // not hitting *all* the time
                        band.kneeWidth = 3f
                        band.postGain = 9f    // << makeup gain in dB (use postGain, not gain)
                        // (optional) band.preGain = 0f

                        mbc.setBand(0, band)
                        channel.mbc = mbc
                        setChannelTo(ch, channel)
                    }

                    // ----- Limiter per channel -----
                    val limiter = DynamicsProcessing.Limiter(
                        /* enabled     */ true,
                        /* linkGroup   */ true,
                        /* linkGroupId */ 0,     // <-- required Int (use 0 if you don't group multiple FX)
                        /* attackMs    */ 1f,
                        /* releaseMs   */ 50f,
                        /* ratio       */ 12f,
                        /* threshold   */ -1.5f,
                        /* postGain    */ 0f
                    )
                    setLimiterByChannelIndex(0, limiter)
                    setLimiterByChannelIndex(1, limiter)

                    enabled = true
                }
            } catch (_: Throwable) {
                // Some OEMs may not fully support this; fail gracefully
            }
        }

    }

    // Cleanup when Composable leaves
    DisposableEffect(Unit) {
        onDispose {
            try { enhancer?.release() } catch (_: Throwable) {}
            try { eq?.release() } catch (_: Throwable) {}
            try { dyn?.release() } catch (_: Throwable) {}
        }
    }
}
