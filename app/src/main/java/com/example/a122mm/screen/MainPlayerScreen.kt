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
import androidx.compose.runtime.State
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.a122mm.components.ApiServiceRecent
import com.example.a122mm.components.MovieDetail
import com.example.a122mm.components.RecentWatchResponse
import com.example.a122mm.dataclass.ApiClient
import com.example.a122mm.dataclass.NetworkModule
import com.example.a122mm.helper.CustomSlider
import com.example.a122mm.helper.fixEncoding
import com.example.a122mm.helper.encodeUrlSegments
import com.example.a122mm.helper.encodeUrlSegmentsStrict
import com.example.a122mm.helper.formatTime
import com.example.a122mm.helper.getCFlareUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query

data class VideoDetailsResponse(
    val gId: String,
    val mId: String,
    val mTitle: String,
    val cFlareVid: String,
    val cFlareSrt: String,
    val cProgress: Int,
    val gDriveVid: String,
    val gDriveSrt: String,
    val sId: Int,
    val tvOrder: Int,
    val nextTvId: String,
    val crTime: Int
)


interface ApiVideoDetails {
    @GET("getvideodetails")
    suspend fun getVideoDetails(
        @Query("code") code: String
    ): VideoDetailsResponse
}

// ----- ViewModel: wiring & API calls -----
class RecentWatchViewModel : ViewModel() {
    private val apiService = ApiClient.create(ApiVideoDetails::class.java)

    private val _item = mutableStateOf<VideoDetailsResponse?>(null)
    val item: State<VideoDetailsResponse?> = _item

    fun fetchItemByCode(code: String) {
        viewModelScope.launch {
            try {
                _item.value = apiService.getVideoDetails(code)
            } catch (e: Exception) {
                e.printStackTrace()
                _item.value = null
            }
        }
    }
}

@Composable
fun MainPlayerScreen(
    videoCode: String,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // ──────────────────────────
    // Fetch video details by code
    // ──────────────────────────
    var currentCode by remember { mutableStateOf(videoCode) }
    var vData by remember { mutableStateOf<VideoDetailsResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val api: ApiVideoDetails = ApiClient.create(ApiVideoDetails::class.java)

    LaunchedEffect(currentCode) {
        try {
            vData = api.getVideoDetails(currentCode)
            error = null
        } catch (e: Exception) {
            error = e.message
            vData = null
        }
    }

    // Orientation & system UI (unchanged)
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
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    // Loading / error states (simple)
    if (error != null) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Error: $error", color = Color.Red)
        }
        return
    }
    if (vData == null) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
        }
        return
    }

    // Map API → local vars (replaces previous parameters)
    val videoUrl = vData!!.cFlareVid //.fixEncoding()
    val subtitleUrl = vData!!.cFlareSrt //.fixEncoding()
    val progress = vData!!.cProgress
    val tTitle = vData!!.mTitle.fixEncoding()
    val nextId = vData!!.nextTvId



    val externalSubUrl = subtitleUrl.trim()
    val hasExternalSrt = externalSubUrl.isNotEmpty()

    // TrackSelector respects your external SRT rule
    val trackSelector = remember(hasExternalSrt) {
        DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters().apply {
                    if (hasExternalSrt) {
                        setSelectUndeterminedTextLanguage(false)
                        setPreferredTextLanguage(null)
                        setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    } else {
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

    // Build ExoPlayer AFTER data is ready; re-build if URL/progress or SRT presence changes
    val exoPlayer = remember(videoUrl, progress, hasExternalSrt) {
        ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .build().apply {
                setAudioAttributes(audioAttrs, true)
                volume = 1.0f

                val startMs = (progress.coerceAtLeast(0)).toLong() * 1_000L - 5_000L

                val resolvedVideoUrl = encodeUrlSegmentsStrict(getCFlareUrl(videoUrl))
                Log.d("MainPlayer", "Video URL -> $resolvedVideoUrl  (raw: $videoUrl)")

                val mediaItemBuilder = MediaItem.Builder()
                    .setUri(Uri.parse(getCFlareUrl(videoUrl)))

                if (hasExternalSrt) {
                    val resolvedSubUrl = encodeUrlSegmentsStrict(getCFlareUrl(externalSubUrl))
                    Log.d("MainSubs", "Subs URL -> $resolvedSubUrl  (raw: $externalSubUrl)")

                    val subCfg = MediaItem.SubtitleConfiguration.Builder(
                        Uri.parse(resolvedSubUrl)
                    )
                        .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                        .setLanguage("en")
                        .setId("ext_srt")
                        .setLabel("External SRT")
                        .setSelectionFlags(0)
                        .build()
                    mediaItemBuilder.setSubtitleConfigurations(listOf(subCfg))
                }

                val mediaItem = mediaItemBuilder.build()
                setMediaItem(mediaItem, startMs)

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

    LaunchedEffect(exoPlayer.audioSessionId) { /* hook for audio FX if needed */ }
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
                seekPosition.value = currentPosition.value.toFloat() / duration.value.toFloat()
            }
            delay(200)
        }
    }

    // Keep your subtitle selection listener logic
    exoPlayer.addListener(object : Player.Listener {
        override fun onTracksChanged(tracks: Tracks) {
            val textGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }

            if (hasExternalSrt) {
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
                                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                .addOverride(override)
                                .build()
                            return
                        }
                    }
                }
                return
            }

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

    // ──────────────────────────
    // UI (kept intact; just binds to variables from vData)
    // ──────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { isControlsVisible.value = !isControlsVisible.value }
    ) {
        // Player surface
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
                modifier = Modifier.size(48.dp).align(Alignment.Center),
                color = Color.White,
                strokeWidth = 4.dp
            )
        }

        // Title (now from API)
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
                        .shadow(4.dp, spotColor = Color.Black, ambientColor = Color.Black)
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
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                        tint = Color.White, modifier = Modifier.size(64.dp))
                }
            }
        }

        val coroutineScope = rememberCoroutineScope()
        var isPlayPressed by remember { mutableStateOf(false) }
        val playScale by animateFloatAsState(if (isPlayPressed) 1.2f else 1f, label = "playScale")
        var isReplayPressed by remember { mutableStateOf(false) }
        val replayScale by animateFloatAsState(if (isReplayPressed) 1.2f else 1f, label = "replayScale")
        var isForwardPressed by remember { mutableStateOf(false) }
        val forwardScale by animateFloatAsState(if (isForwardPressed) 1.2f else 1f, label = "forwardScale")

        // Center controls
        AnimatedVisibility(visible = isControlsVisible.value, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
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
                            coroutineScope.launch { delay(100); isReplayPressed = false }
                        },
                        modifier = Modifier.size(64.dp).scale(replayScale)
                    ) {
                        Icon(Icons.Default.Replay10, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize())
                    }

                    Spacer(Modifier.width(96.dp))

                    // Play/Pause
                    IconButton(
                        onClick = {
                            isPlayPressed = true
                            val newState = !exoPlayer.isPlaying
                            exoPlayer.playWhenReady = newState
                            isPlayingState.value = newState
                            coroutineScope.launch { delay(100); isPlayPressed = false }
                        },
                        modifier = Modifier.size(96.dp).scale(playScale).padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingState.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize().scale(1.2f)
                        )
                    }

                    Spacer(Modifier.width(96.dp))

                    // Forward
                    IconButton(
                        onClick = {
                            isForwardPressed = true
                            exoPlayer.seekForward()
                            coroutineScope.launch { delay(100); isForwardPressed = false }
                        },
                        modifier = Modifier.size(64.dp).scale(forwardScale)
                    ) {
                        Icon(Icons.Default.Forward10, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }

        val configuration = LocalConfiguration.current
        val isTablet = configuration.smallestScreenWidthDp >= 600
        val bottomLift = if (isTablet) 54.dp else 20.dp

        // Bottom: slider + time + (buttons)
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
                    // Row 1: Seek + remaining
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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
                            modifier = Modifier.weight(1f).height(32.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        val totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                        val position = currentPosition.value
                        val remaining = (totalDuration - position).coerceAtLeast(0L)
                        Text(text = formatTime(remaining), color = Color.White, fontSize = 14.sp)
                    }

                    val menuIconSz = if (isTablet) 32.dp else 24.dp
                    val menuTextSz = if (isTablet) 18.sp else 16.sp
                    val spacerHeight = if (isTablet) 24.dp else 2.dp
                    val ctx = LocalContext.current

                    Spacer(Modifier.height(spacerHeight))

                    // Row 2: Buttons (Episodes/Subtitles/Next) — same logic; detect episodes via cFlareVid
                    val hasEpisodes = remember(videoUrl) { videoUrl.contains("Channel-") }

                    Row(
                        modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasEpisodes) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.clickable { /* TODO: Episodes */ }, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.PlaylistPlay, contentDescription = "Episodes", tint = Color.White, modifier = Modifier.size(menuIconSz))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Episodes", color = Color.White, fontSize = menuTextSz)
                                }
                                Row(
                                    modifier = Modifier
                                                .clickable { /* TODO: Subtitles */ },
                                                verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Subtitles, contentDescription = "Subtitles", tint = Color.White, modifier = Modifier.size(menuIconSz))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Subtitles", color = Color.White, fontSize = menuTextSz)
                                }

                                if (nextId != "") {
                                    Row(
                                        modifier = Modifier.clickable {
                                            val next = vData?.nextTvId.orEmpty()
//                                            if (next.isBlank()) {
//                                                android.widget.Toast.makeText(ctx, "No next episode", android.widget.Toast.LENGTH_SHORT).show()
//                                                return@clickable
//                                            }

                                            // Optional: stop current playback immediately so user sees a quick reload
                                            try { exoPlayer.stop() } catch (_: Throwable) {}

                                            // Show spinner while fetching the next episode
                                            vData = null

                                            // Trigger API: this re-runs LaunchedEffect(currentCode) above
                                            currentCode = next
                                        },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.SkipNext,
                                            contentDescription = "Next Episode",
                                            tint = Color.White,
                                            modifier = Modifier.size(menuIconSz)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Next Episode",
                                            color = Color.White,
                                            fontSize = menuTextSz
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.weight(1f).fillMaxWidth().padding(end = 32.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.clickable { /* TODO: Subtitles */ }, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Subtitles, contentDescription = "Subtitles", tint = Color.White, modifier = Modifier.size(menuIconSz))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Subtitles", color = Color.White, fontSize = menuTextSz)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = formatTime(exoPlayer.duration.coerceAtLeast(0L) - currentPosition.value),
                            color = Color.Transparent, // invisible spacer to mirror Row 1
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
