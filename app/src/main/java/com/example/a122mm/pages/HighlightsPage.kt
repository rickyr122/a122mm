package com.example.a122mm.pages

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.a122mm.dataclass.ApiClient
import com.example.a122mm.helper.InListCache.get
import com.example.a122mm.helper.fixEncoding
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ===== API DTO from backend =====
// Keep only what you need to render. Add fields later if needed.
data class HighlightDto(
    val mId: String,
    val mTitle: String,
    val cvrUrl: String,
    val enLogo: String? = null,
    val mDescription: String,
    val mContent: String? = null,
    val inList: String
)

// ===== Retrofit API (single endpoint with filter) =====
interface HighlightsApi {
    @GET("gethighlights")
    suspend fun getHighlights(
        @Query("filter") filter: String,   // "RECENT" | "SHOULD" | "TOP10_MOV" | "TOP10_TVG"
    ): List<HighlightDto>

    @FormUrlEncoded
    @POST("addmylist")
    suspend fun addToMyList(
        @Field("mId") mId: String,
        @Field("client_time") clientTime: String
    ): Response<Unit>

    @FormUrlEncoded
    @POST("removemylist")
    suspend fun removeFromMyList(@Field("mId") mId: String): Response<Unit>
}

// ===== UI model =====
data class HighlightItem(
    val mId: String,
    val mTitle: String,
    val cvrUrl: String,
    val enLogo: String? = null,
    val mDescription: String? = null,
    val mContent: String,
    val inList: String
)

// ===== ViewModel (same pattern as ViewRecentWatch) =====
sealed interface HighlightsUi {
    data object Loading : HighlightsUi
    data class Data(val items: List<HighlightItem>) : HighlightsUi
    data class Error(val message: String) : HighlightsUi
}

class HighlightsViewModel : ViewModel() {
    private val api = ApiClient.create(HighlightsApi::class.java)

    private val _ui = mutableStateOf<HighlightsUi>(HighlightsUi.Loading)
    val ui: State<HighlightsUi> = _ui

    // cache: one list per filter
    private val cache = androidx.compose.runtime.mutableStateMapOf<String, List<HighlightItem>>()

    // optional: track if we've ever loaded a filter
    private val firstLoadDone = mutableSetOf<String>()

    fun load(filter: String, force: Boolean = false) {
        val cached = cache[filter]

        // 1) If we have cache and not forcing → show it immediately, no spinner
        if (cached != null && !force) {
            _ui.value = HighlightsUi.Data(cached)
        } else {
            // only show spinner on first ever load of a filter
            if (cached == null) _ui.value = HighlightsUi.Loading
        }

        // 2) Special rule for SHOULD: if we already have cache and not forcing, SKIP network
        if (filter == "SHOULD" && cached != null && !force) return

        // 3) Otherwise fetch (SWR for other filters; one-time for SHOULD)
        viewModelScope.launch {
            runCatching { api.getHighlights(filter = filter) }
                .onSuccess { list ->
                    val items = list.map { dto ->
                        HighlightItem(
                            mId = dto.mId,
                            mTitle = dto.mTitle,
                            cvrUrl = dto.cvrUrl,
                            enLogo = dto.enLogo,
                            mDescription = dto.mDescription.fixEncoding(),
                            mContent = dto.mContent!!,
                            inList = dto.inList
                        )
                    }
                    cache[filter] = items
                    firstLoadDone += filter
                    _ui.value = HighlightsUi.Data(items)
                }
                .onFailure { e ->
                    // if we have cache, keep showing it; only error on first ever load
                    if (cached == null) {
                        _ui.value = HighlightsUi.Error(e.message ?: "Failed to load")
                    }
                }
        }
    }

    fun updateInListLocally(filter: String, mId: String, newInList: String) {
        val current = cache[filter] ?: return
        val updated = current.map { it ->
            if (it.mId == mId) it.copy(inList = newInList) else it
        }
        cache[filter] = updated

        // If this filter is currently displayed, reflect it immediately
        _ui.value = HighlightsUi.Data(updated)
    }

    fun forceRefresh(filter: String) = load(filter, force = true)
}


// ===== Composable =====
@Composable
fun HighlightsPage(
    modifier: Modifier = Modifier,
    activeCode: String = "RECENT",
    onRefreshTriggered: () -> Unit = {},
    viewModel: HighlightsViewModel = viewModel()
) {
    // Trigger load when pills change
    LaunchedEffect(activeCode) {
        viewModel.load(activeCode)
    }

    val ui by viewModel.ui

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    val widthMod = if (isTablet && isLandscape) {
        Modifier.fillMaxWidth(0.7f)
    } else {
        Modifier.fillMaxWidth()
    }

    // Keep non-scrollable; HomeScreen owns scroll
    Box(modifier = modifier.fillMaxWidth()) {
        when (val s = ui) {
            is HighlightsUi.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    strokeWidth = 4.dp
                )
            }

            is HighlightsUi.Error -> {
                Text(
                    text = s.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp)
                )
            }

            is HighlightsUi.Data -> {
                Column(
                    modifier = widthMod
                        .align(Alignment.TopCenter) // horizontally center
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    s.items.forEachIndexed { idx, item ->
                        HighlightCard(
                            item = item,
                            activeCode = activeCode,
                            rank = idx + 1,
                            onRefreshTriggered = onRefreshTriggered
                        )
                    }
                    //Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun HighlightCard(
    item: HighlightItem,
    activeCode: String,
    rank: Int,
    onRefreshTriggered: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val myListApi = ApiClient.create(HighlightsApi::class.java)

    //var localInList by androidx.compose.runtime.remember { mutableStateOf(item.inList) }
    var isLoading by remember { mutableStateOf(false) }

    val showTop10 = activeCode == "TOP10_MOV" || activeCode == "TOP10_TVG"

    val highlightsVm: HighlightsViewModel = viewModel()

    // 1) initial state prefers override if present
    var localInList by remember {
        mutableStateOf(get(context, item.mId) ?: item.inList)
    }

    // 2) react to any later override changes published from anywhere
    val overrides = com.example.a122mm.helper.InListCache.map.collectAsStateWithLifecycle().value
    LaunchedEffect(overrides[item.mId]) {
        overrides[item.mId]?.let { localInList = it }
    }

    Box(modifier = Modifier.fillMaxWidth()) {

        // 1) Rank badge card (behind the main card, peeking out top-left)
        if (showTop10) {

            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 0.dp, y = 24.dp) // fully left-aligned
                    .fillMaxWidth()
                    .height(72.dp)
                    .border(
                        width = 1.dp, // thickness of the gradient border
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                            startX = 0f,
                            endX = Float.POSITIVE_INFINITY
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize() //matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF21221C), Color(0xFF1A1A1A))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                }
            }
            // 2b) the Texts
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = (-14).dp)
                    .padding(start = 16.dp)
            ) {
                val text = rank.toString().padStart(2, '0')
                val baseStyle = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 92.sp
                )
                val borderWidth = if (isTablet) 6f else 8f
                // Outline layer (white border only)
                Text(
                    text = text,
                    style = baseStyle.copy(
                        drawStyle = Stroke(width = borderWidth) // thickness of outline
                    ),
                    color = Color.White
                )

                // Fill layer (black inside)
                Text(
                    text = text,
                    style = baseStyle,
                    color = Color.Black
                )
            }

        }

        // 3) Main card
        val pushDown = if (isTablet) 60.dp else 52.dp
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (showTop10) pushDown else 0.dp) // push main card down when TOP10
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // --- Image + overlays ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    AsyncImage(
                        model = item.cvrUrl,
                        contentDescription = item.mTitle,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Top-right: content badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.mContent,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Center: play button
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // --- Logo or title ---
                val logoMaxWidth = if (isTablet) 0.4f else 0.5f
                if (!item.enLogo.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 12.dp, end = 16.dp)
                    ) {
                        AsyncImage(
                            model = item.enLogo,
                            contentDescription = "${item.mTitle} logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth(logoMaxWidth)
                        )
                    }
                } else {
                    Text(
                        text = item.mTitle,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                // --- Description (optional) ---
                if (!item.mDescription.isNullOrBlank()) {
                    Text(
                        text = item.mDescription.fixEncoding(),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // --- Buttons row ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 14.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .height(36.dp)
                            .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Play")
                    }

                    // My List
                    val icon = if (localInList == "1") Icons.Filled.Check else Icons.Filled.Add
                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            isLoading = true
                            val currentlyIn = localInList == "1"
                            val nextValue = if (currentlyIn) "0" else "1"

                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                val clientTime = LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                val resp = try {
                                    if (currentlyIn)
                                        myListApi.removeFromMyList(item.mId)
                                    else
                                        myListApi.addToMyList(item.mId, clientTime)
                                } catch (_: Throwable) { null }

                                if (resp?.isSuccessful == true) {
                                    com.example.a122mm.helper.updateInList(item.mId, nextValue, context)
                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        // 1) instant local flip
                                        localInList = nextValue
                                        // 2) patch VM cache → UI updates without a reload
                                        highlightsVm.updateInListLocally(activeCode, item.mId, nextValue)
                                        // 3) tell the rest of the app (ViewContent, etc.) to refresh
                                        onRefreshTriggered()
                                        // 4) SWR: background refresh for server truth (no spinner since cache exists)
                                        highlightsVm.forceRefresh(activeCode)
                                    }
                                }
                                withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .height(36.dp)
                            .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF333333),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("My List")
                        }
                    }
                }
            }
        }
    }
}
