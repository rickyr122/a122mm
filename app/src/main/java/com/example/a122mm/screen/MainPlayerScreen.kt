package com.example.a122mm.screen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.audiofx.DynamicsProcessing
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.a122mm.dataclass.ApiClient
import com.example.a122mm.helper.CustomSlider
import com.example.a122mm.helper.encodeUrlSegmentsStrict
import com.example.a122mm.helper.fixEncoding
import com.example.a122mm.helper.formatTime
import com.example.a122mm.helper.getCFlareUrl
import com.example.a122mm.utility.formatDurationFromMinutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import kotlin.math.floor
import kotlin.math.roundToInt


data class VideoDetailsResponse(
    val gId: String,
    val gName: String,
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

// --- Episodes API models ---
data class totalSeasonResponse(
    val tId: String,
    val totalSeason: Int
)

data class EpisodeItem(
    val tvId: String,
    val tvTitle: String,
    val tvDuration: Int,
    val tvCvrUrl: String,
    val tvDescription: String,
    val selectedEps: Int
)

interface ApiVideoDetails {
    @GET("getvideodetails")
    suspend fun getVideoDetails(
        @Query("code") code: String
    ): VideoDetailsResponse

    @GET("gettotalseason")
    suspend fun gettotalseason(
        @Query("code") code: String
    ): totalSeasonResponse

    @GET("gettvepisodes")
    suspend fun getEpisodes(
        @Query("code") code: String,
        @Query("season") season: Int
    ): List<EpisodeItem>
}

interface ApiContinueWatching {
    @FormUrlEncoded
    @POST("addcontinuewatching")
    suspend fun upsert(
        @Field("mId") mId: String,
        @Field("cId") cId: String,
        @Field("cProgress") cProgress: Double,
        @Field("cPercent") cPercent: Double,
        @Field("cPosition") cPosition: Double,
        @Field("client_time") clientTime: String,
        @Field("state") state: String = "u"
    ): String

    @FormUrlEncoded
    @POST("addcontinuewatching")
    suspend fun delete(
        @Field("mId") mId: String,
        @Field("cId") cId: String,
        @Field("state") state: String = "d"
    ): String
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

enum class SubtitleOption { OFF, ENGLISH, INDONESIAN }

@Composable
fun MainPlayerScreen(
    videoCode: String,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val bottomLift = if (isTablet) 64.dp else 36.dp

    var showSubtitleMenu by remember { mutableStateOf(false) }
    var pendingSubSelection by remember { mutableStateOf(SubtitleOption.ENGLISH) } // default

    var manualSubtitleChange by remember { mutableStateOf(false) }
    var subtitleAvailable by remember { mutableStateOf(false) }


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
            if (!isTablet) {
                // force back to portrait when leaving on phones
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                // let system handle freely on tablets
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }

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
    val spinnerSz = if (isTablet) 64.dp else 54.dp
    if (vData == null) {
        Box(
            Modifier
                .fillMaxSize()
                .size(spinnerSz)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.Red, strokeWidth = 4.dp)
        }
        return
    }

    // Map API → local vars (replaces previous parameters)
    val videoUrl = vData!!.cFlareVid //.fixEncoding()
    val subtitleUrl = vData!!.cFlareSrt //.fixEncoding()
    val progress = vData!!.cProgress
    val tTitle = vData!!.mTitle.fixEncoding()
    val nextId = vData!!.nextTvId
    val gName = vData!!.gName
    val crStartSec = vData!!.crTime

    val externalSubUrl = subtitleUrl.trim()
    val hasExternalSrt = externalSubUrl.isNotEmpty()

    LaunchedEffect(subtitleUrl) {
        subtitleAvailable = false

        if (!subtitleUrl.isNullOrBlank()) {
            subtitleAvailable = withContext(Dispatchers.IO) {
                try {
                    val url = java.net.URL(getCFlareUrl(subtitleUrl))
                    (url.openConnection() as java.net.HttpURLConnection).run {
                        requestMethod = "HEAD"
                        connectTimeout = 3000
                        readTimeout = 3000
                        val ok = (responseCode == 200)
                        disconnect()
                        ok
                    }
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

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
                volume = 1.3f

                val startMs = (progress.coerceAtLeast(0)).toLong() * 1_000L - 5_000L

                val resolvedVideoUrl = encodeUrlSegmentsStrict(getCFlareUrl(videoUrl))
                Log.d("MainPlayer", "Video URL -> $resolvedVideoUrl  (raw: $videoUrl)")

//                val mediaItemBuilder = MediaItem.Builder()
//                    .setUri(Uri.parse(getCFlareUrl(videoUrl)))

                val mediaItemBuilder = MediaItem.Builder()
                    .setUri(Uri.parse(resolvedVideoUrl))

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

    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                val cause = error.cause
                Log.e("Player", "Playback error", cause)

                // unwrap actual HTTP error if possible
                if (cause is HttpDataSource.InvalidResponseCodeException) {
                    Log.e("Player", "HTTP status: ${cause.responseCode}")
                    Log.e("Player", "URL: ${cause.dataSpec.uri}")
                } else if (cause is HttpDataSource.HttpDataSourceException) {
                    Log.e("Player", "Network or IO error: ${cause.message}")
                } else {
                    Log.e("Player", "Non-network error: ${error.errorCodeName}")
                }
            }
        })
    }


    val scope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(true) }
    val isPlayingState = remember { mutableStateOf(exoPlayer.isPlaying) }

    var showEndButtons by remember { mutableStateOf(false) }
    var nextFill by remember { mutableStateOf(0f) }          // 0f..1f fill from left→right
    var nextAnimRunning by remember { mutableStateOf(false) }

    val currentPosition = remember { mutableStateOf(0L) }
    val duration = remember { mutableStateOf(1L) }
    val isSeeking = remember { mutableStateOf(false) }
    val seekPosition = remember { mutableStateOf(0f) }

    var creditsMode by remember { mutableStateOf(false) } // when true, keep buttons hidden until STATE_ENDED


    // Continue Watching API
    val cwApi = remember { ApiClient.create(ApiContinueWatching::class.java) }

    // Compose helpers to compute params safely
    fun currentClientTime(): String {
        val now = java.time.LocalDateTime.now()
        return now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    // Decide IDs:
    // mId = vData!!.mId  (MOV-... or TVG-...)
    // cId = if episode -> currentCode (TVS-...), else movie -> vData!!.mId
    fun resolveIds(): Pair<String,String> {
        val mid = vData!!.mId
        val cid = if (currentCode.startsWith("TVS", ignoreCase = true)) currentCode else mid
        return mid to cid
    }

    // Calculate progress numbers from player state
    fun calcProgressTriplet(): Triple<Double, Double, Double> {
        val durMs = duration.value.coerceAtLeast(1L).toDouble()
        val posMs = currentPosition.value.coerceAtLeast(0L).toDouble()

        // round down to integer seconds
        val progressSec = floor(posMs / 1000.0)

        val pct = (posMs / durMs).coerceIn(0.0, 1.0)
        return Triple(progressSec, pct, pct)
    }


    // Fire-and-forget "u"
    fun postCWUpdate() {
        val (mid, cid) = resolveIds()
        val (prog, pct, pos) = calcProgressTriplet()
        val ts = currentClientTime()
        scope.launch(Dispatchers.IO) {
            try {
                cwApi.upsert(
                    mId = mid,
                    cId = cid,
                    cProgress = prog,
                    cPercent = pct,
                    cPosition = pos,
                    clientTime = ts,
                    state = "u"
                )
            } catch (e: Exception) {
                Log.e("CW", "upsert failed: ${e.message}")
            }
        }
    }

    // Fire-and-forget "d"
    fun postCWDelete() {
        val (mid, cid) = resolveIds()
        scope.launch(Dispatchers.IO) {
            try {
                cwApi.delete(mId = mid, cId = cid, state = "d")
            } catch (e: Exception) {
                Log.e("CW", "delete failed: ${e.message}")
            }
        }
    }

    fun goToNextEpisode() {
        val next = vData?.nextTvId.orEmpty()
        if (next.isBlank()) return
        try { exoPlayer.stop() } catch (_: Throwable) {}
        vData = null                  // show spinner
        currentCode = next            // triggers LaunchedEffect(currentCode)
    }

    var sentDeleteOnCr by remember { mutableStateOf(false) }

    LaunchedEffect(currentPosition.value, duration.value, nextId, crStartSec, isLoading.value, creditsMode) {
        if (creditsMode) return@LaunchedEffect               // ⬅️ important
        if (duration.value <= 0L || isLoading.value) return@LaunchedEffect

        val remainingMs = (duration.value - currentPosition.value).coerceAtLeast(0L)
        val triggerMs = crStartSec.coerceAtLeast(0) * 1000L

        // ★ CW: delete once when remaining time <= crTime
        if (!sentDeleteOnCr && remainingMs <= triggerMs) {
            sentDeleteOnCr = true
            postCWDelete()
        }

        val shouldShow = nextId.isNotBlank() &&
                remainingMs in 1..triggerMs &&
                exoPlayer.playWhenReady &&
                exoPlayer.playbackState == Player.STATE_READY

        if (shouldShow && !showEndButtons) {
            showEndButtons = true
        } else if (!shouldShow && showEndButtons) {
            showEndButtons = false
            nextAnimRunning = false
            nextFill = 0f
        }
    }

    var sent10s by remember { mutableStateOf(false) }

    LaunchedEffect(currentPosition.value) {
        if (!sent10s && currentPosition.value >= 10_000L) {
            sent10s = true
            // ★ CW
            postCWUpdate()
        }
    }

    LaunchedEffect(showEndButtons, creditsMode) {
        if (!showEndButtons || creditsMode) return@LaunchedEffect
        nextFill = 0f
        nextAnimRunning = true
        val totalMs = 10000L
        val stepMs = 40L
        val steps = (totalMs / stepMs).toInt().coerceAtLeast(1)
        repeat(steps) {
            if (!nextAnimRunning || creditsMode) return@LaunchedEffect
            nextFill = (it + 1) / steps.toFloat()
            delay(stepMs)
        }
        if (nextAnimRunning && !creditsMode) {
            goToNextEpisode()
        }
    }

    // Start visible on first load; then auto-hide behavior kicks in
    val isControlsVisible = remember { mutableStateOf(true) }
    var autoHideEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(showEndButtons) {
        if (showEndButtons) {
            isControlsVisible.value = false
            autoHideEnabled = false
        } else {
            autoHideEnabled = true
        }
    }

    var levelerEnabled by remember { mutableStateOf(true) }   // on/off
    var levelerStrength by remember { mutableStateOf(0.6f) }  // 0f..1f

    LaunchedEffect(exoPlayer.audioSessionId) { /* hook for audio FX if needed */ }
    RememberAndAttachAudioEffects(
        exoPlayer = exoPlayer,
        enabled = true,
        strength01 = 0.6f,
        boostDb = 4f          // try 3–5 dB; 6 dB max
    )

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

    // default to 50% regardless of system
    var brightness by remember { mutableStateOf(0.5f) }

// remember previous brightness so we can restore on dispose
    val prevWinBrightness = remember {
        activity?.window?.attributes?.screenBrightness
            ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }

// apply brightness whenever it changes
    LaunchedEffect(brightness) {
        activity?.window?.let { w ->
            val lp = w.attributes
            lp.screenBrightness = brightness.coerceIn(0.05f, 1f)
            w.attributes = lp
        }
    }

// restore brightness when this screen leaves
    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.let { w ->
                val lp = w.attributes
                lp.screenBrightness = prevWinBrightness
                w.attributes = lp
            }
        }
    }

    // Keep your subtitle selection listener logic
    exoPlayer.addListener(object : Player.Listener {
        override fun onTracksChanged(tracks: Tracks) {

            if (manualSubtitleChange) return  // ✅ skip auto override

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





    var allowControlsWhileLoading by remember { mutableStateOf(false) }
    var suppressSpinner by remember { mutableStateOf(false) }

// Auto-hide only when visible AND auto-hide is enabled.
// This restarts the 4s timer every time controls become visible.
    LaunchedEffect(isControlsVisible.value, autoHideEnabled) {
        if (autoHideEnabled && isControlsVisible.value) {
            delay(4000)
            isControlsVisible.value = false
            allowControlsWhileLoading = false   // reset after hide
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> isLoading.value = true
                    Player.STATE_READY -> {
                        isLoading.value = false
                        // Show controls on first load, then enable auto-hide behavior.
                        if (!showEndButtons) {
                            isControlsVisible.value = true
                            autoHideEnabled = true
                        }
                        suppressSpinner = false
                        allowControlsWhileLoading = false
                    }
                    Player.STATE_ENDED -> {
                        if (vData?.nextTvId.orEmpty().isNotBlank()) {
                            creditsMode = false
                            goToNextEpisode()
                        } else {
                            navController.popBackStack()
                        }
                    }
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

    var showEpisodes by remember { mutableStateOf(false) }

    var epSeasons by remember { mutableStateOf(listOf<String>()) }
    var epList by remember { mutableStateOf(listOf<EpisodeItem>()) }
    var epSelectedSeason by remember { mutableStateOf(vData!!.sId.coerceAtLeast(1)) }
    var epSelectedSeasonIndex by remember { mutableStateOf((vData!!.sId.coerceAtLeast(1)) - 1) }
    var epActiveIndex by remember { mutableStateOf(vData!!.tvOrder.coerceAtLeast(1) - 1) }

    val episodesApi = ApiClient.create(ApiVideoDetails::class.java)
    var epLoading by remember { mutableStateOf(false) }

    // season-related state
    var epTotalSeasons by remember { mutableStateOf<Int?>(null) }   // unknown until API returns
    var seasonPickerOpen by remember { mutableStateOf(false) }       // overlay for season list

    // When overlay opens, force the season to the one currently playing
    LaunchedEffect(showEpisodes) {
        if (showEpisodes) {
            val currentSeason = vData?.sId?.coerceAtLeast(1) ?: 1
            if (epSelectedSeason != currentSeason) {
                epSelectedSeason = currentSeason    // this will trigger your fetch effect
            }
        }
    }

    LaunchedEffect(showEpisodes, epSelectedSeason) {
        if (!showEpisodes) return@LaunchedEffect
        epLoading = true
        try {
            val res = episodesApi.getEpisodes(
                code = vData!!.gId,
                season = epSelectedSeason
            )

            // ✅ trust the API’s selectedEps
            epList = res

            // ✅ compute the active index based on selectedEps first, then fall back to currentCode
            val idxFromApi = res.indexOfFirst { it.selectedEps == 1 }
            val idxFromId  = res.indexOfFirst { it.tvId == currentCode }
            epActiveIndex = when {
                idxFromApi >= 0 -> idxFromApi
                idxFromId  >= 0 -> idxFromId
                else            -> 0
            }
        } catch (e: Exception) {
            epList = emptyList()
        } finally {
            epLoading = false
        }
    }


    LaunchedEffect(showEpisodes) {
        if (!showEpisodes) return@LaunchedEffect
        try {
            // call the endpoint you already declared in ApiVideoDetails
            val r = episodesApi.gettotalseason(code = vData!!.gId)
            epTotalSeasons = r.totalSeason.coerceAtLeast(1)
        } catch (_: Exception) {
            epTotalSeasons = 1 // fallback so UI still works
        }
    }

    // ──────────────────────────
    // UI (kept intact; just binds to variables from vData)
    // ──────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
//            .clickable(
//                enabled = !showEndButtons,
//                indication = null,
//                interactionSource = remember { MutableInteractionSource() }
//            ) {
//                isControlsVisible.value = !isControlsVisible.value
//                if (isLoading.value && isControlsVisible.value) {
//                    allowControlsWhileLoading = true
//                }
//            }
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

        if (isLoading.value && !suppressSpinner) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(spinnerSz)
                    .align(Alignment.Center),
                color = Color.Red,
                strokeWidth = 4.dp
            )
        }

        // A) When HUD is HIDDEN → tap anywhere to SHOW
        if (!isControlsVisible.value && !showEndButtons) {
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(1f) // above PlayerView, below all HUD controls
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            isControlsVisible.value = true
                            autoHideEnabled = true
                            if (isLoading.value) allowControlsWhileLoading = true
                        })
                    }
            )
        }

//        // B) When HUD is VISIBLE → tap empty area to HIDE
//        if (isControlsVisible.value && (!isLoading.value || suppressSpinner || allowControlsWhileLoading)) {
//            Box(
//                Modifier
//                    .fillMaxSize()
//                    .zIndex(1f) // above PlayerView, but BELOW the buttons/back arrow
//                    .pointerInput(showEndButtons) {
//                        detectTapGestures(onTap = {
//                            if (!showEndButtons) {
//                                isControlsVisible.value = false
//                                autoHideEnabled = false
//                            }
//                        }
//                        )
//                    }
//            )
//        }


        // Title (now from API)
        AnimatedVisibility(
            visible = isControlsVisible.value && (!isLoading.value || suppressSpinner || allowControlsWhileLoading),
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

        // Back (always on top of overlay/clickable surface)
        // Replace your AnimatedVisibility for the back button with this:
//        AnimatedVisibility(
//            visible = isControlsVisible.value && (!isLoading.value || suppressSpinner || allowControlsWhileLoading),
//            enter = slideInVertically(initialOffsetY = { -150 }) + fadeIn(),
//            exit = slideOutVertically(targetOffsetY = { -150 }) + fadeOut()
//        ) {
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .padding(start = 12.dp, top = 4.dp)
//                    .size(42.dp)
//                    .pointerInput(Unit) {
//                        awaitEachGesture {
//                            val down = awaitFirstDown()
//                            down.consume()
//                            val up = waitForUpOrCancellation()
//                            if (up != null) {
//                                when {
//                                    showEpisodes -> {
//                                        showEpisodes = false
//                                        autoHideEnabled = true
//                                        exoPlayer.playWhenReady = true
//                                    }
//                                    showSubtitleMenu -> {
//                                        showSubtitleMenu = false
//                                        autoHideEnabled = true
//                                        exoPlayer.playWhenReady = true
//                                    }
//                                    else -> navController.popBackStack()
//                                }
//                            }
//                        }
//                    }
//            ) {
//                Icon(
//                    imageVector = Icons.Default.ArrowBack,
//                    contentDescription = "Back",
//                    tint = Color.White,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(4.dp)
//                )
//            }
//        }




        val coroutineScope = rememberCoroutineScope()
        var isPlayPressed by remember { mutableStateOf(false) }
        val playScale by animateFloatAsState(if (isPlayPressed) 1.2f else 1f, label = "playScale")
        var isReplayPressed by remember { mutableStateOf(false) }
        val replayScale by animateFloatAsState(if (isReplayPressed) 1.2f else 1f, label = "replayScale")
        var isForwardPressed by remember { mutableStateOf(false) }
        val forwardScale by animateFloatAsState(if (isForwardPressed) 1.2f else 1f, label = "forwardScale")

        var backLoading by remember { mutableStateOf(false) }
        val backSize = 48.dp
        val backPadStart = 12.dp
        val backPadTop = 4.dp

        val density = LocalDensity.current
        val backRightPx  = with(density) { (backPadStart + backSize).toPx() }
        val backBottomPx = with(density) { (backPadTop   + backSize).toPx() }

        // Center controls
        AnimatedVisibility(
            visible = isControlsVisible.value && (!isLoading.value || suppressSpinner || allowControlsWhileLoading),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(Modifier
                .fillMaxSize()
                .pointerInput(showEndButtons) {
                    detectTapGestures(
                        onTap = {
                            if (showEndButtons) return@detectTapGestures
                            isControlsVisible.value = !isControlsVisible.value
                            autoHideEnabled = isControlsVisible.value

                            // normal toggle
//                            isControlsVisible.value = !isControlsVisible.value
//                            autoHideEnabled = isControlsVisible.value
                        }
                    )
                }
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Replay
                    IconButton(
                        onClick = {
                            isReplayPressed = true

                            // Always show controls + suppress spinner for a smooth jump
                            isControlsVisible.value = true
                            suppressSpinner = true
                            coroutineScope.launch {
                                delay(1000)       // safety: auto-clear if READY lags
                                suppressSpinner = false
                            }

                            exoPlayer.seekBack()
                            postCWUpdate()
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
                            isControlsVisible.value = true   // restart 4s timer
                            postCWUpdate()
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

                            isControlsVisible.value = true
                            suppressSpinner = true
                            coroutineScope.launch {
                                delay(1000)
                                suppressSpinner = false
                            }

                            exoPlayer.seekForward()
                            postCWUpdate()
                            coroutineScope.launch { delay(100); isForwardPressed = false }
                        },
                        modifier = Modifier.size(64.dp).scale(forwardScale)
                    ) {
                        Icon(Icons.Default.Forward10, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize())
                    }
                }

                // Brightness control – Netflix-style
                // Brightness rail – taller & thumb hidden
                // --- Brightness rail: inline with Play/Pause buttons, no background ---
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 36.dp, y = (-20).dp)
                        .size(width = 56.dp, height = 140.dp)
                        .zIndex(4f)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WbSunny,
                            contentDescription = "Brightness",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        VerticalBrightnessBar(
                            value = brightness,          // default 0.5f
                            onChange = { v ->
                                brightness = v.coerceIn(0.05f, 1f)
                                (context as? Activity)?.window?.let { w ->
                                    val lp = w.attributes
                                    lp.screenBrightness = brightness
                                    w.attributes = lp
                                }
                                // keep HUD alive while scrubbing
                                isControlsVisible.value = true
                            },
                            onHoldStart = {
                                // exactly like your seekbar: show HUD & pause auto-hide
                                isControlsVisible.value = true
                                autoHideEnabled = false
                            },
                            onHoldEnd = {
                                // release: allow auto-hide timer to resume
                                autoHideEnabled = true
                                isControlsVisible.value = true
                            },
                            modifier = Modifier
                                .width(36.dp)
                                .height(120.dp),
                            trackWidth = 4.dp
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isControlsVisible.value && (!isLoading.value || suppressSpinner || allowControlsWhileLoading),
            enter = slideInVertically(initialOffsetY = { -120 }) + fadeIn(),
            exit  = slideOutVertically(targetOffsetY  = { -120 }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = backPadStart, top = backPadTop)
                    .size(backSize)
                    .animateEnterExit(
                        enter = slideInVertically { -120 } + fadeIn(),
                        exit  = slideOutVertically { -120 } + fadeOut()
                    )
                    .pointerInput(backLoading) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            down.consume()

                            // If already loading, swallow the tap and bail
                            if (backLoading) {
                                waitForUpOrCancellation()
                                return@awaitEachGesture
                            }

                            val up = waitForUpOrCancellation()
                            if (up != null) {
                                backLoading = true     // lock + show spinner immediately

                                when {
                                    showEpisodes -> {
                                        // close overlay, then unlock shortly after to keep UI snappy
                                        showEpisodes = false
                                        autoHideEnabled = true
                                        exoPlayer.playWhenReady = true
                                        scope.launch {
                                            // brief delay so user sees the feedback
                                            kotlinx.coroutines.delay(250)
                                            backLoading = false
                                        }
                                    }
                                    showSubtitleMenu -> {
                                        showSubtitleMenu = false
                                        autoHideEnabled = true
                                        exoPlayer.playWhenReady = true
                                        scope.launch {
                                            kotlinx.coroutines.delay(250)
                                            backLoading = false
                                        }
                                    }
                                    else -> {
                                        postCWUpdate()
                                        // navigating away; no need to unlock (screen will dispose)
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }
                    }
            ) {
                if (backLoading) {
                    // compact spinner so it fits the 42.dp box
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }
            }

        }


        // Bottom: slider + time + (buttons)
        AnimatedVisibility(
            visible = isControlsVisible.value && (!isLoading.value || suppressSpinner || allowControlsWhileLoading),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // overlay box so label shares the same coordinate space as the slider
                        val sliderBoxWidthPx = remember { mutableStateOf(0) }
//                        val density = LocalDensity.current

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .onGloballyPositioned { sliderBoxWidthPx.value = it.size.width } // width of the slider box
                        ) {
                            // the actual slider
                            CustomSlider(
                                progress = seekPosition.value,
                                onSeekChanged = {
                                    isSeeking.value = true
                                    seekPosition.value = it
                                    isControlsVisible.value = true // keep HUD alive while dragging
                                },
                                onSeekStart = {                     // ⬅️ new callback
                                    autoHideEnabled = false         // stop auto-hide during drag
                                    isControlsVisible.value = true  // make sure HUD is visible
                                },
                                onSeekFinished = {
                                    val newPos = (seekPosition.value * duration.value).toLong()
                                    exoPlayer.seekTo(newPos)
                                    currentPosition.value = newPos
                                    isSeeking.value = false
                                    // resume auto-hide only AFTER release
                                    autoHideEnabled = true
                                    isControlsVisible.value = true
                                    postCWUpdate()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                            )


                            // Compute preview text
                            val previewMs = (seekPosition.value * duration.value).toLong()
                            val previewText = formatTime(previewMs)

                            ScrubTimeLabel(
                                visible      = isSeeking.value && sliderBoxWidthPx.value > 0,
                                trackWidthPx = sliderBoxWidthPx.value,
                                progress01   = seekPosition.value,
                                yLift        = (-28).dp,
                                text         = previewText
                            )

                        }

                        Spacer(modifier = Modifier.width(12.dp))

//                        val totalDuration = exoPlayer.duration.coerceAtLeast(0L)
//                        val position = currentPosition.value
//                        val remaining = (totalDuration - position).coerceAtLeast(0L)
//                        Text(text = formatTime(remaining), color = Color.White, fontSize = 14.sp)
                        val totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                        val remaining = (totalDuration - currentPosition.value).coerceAtLeast(0L)
                        Text(text = formatTime(remaining), color = Color.White, fontSize = 14.sp)
                    }

                    val menuIconSz = if (isTablet) 32.dp else 24.dp
                    val menuTextSz = if (isTablet) 18.sp else 16.sp
                    val spacerHeight = if (isTablet) 24.dp else 18.dp
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
                                Row(
                                    modifier = Modifier.clickable {
                                        exoPlayer.playWhenReady = false
                                        showEpisodes = true
                                        isControlsVisible.value = true
                                        autoHideEnabled = false        // ⬅️ stop 4s timer while overlay is up
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.PlaylistPlay, contentDescription = "Episodes", tint = Color.White, modifier = Modifier.size(menuIconSz))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Episodes", color = Color.White, fontSize = menuTextSz)
                                }
                                if (subtitleAvailable) {
                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                // pause and open menu
                                                exoPlayer.playWhenReady = false

                                                val isTextDisabled =
                                                    exoPlayer.trackSelectionParameters
                                                        .disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)

                                                val textGroups =
                                                    exoPlayer.currentTracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }

                                                // is any text track currently selected?
                                                val hasAnyTextSelected = textGroups.any { g ->
                                                    (0 until g.length).any { i ->
                                                        g.isTrackSelected(
                                                            i
                                                        )
                                                    }
                                                }

                                                // is the selected text track the external SRT?
                                                val isExternalSelected = textGroups.any { g ->
                                                    (0 until g.length).any { i ->
                                                        g.isTrackSelected(i) && run {
                                                            val f = g.getTrackFormat(i)
                                                            f.id == "ext_srt" || f.label == "External SRT"
                                                        }
                                                    }
                                                }

                                                pendingSubSelection = when {
                                                    isTextDisabled || !hasAnyTextSelected -> SubtitleOption.OFF
                                                    isExternalSelected -> SubtitleOption.INDONESIAN
                                                    else -> SubtitleOption.ENGLISH
                                                }

                                                showSubtitleMenu = true
                                                isControlsVisible.value = true

                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Subtitles,
                                            contentDescription = "Subtitles",
                                            tint = Color.White,
                                            modifier = Modifier.size(menuIconSz)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Subtitles",
                                            color = Color.White,
                                            fontSize = menuTextSz
                                        )
                                    }
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
                            if (subtitleAvailable) {
                                Row(
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                        .padding(end = 32.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.clickable {
                                            // pause and open menu
                                            exoPlayer.playWhenReady = false

                                            val isTextDisabled = exoPlayer.trackSelectionParameters
                                                .disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)

                                            val textGroups =
                                                exoPlayer.currentTracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }

                                            // is any text track currently selected?
                                            val hasAnyTextSelected = textGroups.any { g ->
                                                (0 until g.length).any { i -> g.isTrackSelected(i) }
                                            }

                                            // is the selected text track the external SRT?
                                            val isExternalSelected = textGroups.any { g ->
                                                (0 until g.length).any { i ->
                                                    g.isTrackSelected(i) && run {
                                                        val f = g.getTrackFormat(i)
                                                        f.id == "ext_srt" || f.label == "External SRT"
                                                    }
                                                }
                                            }

                                            pendingSubSelection = when {
                                                isTextDisabled || !hasAnyTextSelected -> SubtitleOption.OFF
                                                isExternalSelected -> SubtitleOption.INDONESIAN
                                                else -> SubtitleOption.ENGLISH
                                            }

                                            showSubtitleMenu = true
                                            isControlsVisible.value = true
                                        },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Subtitles,
                                            contentDescription = "Subtitles",
                                            tint = Color.White,
                                            modifier = Modifier.size(menuIconSz)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Subtitles",
                                            color = Color.White,
                                            fontSize = menuTextSz
                                        )
                                    }
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

                if (showEpisodes) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xE0000000))
                    ) {
                        // dismiss by tapping background
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    showEpisodes = false
                                    autoHideEnabled = true   // ▲ restore
                                    exoPlayer.playWhenReady = true
                                }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF111111).copy(alpha = 0.95f))
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .navigationBarsPadding()
                        ) {
                            Row(
                                modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 2.dp),            // ↓ small bottom gap under the header,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                                // close Episodes overlay when back is tapped
                                                showEpisodes = false
                                                autoHideEnabled = true
                                                exoPlayer.playWhenReady = true
                                              },
                                    Modifier
                                        .align(Alignment.Top)      // ⬅️ vertical alignment (Row aligns vertically)
                                        .padding(0.dp)

                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                if (epTotalSeasons == 1) {
                                    Spacer(modifier = Modifier.weight(1f))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                    ) {
                                        // LEFT: Show group name if only 1 season
                                        Text(
                                            text = gName.replace("`", "'"),
                                            color = Color.White,
                                            fontSize = if (isTablet) 18.sp else 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis, // Truncate long text
                                        )
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF262626))
                                            .clickable { seasonPickerOpen = true }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Season $epSelectedSeason",
                                            color = Color.White,
                                            fontSize = if (isTablet) 16.sp else 14.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Select Season",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            //Spacer(Modifier.height(4.dp))

                            if (epLoading) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.Red, strokeWidth = 4.dp)
                                }
                            } else {
                                val listState = rememberLazyListState()
                                LaunchedEffect(showEpisodes) {
                                    if (showEpisodes && epActiveIndex >= 0) {
                                        coroutineScope.launch {
                                            delay(120)
                                            listState.animateScrollToItem(epActiveIndex)
                                        }
                                    }
                                }

                                LaunchedEffect(epSelectedSeason, epList.size) {
                                    if (showEpisodes && epList.isNotEmpty()) {
                                        delay(120)
                                        listState.animateScrollToItem(0)
                                    }
                                }

                                // right before LazyRow
                                val selectedIndex = epList.indexOfFirst { it.selectedEps != 0 }.takeIf { it >= 0 } ?: 0


                                LazyRow(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 0.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    itemsIndexed(epList) { idx, ep ->
                                        //val isActive = (epList.indexOf(ep) == epActiveIndex)
                                        val isActive = idx == selectedIndex
                                        //Log.d("isActive rara", "isActives:  $ep.selectedEps")
                                        val cardWidth = if (isTablet) 250.dp else 200.dp
                                        Box(
                                            modifier = Modifier
                                                .width(cardWidth)
                                                //.height(380.dp) // fixed card width
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isActive) Color(0xFF1A1A1A)  else Color.Transparent)
                                                .clickable {
                                                    val next = ep.tvId

                                                    if (next == currentCode) {
                                                        showEpisodes = false
                                                        autoHideEnabled = true
                                                        exoPlayer.playWhenReady = true
                                                        return@clickable
                                                    }

                                                    if (next.isBlank()) {
                                                        android.widget.Toast.makeText(context, "No episode data", android.widget.Toast.LENGTH_SHORT).show()
                                                        return@clickable
                                                    }

                                                    try { exoPlayer.stop() } catch (_: Throwable) {}
                                                    showEpisodes = false
                                                    autoHideEnabled = true

                                                    // clear current data to show spinner, etc.
                                                    vData = null

                                                    // 🔥 this will automatically trigger your LaunchedEffect(currentCode)
                                                    currentCode = next

                                                    epList = epList.map { it.copy(selectedEps = if (it.tvId == next) 1 else 0) }


                                                    exoPlayer.playWhenReady = true
                                                }
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(6.dp)
                                            ) {
                                                // poster
                                                AsyncImage(
                                                    model = ep.tvCvrUrl,
                                                    contentDescription = ep.tvTitle,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .aspectRatio(16f / 9f)
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                )
                                                Spacer(Modifier.height(6.dp))
                                                val tabSpacer = if (isTablet) 4.dp else 2.dp
                                                // title
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(38.dp), // roughly fits two lines of 14sp text
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Text(
                                                        text = ep.tvTitle.fixEncoding(),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp,
                                                        maxLines = 2,                         // ✅ allow up to 2 lines
                                                        overflow = TextOverflow.Ellipsis,
                                                        lineHeight = 18.sp,                   // nice spacing
                                                        modifier = Modifier
                                                            .align(Alignment.CenterStart)     // keep vertical centering
                                                            .padding(horizontal = 4.dp)
                                                    )
                                                }
                                                //Spacer(Modifier.height(4.dp))
                                                // gray separator
                                                Divider(
                                                    color = Color.Gray.copy(alpha = 0.5f),
                                                    thickness = 1.dp,
                                                    modifier = Modifier.padding(
                                                        vertical = 4.dp,
                                                        horizontal = 4.dp
                                                    )
                                                )
                                                Spacer(Modifier.height(tabSpacer))
                                                // duration
                                                Text(
                                                    text = formatDurationFromMinutes(ep.tvDuration),
                                                    color = Color.Gray,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(
                                                        start = 4.dp,
                                                        end = 4.dp
                                                    )
                                                )
                                                Spacer(Modifier.height(tabSpacer))
                                                // description
                                                Text(
                                                    text = ep.tvDescription.fixEncoding(),
                                                    color = Color.LightGray,
                                                    fontSize = 13.sp,
                                                    lineHeight = 18.sp,
                                                    overflow = TextOverflow.Ellipsis, // no ellipsis
                                                    modifier = Modifier
                                                        .padding(4.dp)
                                                        .weight(1f, fill = true)   // let it take remaining space
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (seasonPickerOpen) {
                                FullScreenDialog(onDismissRequest = { seasonPickerOpen = false }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.8f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val scrollState = rememberScrollState()

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .verticalScroll(scrollState)
                                                .background(
                                                    Color.Black.copy(alpha = 0.4f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(vertical = 12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val total = epTotalSeasons ?: 1
                                            repeat(total) { idx ->
                                                val seasonNum = idx + 1
                                                val selected = (seasonNum == epSelectedSeason)

                                                Text(
                                                    text = "Season $seasonNum",
                                                    color = Color.White,
                                                    fontSize = if (selected) 22.sp else 20.sp,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            if (epSelectedSeason != seasonNum) {
                                                                epSelectedSeason = seasonNum
                                                                epList = emptyList()
                                                                epActiveIndex = 0
                                                            }
                                                            seasonPickerOpen = false
                                                        }
                                                        .padding(horizontal = 24.dp, vertical = 10.dp),
                                                    textAlign = TextAlign.Center
                                                )
                                            }

                                            Spacer(Modifier.height(12.dp))

                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color.White)
                                                    .clickable { seasonPickerOpen = false },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowBack,
                                                    contentDescription = "Close",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

        //BackHandler { navController.popBackStack() }
        BackHandler(enabled = true) {
            when {
                showEpisodes -> {
                    showEpisodes = false
                    autoHideEnabled = true
                    exoPlayer.playWhenReady = true
                }
                showSubtitleMenu -> {
                    showSubtitleMenu = false
                    autoHideEnabled = true
                    exoPlayer.playWhenReady = true
                }
                else -> {
                    postCWUpdate()
                    navController.popBackStack()
                }
            }
        }


        if (showSubtitleMenu) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000))
            ) {
                // Dismiss area (covers entire screen, below sheet)
                Spacer(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showSubtitleMenu = false }
                )

                // Bottom sheet
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0xFF111111).copy(alpha = 0.95f))
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Subtitles",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(18.dp))

                    @Composable
                    fun row(label: String, opt: SubtitleOption, enabled: Boolean = true) {
                        val selected = pendingSubSelection == opt
                        val alpha = if (enabled) 1f else 0.35f

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .clickable(enabled = enabled) { pendingSubSelection = opt },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // checkmark closer to text, both centered as a group
                            if (selected) {
                                Text(
                                    "✓",
                                    color = Color.White.copy(alpha = alpha),
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            Text(
                                text = label,
                                color = Color.White.copy(alpha = alpha),
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }


                    }


                    row("Off", SubtitleOption.OFF)
                    row("English", SubtitleOption.ENGLISH)
                    row("Indonesian", SubtitleOption.INDONESIAN, enabled = hasExternalSrt)

                    Spacer(Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Cancel",
                            color = Color.White,
                            modifier = Modifier
                                .clickable {
                                    showSubtitleMenu = false
                                    exoPlayer.playWhenReady = true // resume playback
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Apply",
                            color = Color.Black,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .clickable {
                                    manualSubtitleChange = true
                                    applySubtitleSelection(
                                        selection = pendingSubSelection,
                                        hasExternalSrt = hasExternalSrt,
                                        player = exoPlayer
                                    )
                                    showSubtitleMenu = false
                                    isControlsVisible.value = true
                                    exoPlayer.playWhenReady = true // resume playback
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (showEndButtons) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)   // ✅ works here
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3A3A3A))
                        .clickable {
                            // enter credits mode: hide buttons and stop auto-fill
                            creditsMode = true
                            nextAnimRunning = false
                            nextFill = 0f
                            showEndButtons = false
                            // keep playing to the end
                            exoPlayer.playWhenReady = true
                            postCWDelete()
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Watch Credits", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                NextAutoButton(
                    label = "Next Episode",
                    fill01 = nextFill,
                    onClick = {
                        creditsMode = false
                        nextAnimRunning = false
                        goToNextEpisode()
                    }
                )
            }
        }


    }
}

@Composable
fun RememberAndAttachAudioEffects(
    exoPlayer: ExoPlayer,
    enabled: Boolean,
    strength01: Float,
    boostDb: Float = 4f     // NEW: 0..6 recommended
) {
    DisposableEffect(exoPlayer.audioSessionId, enabled, strength01, boostDb) {
        val session = exoPlayer.audioSessionId
        var enhancer: LoudnessEnhancer? = null
        var dyn: DynamicsProcessing? = null

        if (session != C.AUDIO_SESSION_ID_UNSET && enabled) {
            if (Build.VERSION.SDK_INT < 28) {
                // ---- pre-P fallback: LoudnessEnhancer only ----
                try {
                    val baseMb = (800 + (400 * strength01)).toInt()      // ≈ 8–12 dB
                    val boostMb = (boostDb.coerceIn(0f, 6f) * 100f).toInt()
                    enhancer = LoudnessEnhancer(session).apply {
                        setTargetGain(baseMb + boostMb)                  // add safe extra loudness
                        this.enabled = true
                    }
                } catch (_: Throwable) {}
            } else {
                // ---- Android 9+: MBC + Limiter ----
                try {
                    val mbcBands = 4
                    val cfg = DynamicsProcessing.Config.Builder(
                        DynamicsProcessing.VARIANT_FAVOR_TIME_RESOLUTION,
                        2, false, 0, true, mbcBands, false, 0, true
                    ).build()

                    dyn = DynamicsProcessing(0, session, cfg).apply {
                        val thr   = -26f + (8f * strength01)  // compressor threshold
                        val ratio = 3f   + (3f * strength01)
                        val atk   = 6f
                        val rel   = 120f

                        for (ch in 0 until 2) {
                            val chProc = getChannelByChannelIndex(ch)
                            for (b in 0 until mbcBands) {
                                val band = chProc.mbc.getBand(b)
                                band.isEnabled   = true
                                band.threshold   = thr
                                band.ratio       = ratio
                                band.attackTime  = atk
                                band.releaseTime = rel
                                band.kneeWidth   = 3f
                                band.postGain    = if (b == 2) 3f + 2f*strength01 else 1f + 1f*strength01
                                chProc.mbc.setBand(b, band)
                            }
                            chProc.mbc.isEnabled = true
                            setChannelTo(ch, chProc)
                        }

                        // Final limiter: add master make-up gain safely
                        val safeBoost = boostDb.coerceIn(0f, 6f)
                        val limiter = DynamicsProcessing.Limiter(
                            /*enabled*/ true,
                            /*link*/    true,
                            /*attack*/  1,
                            /*release*/ 50f,
                            /*ratio*/   20f,
                            /*thresh*/  -1.5f,
                            /*knee*/    1.0f,
                            /*post*/    safeBoost              // ★ raise overall loudness here
                        )
                        setLimiterByChannelIndex(0, limiter)
                        setLimiterByChannelIndex(1, limiter)

                        this.enabled = true
                    }
                } catch (_: Throwable) {
                    // fallback if DP init fails
                    try {
                        val baseMb = (900 + (300 * strength01)).toInt()
                        val boostMb = (boostDb.coerceIn(0f, 6f) * 100f).toInt()
                        enhancer = LoudnessEnhancer(session).apply {
                            setTargetGain(baseMb + boostMb)
                            this.enabled = true
                        }
                    } catch (_: Throwable) {}
                }
            }
        }

        onDispose {
            try { enhancer?.release() } catch (_: Throwable) {}
            try { dyn?.release() } catch (_: Throwable) {}
        }
    }
}

@Composable
private fun ScrubTimeLabel(
    visible: Boolean,
    trackWidthPx: Int,
    progress01: Float,          // 0f..1f (seekPosition)
    yLift: Dp = (-28).dp,
    text: String
) {
    val density = LocalDensity.current
    var labelWidthPx by remember { mutableStateOf(0) }

    // compute X in px relative to the track, then to dp
    val clampedTrack = (trackWidthPx - labelWidthPx).coerceAtLeast(0)
    val xPx = (clampedTrack * progress01.coerceIn(0f, 1f)).roundToInt()
    val xDp = with(density) { xPx.toDp() }

    // Always place the container so the Text can measure; animate only the appearance
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(2f) // stay above slider
    ) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            // compute position again here for safety
            val track = (trackWidthPx - labelWidthPx).coerceAtLeast(0)
            val xPx = (track * progress01.coerceIn(0f, 1f)).roundToInt()
            val xDp = with(density) { xPx.toDp() }

            // move the entire pill (background + text)
            Box(
                modifier = Modifier
                    .absoluteOffset(x = xDp, y = yLift)
                    .background(Color.White, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .onGloballyPositioned { labelWidthPx = it.size.width }
            ) {
                Text(
                    text = text,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

private fun applySubtitleSelection(
    selection: SubtitleOption,
    hasExternalSrt: Boolean,
    player: ExoPlayer
) {
    when (selection) {
        SubtitleOption.OFF -> {
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .build()
        }

        SubtitleOption.ENGLISH -> {
            // Explicitly disable all overrides (so embedded track is free to load)
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .build()

            // 🔑 Force re-evaluate tracks so embedded (default) becomes visible
            player.currentTracks.groups.forEach { group ->
                if (group.type == C.TRACK_TYPE_TEXT) {
                    for (i in 0 until group.length) {
                        val f = group.getTrackFormat(i)
                        if (f.label?.contains("English", ignoreCase = true) == true ||
                            f.language == "en") {
                            val override = TrackSelectionOverride(group.mediaTrackGroup, listOf(i))
                            player.trackSelectionParameters = player.trackSelectionParameters
                                .buildUpon()
                                .addOverride(override)
                                .build()
                            return
                        }
                    }
                }
            }
        }

        SubtitleOption.INDONESIAN -> {
            if (!hasExternalSrt) return
            player.currentTracks.groups.forEach { group ->
                if (group.type != C.TRACK_TYPE_TEXT) return@forEach
                for (i in 0 until group.length) {
                    val f = group.getTrackFormat(i)
                    val isExt = f.id == "ext_srt" || f.label == "External SRT"
                    if (isExt) {
                        val override = TrackSelectionOverride(group.mediaTrackGroup, listOf(i))
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                            .addOverride(override)
                            .build()
                        return
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // 🔑 Get the DIALOG window, not the Activity window
        val dialogWindow = (LocalView.current.parent as DialogWindowProvider).window

        // Configure the dialog window to be truly fullscreen
        DisposableEffect(Unit) {
            // draw behind system bars
            WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)
            dialogWindow.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

            // optionally hide bars while open
            val controller = WindowInsetsControllerCompat(dialogWindow, dialogWindow.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())

            onDispose {
                // restore bars on close
                controller.show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(dialogWindow, true)
                dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            }
        }

        // your existing overlay content
        content()
    }
}

@Composable
private fun NextAutoButton(
    label: String,
    fill01: Float,                 // 0f..1f
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val clamped = fill01.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(Color(0xFF3A3A3A)) // base gray
            .drawBehind {
                // draw the white fill from left→right within the button's own size
                drawRoundRect(
                    color = Color.White,
                    size = androidx.compose.ui.geometry.Size(
                        width = size.width * clamped,
                        height = size.height
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        x = 12.dp.toPx(),
                        y = 12.dp.toPx()
                    )
                )
            }
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp) // sets the intrinsic height
    ) {
        val textColor = if (clamped >= 0.55f) Color.Black else Color.White
        Text(
            text = label,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun VerticalBrightnessBar(
    value: Float,                    // 0f..1f
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    trackWidth: Dp = 3.dp,
    activeAlpha: Float = 1f,
    inactiveAlpha: Float = 0.25f,
    minValue: Float = 0.05f,
    onHoldStart: () -> Unit = {},   // NEW
    onHoldEnd: () -> Unit = {}      // NEW
) {
    val density = LocalDensity.current
    val trackPx = with(density) { trackWidth.toPx() }

    Box(
        modifier = modifier
            // Tap to set
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        try {
                            onHoldStart()
                            val h = size.height.toFloat().coerceAtLeast(1f)
                            val newV = 1f - (offset.y / h)
                            onChange(newV.coerceIn(minValue, 1f))
                            // wait for release/cancel (blocks until finger lifts)
                            tryAwaitRelease()
                        } finally {
                            onHoldEnd()
                        }
                    }
                )
            }
            // Drag to adjust
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onHoldStart()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val h = size.height.toFloat().coerceAtLeast(1f)
                        val y = change.position.y
                        val newV = 1f - (y / h)
                        onChange(newV.coerceIn(minValue, 1f))
                    },
                    onDragEnd = { onHoldEnd() },
                    onDragCancel = { onHoldEnd() }
                )
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Inactive full track
            drawLine(
                color = Color.White.copy(alpha = inactiveAlpha),
                start = Offset(w / 2f, 0f),
                end = Offset(w / 2f, h),
                strokeWidth = trackPx,
                cap = StrokeCap.Round
            )
            // Active from bottom up
            val yActive = h * (1f - value.coerceIn(0f, 1f))
            drawLine(
                color = Color.White.copy(alpha = activeAlpha),
                start = Offset(w / 2f, h),
                end = Offset(w / 2f, yActive),
                strokeWidth = trackPx,
                cap = StrokeCap.Round
            )
        }
    }
}