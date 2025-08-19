package com.example.a122mm.components

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.a122mm.R
import com.example.a122mm.dataclass.NetworkModule
import com.example.a122mm.dataclass.NetworkModule.mApiService
import com.example.a122mm.sections.CollectionItem
import com.example.a122mm.sections.EpisodeData
import com.example.a122mm.sections.MoreLikeThisItem
import com.example.a122mm.sections.TabCollection
import com.example.a122mm.sections.TabMoreLikeThis
import com.example.a122mm.sections.TabTrailer
import com.example.a122mm.sections.TrailerItem
import com.example.a122mm.sections.TvEpisodes
import com.example.a122mm.utility.BannerStorage.loadBanner
import com.example.a122mm.utility.BannerStorage.saveBanner
import com.example.a122mm.utility.formatDurationFromMinutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// --- Data class for API result
data class MovieDetail(
    val m_id: String,
    val m_title: String,
    val m_year: String,
    val m_rating: String,
    val m_content: String,
    val m_release_date: String,
    val m_duration: Int,
    val m_description: String,
    val m_starring: String,
    val m_director: String,
    val bdropUrl: String,
    val logoUrl: String,
    val rt_state: String,
    val rt_score: Int,
    val audience_state: String,
    val audience_score: Int,
    val c_remaining: Int,
    val c_percent : Double?,
    val gId : String,
    val gName: String,
    val totalSeason: Int,
    val totalEps: Int,
    val activeSeason: Int,
    val activeEps : Int,
    val inList: String,
    val hasCollection: String,
    val hasTrailer: String
)

// --- Retrofit interface
interface MovieApiService {
    @GET("getmoviedetail")
    suspend fun getMovie(@Query("code") code: String): MovieDetail

    @GET("gettvepisodes")
    suspend fun getEpisodes(
        @Query("code") seriesId: String,
        @Query("season") seasonNumber: Int
    ): List<EpisodeData>

    @GET("getcollections")
    suspend fun getCollections(
        @Query("code") movieId: String,
    ): List<CollectionItem>

    @GET("getmorelikethis")
    suspend fun getMoreLikeThis(
        @Query("code") movieId: String,
    ): List<MoreLikeThisItem>

    @GET("gettrailers")
    suspend fun getTrailers(
        @Query("code") movieId: String,
    ): List<TrailerItem>

    @FormUrlEncoded
    @POST("addmylist")
    suspend fun addToMyList(
        @Field("mId") mId: String,
    ): retrofit2.Response<Unit>

    @FormUrlEncoded
    @POST("removemylist")
    suspend fun removeFromMyList(@Field("mId") mId: String
    ): retrofit2.Response<Unit>
}

@Composable
fun ViewMovieDetail(
    modifier: Modifier = Modifier,
    movieId: String,
    navController: NavController,
    onMyListChanged: () -> Unit
) {
    var movie by remember { mutableStateOf<MovieDetail?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val api = NetworkModule.mApiService

    val view = LocalView.current
    val activity = LocalContext.current as Activity

    DisposableEffect(Unit) {
        onDispose {
            onMyListChanged()
        }
    }

    SideEffect {
        val window = activity.window

        // Edge-to-edge with transparent bars
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.BLACK // or any color you want

        val insetsController = WindowCompat.getInsetsController(window, view)

        // Status bar icons
        insetsController.isAppearanceLightStatusBars = false  // dark icons

        // Navigation bar icons
        insetsController.isAppearanceLightNavigationBars = false // light icons for dark nav bar
    }


    LaunchedEffect(movieId) {
        try {
            movie = api.getMovie(movieId)
        } catch (e: Exception) {
            error = e.message
        }
    }

    when {
        movie != null -> MovieDetailContent(movie!!, navController, onMyListChanged)
        error != null -> Text("Error: $error", color = Color.Red, modifier = Modifier.padding(16.dp))
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f), // Match banner space
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.8f),
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

@Composable
fun MovieDetailContent(
    movie: MovieDetail,
    navController: NavController,
    onMyListChanged: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    val rtState = movie.rt_state

    val rtIconRes = when {
        rtState == "rotten" -> R.drawable.rotten
        rtState == "fresh" -> R.drawable.fresh
        else -> R.drawable.certifiedfresh
    }

    val rtAudience = movie.audience_state

    val rtAudienceRes = when {
        rtAudience == "upright" -> R.drawable.upright
        else -> R.drawable.spill
    }

    val displayDuration = if (movie.m_id.startsWith("MOV")) {
        formatDurationFromMinutes(movie.m_duration)
    } else if (movie.m_id.startsWith("TVG")) {
        if (movie.totalSeason == 1) {
            "${movie.totalEps} Episodes"
        } else {
            "${movie.totalSeason} Seasons"
        }
    } else {
        "${movie.totalSeason} Seasons"
    }

    val api = NetworkModule.mApiService
    //val defaultSeason = 1

    var selectedSeasonIndex by remember { mutableStateOf(0) }
    var episodeList by remember { mutableStateOf<List<EpisodeData>>(emptyList()) }
    var episodeError by remember { mutableStateOf<String?>(null) }
    var episodeLoading by remember { mutableStateOf(false) }

    val availableSeasons = remember(movie.totalSeason) {
        (1..movie.totalSeason).map { "Season $it" }
    }

    LaunchedEffect(movie.m_id) {
        if (!movie.m_id.startsWith("MOV")) {
            selectedSeasonIndex = movie.activeSeason - 1

            episodeLoading = true
            try {
                val seasonNumber = movie.activeSeason
                episodeList = api.getEpisodes(movie.gId, seasonNumber)
            } catch (e: Exception) {
                episodeError = e.message
            } finally {
                episodeLoading = false
            }
        }
    }

    var hasCollection by remember { mutableStateOf(true) }
    var hasMoreLikeThis by remember { mutableStateOf(true)}
    var hasTrailer by remember { mutableStateOf(true) }

    var collectionLoading by remember { mutableStateOf(true) }
    var collectionError by remember { mutableStateOf<String?>(null) }
    var collectionItems by remember { mutableStateOf<List<CollectionItem>>(emptyList()) }

    val sendCode = if (movie.m_id.startsWith("MOV")) movie.m_id else movie.gId

    LaunchedEffect(sendCode) {
        collectionLoading = true
        hasCollection = true
        try {
            collectionItems = mApiService.getCollections(sendCode)
        } catch (e: Exception) {
            collectionError = e.message
            if (e is HttpException && e.code() == 404) {
                hasCollection = false // ✅ hide tab if 404
            }
        } finally {
            collectionLoading = false
        }
    }

    var moreLikeThisLoading by remember { mutableStateOf(true) }
    var moreLikeThisError by remember { mutableStateOf<String?>(null) }
    var moreLikeThisItems by remember { mutableStateOf<List<MoreLikeThisItem>>(emptyList()) }

    LaunchedEffect(sendCode) {
        moreLikeThisLoading = true
        hasMoreLikeThis = true
        try {
            moreLikeThisItems = mApiService.getMoreLikeThis(sendCode)
        } catch (e: Exception) {
            moreLikeThisError = e.message
            if (e is HttpException && e.code() == 404) {
                hasMoreLikeThis = false // ✅ hide tab if 404
            }
        } finally {
            moreLikeThisLoading = false
        }
    }

    var trailers by remember { mutableStateOf<List<TrailerItem>>(emptyList()) }
    var trailerError by remember { mutableStateOf<String?>(null) }
    var trailerLoading by remember { mutableStateOf(true) }

    LaunchedEffect(sendCode) {
        trailerLoading = true
        hasTrailer = true
        try {
            trailers = mApiService.getTrailers(sendCode)
        } catch (e: Exception) {
            trailerError = e.message
            if (e is HttpException && e.code() == 404) {
                hasTrailer = false // ✅ hide tab if 404
            }
        } finally {
            trailerLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(
                WindowInsets.statusBars
                    .add(WindowInsets.navigationBars)
                    .asPaddingValues()
            )
    ) {
        // Top bar (sticky)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("refreshBanner", true)
                        navController.popBackStack()
                    }
            )
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        // TODO: Handle search
                    }
            )
        }

        val contentModifier = if (isTablet && isLandscape) {
            Modifier
                .fillMaxWidth(0.6f)
                .align(Alignment.TopCenter)
        } else {
            Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        }

        val progress = (movie.c_percent ?: 0.0).toFloat()

        LaunchedEffect(selectedSeasonIndex) {
            if (!movie.m_id.startsWith("MOV") && movie.activeSeason - 1 != selectedSeasonIndex) {
                episodeLoading = true
                try {
                    val seasonNumber = selectedSeasonIndex + 1
                    episodeList = api.getEpisodes(movie.gId, seasonNumber)
                } catch (e: Exception) {
                    episodeError = e.message
                } finally {
                    episodeLoading = false
                }
            }
        }

        Box(
            modifier = contentModifier
                .padding(top = 60.dp)
        ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight() // 🔥 allow full height
                        .verticalScroll(rememberScrollState())
                ) {
                    // Banner image
                    AsyncImage(
                        model = movie.bdropUrl,
                        contentDescription = movie.m_id,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier.padding(bottom = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            AsyncImage(
                                model = movie.logoUrl,
                                contentDescription = "Title Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(8f),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Year, Rating, Duration
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = movie.m_year,
                                    color = Color(0xFFB3B3B3),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(Modifier.width(8.dp))

                                // Rating box
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF444444), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = movie.m_content,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = displayDuration,
                                    color = Color(0xFFB3B3B3),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // Right: IMDb, RT, Audience
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.imdb),
                                    contentDescription = "IMDb",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = movie.m_rating,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(Modifier.width(8.dp))

                                Icon(
                                    painter = painterResource(id = rtIconRes),
                                    contentDescription = "Rotten Tomatoes",
                                    modifier = Modifier.size(22.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = movie.rt_score.toString() + "%",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(Modifier.width(8.dp))

                                if (rtAudienceRes != 0 && movie.audience_score != 0) {
                                    Icon(
                                        painter = painterResource(id = rtAudienceRes),
                                        contentDescription = "Audience Score",
                                        modifier = Modifier.size(22.dp),
                                        tint = Color.Unspecified
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${movie.audience_score}%",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))

                        val buttonLabel = if (movie.c_remaining > 0) "Resume" else "Play"

                        Button(
                            onClick = { /* TODO: Play */ },
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text(buttonLabel, color = Color.Black)
                        }

                        //this section only appear if progress > 0 (unfinished watching)
                        if (movie.c_remaining > 0) {
                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // LEFT

                                Spacer(Modifier.height(16.dp))

                                Column(modifier = Modifier.weight(1.5f)) {
                                    if (movie.m_title != "") {
                                        Text(
                                            text = movie.m_title.replace("`", "'"),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(12.dp))
                                    }

                                    if ((movie.m_duration * 60) > movie.c_remaining) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(Color(0xFF999999)) //.copy(alpha = 0.6f))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                                                    .height(2.dp)
                                                    .background(Color.Red)
                                            )
                                        }
                                    }
                                }

                                if ((movie.m_duration * 60) > movie.c_remaining) {
                                    // SPACE between columns
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // RIGHT
                                    Column(
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .padding(start = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center // ✅ center text vertically within its own column height
                                    ) {
                                        Text(
                                            text = formatDurationFromMinutes(movie.c_remaining / 60),
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "remaining",
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }

                        Spacer(Modifier.height(6.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(text = movie.m_description.replace("`", "'"), color = Color.White)
                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            color = Color(0xFFB3B3B3),
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("Starring: ")
                                    }
                                    withStyle(style = SpanStyle(color = Color(0xFFB3B3B3))) {
                                        append(movie.m_starring)
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (movie.m_id.startsWith("MOV")) {
                                Log.d("MovieDetail", "Director: '${movie.m_director}'")
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            style = SpanStyle(
                                                color = Color(0xFFB3B3B3),
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append("Director: ")
                                        }
                                        withStyle(style = SpanStyle(color = Color(0xFFB3B3B3))) {
                                            append(movie.m_director)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))

                        val scopes = rememberCoroutineScope()
                        val context = LocalContext.current
                        var isLoading by remember { mutableStateOf(false) }
                        var movieInList by remember { mutableStateOf(movie.inList) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // My List Button (Interactive)
                            val sId = if (movie.m_id.startsWith("MOV")) movie.m_id else movie.gId
                            Box(
                                modifier = Modifier
                                    .clickable(enabled = !isLoading) {
                                        isLoading = true
                                        scopes.launch(Dispatchers.IO) {
                                            try {
                                                val isCurrentlyInList = movieInList == "1"
                                                val newInListValue = if (isCurrentlyInList) "0" else "1"
                                                val response = if (isCurrentlyInList) {
                                                    api.removeFromMyList(sId)
                                                } else {
                                                    api.addToMyList(sId)
                                                }

                                                if (response.isSuccessful) {
                                                    withContext(Dispatchers.Main) {
                                                        movieInList = newInListValue
                                                        updateInList(movie.m_id, newInListValue, context) // optional cache sync
                                                        onMyListChanged() // ✅ triggers ViewBanner + ViewContent refresh
                                                        Toast.makeText(
                                                            context,
                                                            if (newInListValue == "1") "Added to My List" else "Removed from My List",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e("MyList", "Error", e)
                                            } finally {
                                                withContext(Dispatchers.Main) {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    }
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    val icon =
                                        if (movieInList == "1") Icons.Filled.Check else Icons.Filled.Add
                                    MovieAction(icon = icon, label = "My List")
                                }
                            }

//                          MovieAction(icon = Icons.Outlined.ThumbUp, label = "Rate")
                            var isRated by rememberSaveable { mutableStateOf(false) }
                            val interaction = remember { MutableInteractionSource() }
                            val pressed by interaction.collectIsPressedAsState()
                            val pressScale by animateFloatAsState(
                                targetValue = if (pressed) 0.95f else 1f,
                                animationSpec = spring(stiffness = 400f),
                                label = "pressScale"
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isRated) Icons.Outlined.HighlightOff else Icons.Outlined.ThumbUp,
                                    contentDescription = "Rate",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .scale(pressScale)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = interaction,
                                            indication = ripple(bounded = true, color = Color.White)
                                        ) {
                                            isRated = !isRated
                                        }
                                )

                                AnimatedVisibility(
                                    visible = !isRated,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Text(
                                        text = "Rate",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }


                            //MovieAction(icon = Icons.Outlined.Share, label = "Share")
                        }


                        Spacer(Modifier.height(32.dp))

                        val rawTabs = listOf("Episodes", "Collection", "Trailers & More", "More Like This")

                        val tabTitles = remember(movie.m_id, hasCollection, hasTrailer) {
                            rawTabs.filter { tab ->
                                !(movie.m_id.startsWith("MOV") && tab == "Episodes") &&
                                        !(tab == "Collection" && movie.hasCollection == "0") &&
                                        !(tab == "Trailers & More" && movie.hasTrailer == "0")
                            }
                        }

                        var selectedTabIndex by remember { mutableStateOf(0) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Blue)
                        ) {

                            val tabTextSize = if (isTablet) 16.sp else 14.sp
                            val tabTextHorzPadding = if (isTablet) 30.dp else 20.dp

                            Column {
                                ScrollableTabRow(
                                    selectedTabIndex = selectedTabIndex,
                                    backgroundColor = Color.Black,
                                    contentColor = Color.White,
                                    edgePadding = 0.dp,
                                    divider = {}, // Disable internal divider
                                    indicator = { tabPositions ->
                                        TabRowDefaults.Indicator(
                                            modifier = Modifier
                                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                                .height(3.dp)
                                                .width(40.dp),
                                            color = Color.Red
                                        )
                                    }
                                ) {
                                    tabTitles.forEachIndexed { index, title ->
                                        Tab(
                                            selected = selectedTabIndex == index,
                                            onClick = { selectedTabIndex = index },
                                            selectedContentColor = Color.White,
                                            unselectedContentColor = Color.Gray
                                        ) {
                                            Text(
                                                text = title,
                                                fontSize = tabTextSize,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(
                                                    vertical = 14.dp,
                                                    horizontal = tabTextHorzPadding
                                                )
                                            )
                                        }
                                    }
                                }

                                // Custom full-width divider right below the TabRow
                                Divider(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.DarkGray,
                                    thickness = 2.dp
                                )
                            }

                        }
                        // Tab Content
                        val scope = rememberCoroutineScope()
                        when (tabTitles[selectedTabIndex]) {
                            "Episodes" -> {
                                when {
                                    episodeLoading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color.White)
                                        }
                                    }

                                    episodeError != null -> {
                                        Text(
                                            text = "Failed to load episodes: $episodeError",
                                            color = Color.Red,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                    episodeList.isEmpty() -> {
                                        Text(
                                            text = "No episodes found.",
                                            color = Color.Gray,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                    else -> {
                                        TvEpisodes(
                                            modifier = Modifier,
                                            episodes = episodeList,
                                            seasons = availableSeasons,
                                            selectedSeasonIndex = selectedSeasonIndex,
                                            onSeasonSelected = { newIndex ->
                                                selectedSeasonIndex = newIndex
                                                episodeLoading = true
                                                episodeError = null

                                                scope.launch {
                                                    try {
                                                        val seasonNumber = newIndex + 1
                                                        episodeList =
                                                            api.getEpisodes(movie.gId, seasonNumber)
                                                    } catch (e: Exception) {
                                                        episodeError = e.message
                                                    } finally {
                                                        episodeLoading = false
                                                    }
                                                }
                                            },
                                            gName = movie.gName,
                                            activeEpisodeIndex = movie.activeEps - 1,
                                            activeSeason = movie.activeSeason
                                        )
                                    }
                                }
                            }

                            "Collection" -> {
                                when {
                                    collectionLoading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color.White)
                                        }
                                    }

                                    collectionError != null -> {
                                        Text(
                                            text = "Failed to load collections: $collectionError",
                                            color = Color.Red,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                    collectionItems.isEmpty() -> {
                                        Text(
                                            text = "No collection data.",
                                            color = Color.Gray,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                    else -> {
                                        TabCollection(
                                            collection = collectionItems,
                                            navController = navController,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            "Trailers & More" -> {
                                when {
                                    trailerLoading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color.White)
                                        }
                                    }

                                    trailerError != null -> {
                                        Text(
                                            text = "Failed to load trailers: $trailerError",
                                            color = Color.Red,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                    trailers.isEmpty() -> {
                                        Text(
                                            text = "No trailers available.",
                                            color = Color.Gray,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                    else -> {
                                            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                                            trailers.forEach { trailer ->
                                                TabTrailer(trailer,  navController = navController)
                                                Spacer(modifier = Modifier.height(12.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            "More Like This" -> {
                                when {
                                    moreLikeThisLoading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color.White)
                                        }
                                    }

                                    moreLikeThisError != null -> {
                                        Text(
                                            text = "Failed to load moreLikeThis: $moreLikeThisError",
                                            color = Color.Red,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                    moreLikeThisItems.isEmpty() -> {
                                        Text(
                                            text = "No More Like This data.",
                                            color = Color.Gray,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                    else -> {
                                        TabMoreLikeThis(
                                            collection = moreLikeThisItems,
                                            navController = navController,
                                            modifier = Modifier
                                                .fillMaxWidth()
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

@Composable
fun MovieAction(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = Color.White)
        Text(text = label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun animateIconAsState(targetIcon: ImageVector): State<ImageVector> {
    val transition = updateTransition(targetIcon, label = "iconTransition")
    return transition.animateValue(
        transitionSpec = { tween(durationMillis = 300) }, // smooth swap
        typeConverter = TwoWayConverter(
            convertToVector = { _ -> AnimationVector1D(0f) },
            convertFromVector = { _ -> targetIcon }
        ),
        label = "iconAnim"
    ) { state -> state }
}

//fun updateInList(mId: String, inList: String, context: Context) {
//    val (cachedJson, cachedColor, expired) = loadBanner(context)
//    if (cachedJson != null && cachedJson.optString("mId") == mId) {
//        cachedJson.put("inList", inList)
//        saveBanner(context, cachedJson.toString(), cachedColor?.value?.toInt() ?: 0)
//    }
//}

fun updateInList(mId: String, inList: String, context: Context) {
    val types = listOf("HOM", "MOV", "TVG")

    for (type in types) {
        val (cachedJson, cachedColor, expired) = loadBanner(context, type)
        if (cachedJson != null && cachedJson.optString("mId") == mId) {
            cachedJson.put("inList", inList)
            saveBanner(
                context,
                type,
                cachedJson.toString(),
                cachedColor?.value?.toInt() ?: 0
            )
        }
    }
}
