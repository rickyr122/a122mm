package com.example.a122mm.pages

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.ProfileViewModel2
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.components.ProfileHeader
import com.example.a122mm.components.ViewContent
import com.example.a122mm.components.ViewContinue
import com.example.a122mm.components.ViewRecentWatch
import com.example.a122mm.dataclass.AuthNetwork
import com.example.a122mm.dataclass.NetworkModule
import com.example.a122mm.dataclass.ProfileSection
import com.example.a122mm.dataclass.ProfileViewModel
import com.example.a122mm.utility.getDeviceId
import com.example.a122mm.utility.getDeviceName
import com.example.a122mm.utility.getDeviceType
import retrofit2.http.GET
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// API response model
data class topPicks(
    val mId: String,
    val cvrUrl: String
)

// API Service
interface ApiServiceTopPick {
    @GET("gettoppick")
    suspend fun getTopPick(): List<topPicks>

}

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    onDominantColorExtracted: (Color) -> Unit,
    viewModel: ProfileViewModel = viewModel(), // ViewModel only initialized once
    type: String = "PRO"
) {
    val isLoading = viewModel.isLoading
    val allSections = viewModel.allSections

// ✅ Force black once when ProfilePage first shows
    LaunchedEffect(Unit) {
        onDominantColorExtracted(Color.Black)
    }

// ✅ Re-assert black when ProfilePage resumes (e.g., device back)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onDominantColorExtracted(Color.Black)

                viewModel.triggerRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ✅ Keep your existing collector; just also set black when refreshing
    LaunchedEffect(navController) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("refreshContent", false).collect { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.triggerRefresh()               // bumps refreshTrigger
                onDominantColorExtracted(Color.Black)    // re-assert black here too
                handle.set("refreshContent", false)      // reset flag
            }
        }
    }

    val context = LocalContext.current
    val vm: ProfileViewModel2 = viewModel()

    // Build the repo once per composition
    val repo = remember {
        AuthRepository(
            publicApi = AuthNetwork.publicAuthApi,
            authedApi = AuthNetwork.authedAuthApi(context),
            store = TokenStore(context)
        )
    }

    LaunchedEffect(Unit) {
        val did = getDeviceId(context)
        if (!did.isNullOrBlank()) {
            val dname = getDeviceName()
            val dtype = getDeviceType(context)
            val clientTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            repo.registerDevice(did, dname, dtype, clientTime)
        } else {
            Log.w("DeviceRegister", "Skipped registerDevice: deviceId is null or empty")
        }
    }

    val needsRefresh = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<Boolean>("profile_needs_refresh", false)
        ?.collectAsState(initial = false)?.value

    LaunchedEffect(needsRefresh) {
        if (needsRefresh == true) {
            // call your repo.profile() / loadProfilePic() here
            // then reset the flag so it doesn’t loop
            navController.currentBackStackEntry?.savedStateHandle?.set("profile_needs_refresh", false)
        }
    }

    Column (
        modifier = modifier // ✅ use the passed-in modifier
            .fillMaxSize()
            .background(Color.Black)
        //.verticalScroll(scrollState)
    ) {
        ProfileHeader(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onDominantColorExtracted = { onDominantColorExtracted(Color.Black) },
            onLogoutClicked = {
                vm.logout(context)   // your ViewModel logout logic
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        )
        val refreshTrigger = viewModel.refreshTrigger.collectAsState()
        //var hasAnyRealData by remember(refreshTrigger.value) { mutableStateOf(false) }

        var hasContinue by remember(refreshTrigger.value) { mutableStateOf(false) }
        var hasCategory by remember(refreshTrigger.value) { mutableStateOf(false) }
        var hasRecent by remember(refreshTrigger.value) { mutableStateOf(false) }

        // 1) While loading: show spinner (but still compose sections below)
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(32.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

// 2) Always render sections so they can report hasData = true/false
        allSections.forEach { section ->
            when (section) {
                is ProfileSection.Continue -> ViewContinue(
                    modifier,
                    navController,
                    refreshTrigger = refreshTrigger.value,
                    onRefreshTriggered = { viewModel.triggerRefresh() },
                    currentTabIndex = 3,
                    type = type,
                    onHasData = { has ->
                        hasContinue = has
                    }
                )
                is ProfileSection.Category -> ViewContent(
                    modifier,
                    section.code,
                    navController,
                    refreshTrigger = refreshTrigger.value,
                    onRefreshTriggered = { viewModel.triggerRefresh() },
                    currentTabIndex = 3,
                    type = type,
                    onHasData = { has ->
                        hasCategory = has
                    }
                )
                is ProfileSection.RecentWatch -> ViewRecentWatch(
                    modifier,
                    navController,
                    refreshTrigger = refreshTrigger.value,
                    onRefreshTriggered = { viewModel.triggerRefresh() },
                    currentTabIndex = 3,
                    type = type,
                    onHasData = { has ->
                        hasRecent = has
                    }
                )
            }
        }

        // 3) When NOT loading and NO section reported any data → show fallback card
        if (!isLoading && !hasContinue && !hasCategory && !hasRecent) {
            DownloadsForYouEmptyCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .align(Alignment.CenterHorizontally),
                navController = navController
            )
        }

    }
}

@Composable
fun DownloadsForYouEmptyCard(
    modifier: Modifier = Modifier,
    navController: NavController,
    onFindFirstWatchClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val topPickService = remember { NetworkModule.topPickApi }

    var posterUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Load from cache / API once
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            // 1) Try cache first
            val (cached, expired) = DownloadsTopPicksCache.load(context)
            if (cached != null && !expired) {
                posterUrls = cached
            } else {
                // 2) Fallback to API, then save
                val result = topPickService.getTopPick()  // your ApiServiceTopPick
                val urlsFromApi = result.mapNotNull { it.cvrUrl }.take(3)
                posterUrls = urlsFromApi
                if (urlsFromApi.isNotEmpty()) {
                    DownloadsTopPicksCache.save(context, urlsFromApi)
                }
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    // Fallback URLs if not enough items
    val leftUrl = posterUrls.getOrNull(0)

    val centerUrl = posterUrls.getOrNull(1)

    val rightUrl = posterUrls.getOrNull(2)


    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val scaleFactor = if (isTablet) 1.10f else 1.0f
    val topPadding = if (isTablet) 36.dp else 0.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .padding(top = topPadding)
                .graphicsLayer {
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color(0xFF373737),
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Start Exploring",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Browse our large collections of Movies and Shows.",
                    color = Color(0xFFB0B0B0),
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val posterWidth = 120.dp
                    val posterAspect = 2f / 3f

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF202020))
                    )

                    // Left
                    PosterMock(
                        url = leftUrl,
                        modifier = Modifier
                            .offset(x = (-85).dp, y = 40.dp)
                            .graphicsLayer { rotationZ = -15f }
                            .width(posterWidth)
                            .aspectRatio(posterAspect)
                    )

                    // Right
                    PosterMock(
                        url = rightUrl,
                        modifier = Modifier
                            .offset(x = 85.dp, y = 40.dp)
                            .graphicsLayer { rotationZ = 15f }
                            .width(posterWidth)
                            .aspectRatio(posterAspect)
                    )

                    // Center (on top)
                    PosterMock(
                        url = centerUrl,
                        modifier = Modifier
                            .offset(y = (-10).dp)
                            .graphicsLayer { rotationZ = 0f }
                            .width(posterWidth)
                            .aspectRatio(posterAspect)
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(30.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                }

                Button(
                    onClick = {
                        val homeEntry = try {
                            navController.getBackStackEntry("home")
                        } catch (e: Exception) {
                            null
                        }

                        homeEntry?.savedStateHandle?.set("selectedTab", 0)

                        // Also pop back to home if current screen is not home
                        navController.popBackStack("home", false)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .padding(top = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF373737),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 0.dp)
                ) {
                    Text(
                        text = "Find Your First Watch",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (error != null && posterUrls.isEmpty()) {
                    Text(
                        text = "Failed to load suggestions.",
                        color = Color(0xFF777777),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterMock(
    url: String?,
    modifier: Modifier = Modifier
) {
    if (url == null) {
        // Fallback gray box
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2A2A2A))  // gray fallback
        )
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = modifier
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

private object DownloadsTopPicksCache {
    private const val PREFS = "downloads_top_picks_cache"
    private const val KEY_URLS = "urls"
    private const val KEY_TIME = "time"
    private const val ONE_DAY_MS = 24L * 60L * 60L * 1000L

    private fun Context.prefs() =
        getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(context: Context, urls: List<String>) {
        val json = org.json.JSONArray().apply {
            urls.forEach { put(it) }
        }.toString()
        val now = System.currentTimeMillis()

        context.prefs().edit()
            .putString(KEY_URLS, json)
            .putLong(KEY_TIME, now)
            .apply()
    }

    /**
     * @return Pair(urls or null, expiredFlag)
     */
    fun load(context: Context): Pair<List<String>?, Boolean> {
        val prefs = context.prefs()
        val json = prefs.getString(KEY_URLS, null) ?: return null to true
        val savedTime = prefs.getLong(KEY_TIME, 0L)
        val now = System.currentTimeMillis()
        val expired = (now - savedTime) > ONE_DAY_MS

        val arr = org.json.JSONArray(json)
        val urls = buildList {
            for (i in 0 until arr.length()) {
                add(arr.getString(i))
            }
        }
        return urls to expired
    }
}
