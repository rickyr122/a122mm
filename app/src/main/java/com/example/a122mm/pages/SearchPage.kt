package com.example.a122mm.pages

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.a122mm.dataclass.ApiClient
import com.example.a122mm.helper.fixEncoding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query

// ---------- DTOs ----------
data class RecommendedItem(
    val mId: String,
    val mTitle: String,
    val cvrUrl: String,
    val cTag: String?
)

data class SearchItem(
    val mId: String,
    val mTitle: String,
    val cvrUrl: String
)

data class SearchResponse(
    val topResults: List<SearchItem>?,
    val genrePicks: List<SearchItem>?,
    val actorPicks: List<SearchItem>?,
    val byPerson: List<SearchItem>?
)

// ---------- Retrofit services ----------
interface ApiServiceRecommended {
    @GET("getrecommended")
    suspend fun getRecommended(
        @Query("limit") limit: Int = 40
    ): Map<String, @JvmSuppressWildcards Any>
}

interface ApiServiceSearch {
    @GET("search_topresults.php")
    suspend fun search(
        @Query("q") q: String,
        @Query("limitPrimary") limitPrimary: Int = 30,
        @Query("limitGenre") limitGenre: Int = 6,
        @Query("limitActor") limitActor: Int = 6,
        @Query("limitPeople") limitPeople: Int = 12
    ): SearchResponse
}

// ---------- ViewModels ----------
class SearchViewModel : ViewModel() {
    private val api = ApiClient.create(ApiServiceRecommended::class.java)

    private val _items = MutableStateFlow<List<RecommendedItem>>(emptyList())
    val items: StateFlow<List<RecommendedItem>> = _items

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load(limit: Int = 40) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                @Suppress("UNCHECKED_CAST")
                val res = api.getRecommended(limit)
                val ok = (res["ok"] as? Boolean) == true
                if (!ok) throw IllegalStateException("API ok=false")

                val arr = res["results"] as? List<Map<String, Any?>> ?: emptyList()
                val list = arr.mapNotNull { o ->
                    val id = (o["mId"] as? String)?.trim() ?: return@mapNotNull null
                    val title = (o["mTitle"] as? String).orEmpty()
                    val cvr = (o["cvrUrl"] as? String).orEmpty()
                    val tag = o["cTag"] as? String
                    if (cvr.isBlank()) return@mapNotNull null
                    RecommendedItem(
                        mId = id,
                        mTitle = if (title.isBlank()) "(untitled)" else title,
                        cvrUrl = cvr,
                        cTag = tag
                    )
                }
                _items.value = list
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.message ?: "Unknown error"
                _items.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
    fun loadIfNeeded(limit: Int = 40) {
        if (_items.value.isNotEmpty() || _loading.value) return
        load(limit)
    }
}

class SearchResultsViewModel : ViewModel() {
    private val api = ApiClient.create(ApiServiceSearch::class.java)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _results = MutableStateFlow<List<SearchItem>>(emptyList())
    val results: StateFlow<List<SearchItem>> = _results

    fun clear() {
        _results.value = emptyList()
        _error.value = null
        _loading.value = false
    }

    fun search(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                clear()
                return@launch
            }
            _loading.value = true
            _error.value = null
            try {
                val res = api.search(query)
                // Merge arrays, preserve order, dedupe by mId
                val merged = buildList {
                    res.topResults?.let { addAll(it) }
                    res.byPerson?.let { addAll(it) }
                    res.genrePicks?.let { addAll(it) }
                    res.actorPicks?.let { addAll(it) }
                }
                val seen = HashSet<String>()
                val unique = merged.filter { seen.add(it.mId) }
                _results.value = unique
            } catch (e: Exception) {
                _error.value = e.message ?: "Search failed"
                // keep old results to avoid flicker
            } finally {
                _loading.value = false
            }
        }
    }
}

// ---------- UI ----------
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchPage(
    modifier: Modifier = Modifier,
    showHeader: Boolean = false,
    navController: NavController? = null,
    query: String = ""
) {
    // Recommended
    val vm: SearchViewModel = viewModel()
    val recItems by vm.items.collectAsState()
    val recLoading by vm.loading.collectAsState()
    val recError by vm.error.collectAsState()

    // Search
    val svm: SearchResultsViewModel = viewModel()
    val sLoading by svm.loading.collectAsState()
    val sError by svm.error.collectAsState()
    val sItems by svm.results.collectAsState()

    var firstFetchDone by remember(query) { mutableStateOf(false) }

    // Debounce input → call API
    LaunchedEffect(query) {
        if (query.isBlank()) {
            firstFetchDone = false
            svm.clear()
        } else {
            firstFetchDone = false            // reset for this new query
            kotlinx.coroutines.delay(400)
            svm.search(query)                 // suspend → waits for API
            firstFetchDone = true             // mark first response arrived (success or failure)
        }
    }

    LaunchedEffect(Unit) {
        vm.loadIfNeeded(40)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(Modifier.height(if (showHeader) 112.dp else 12.dp))

        val hasQuery = query.isNotBlank()
        val hasResults = sItems.isNotEmpty()
        // Keep Recommended visible while first result is loading
        val showRecommended = !hasQuery || (sLoading && !hasResults)

        // Thin Netflix-style progress bar at the top when searching
        if (sLoading) {
            LinearProgressIndicator(
                color = Color(0xFFE50914),
                trackColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        if (showRecommended) {
            RecommendedSection(recLoading, recError, recItems, navController)
        }

        // Fade the grid in only when we HAVE results (no section crossfade)
        AnimatedVisibility(
            visible = hasResults,
            enter = fadeIn(),
            exit = fadeOut() // optional
        ) {
            Column {
                PosterGrid(items = sItems, navController = navController)
                Spacer(Modifier.height(24.dp))
            }
        }

        // Empty state when query is present, loading finished, and still no results
        if (hasQuery && firstFetchDone && !sLoading && !hasResults) {
            Text(
                text = "No movies or series found",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(Modifier.height(12.dp))
            // Always show Recommended below the empty state
            RecommendedSection(recLoading, recError, recItems, navController)
        }
    }
}

@Composable
private fun RecRow(
    item: RecommendedItem,
    navController: NavController?,
    onRowClick: () -> Unit,    // for row/title/image click
    onPlayClick: () -> Unit    // for play icon click
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }, // whole row click (open detail)
        verticalAlignment = Alignment.CenterVertically
    ) {
        val cardSz = if (isTablet) 160.dp else 120.dp

        // Thumbnail Card
        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier
                .width(cardSz)
                .aspectRatio(16f / 9f)
                .clickable { onRowClick() } // open detail
        ) {
            AsyncImage(
                model = item.cvrUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.width(14.dp))

        // Title + Tag
        Column(
            Modifier
                .weight(1f)
                .clickable { onRowClick() } // same as card click
        ) {
            Text(
                item.mTitle.fixEncoding(),
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!item.cTag.isNullOrBlank()) {
                Text(item.cTag, color = Color(0xFFB3B3B3), fontSize = 12.sp)
            }
        }

        Spacer(Modifier.width(10.dp))

        // Separate Play button click
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .clip(CircleShape)
                .background(Color.Black)
                .clickable { onPlayClick() }, // separate handler
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun PosterGrid(
    items: List<SearchItem>,
    navController: NavController?
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    // 1) phone → 3, tablet portrait → 3, tablet landscape → 4
    val columns = if (isTablet && isLandscape) 5 else 3

    val hGap = 8.dp
    val vGap = 12.dp
    val ctx = LocalContext.current

    // Chunk items into rows of `columns`
    val rows: List<List<SearchItem>> = remember(items, columns) {
        if (items.isEmpty()) emptyList() else items.chunked(columns)
    }

    Column(Modifier.fillMaxWidth()) {
        rows.forEachIndexed { rowIndex, rowItems ->
            if (rowIndex > 0) Spacer(Modifier.height(vGap))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(hGap)
            ) {
                // Render exactly `columns` cells per row. If last row is short, pad with spacers.
                for (i in 0 until columns) {
                    if (i < rowItems.size) {
                        val item = rowItems[i]
                        Card(
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                            modifier = Modifier
                                .weight(1f)              // <-- exact equal width cells
                                .aspectRatio(2f / 3f)    // 2:3 poster
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    navController?.currentBackStackEntry
                                        ?.savedStateHandle?.set("selectedTab", 1)
                                    navController?.navigate("movie/${item.mId}")
                                }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(ctx)
                                    .data(item.cvrUrl)
                                    .crossfade(false) // avoid per-image stacking
                                    .build(),
                                contentDescription = item.mId,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        // pad cell to keep row layout stable
                        Spacer(
                            Modifier
                                .weight(1f)
                                .aspectRatio(2f / 3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendedSection(
    loading: Boolean,
    error: String?,
    items: List<RecommendedItem>,
    navController: NavController?
) {
    Text(
        text = "Recommended TV Shows & Movies",
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 10.dp)
    )
    when {
        loading -> {
            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
            }
        }
        error != null -> {
            Text(
                text = "Failed to load: $error",
                color = Color(0xFFFF6B6B),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
        else -> {
            items.forEachIndexed { idx, item ->
                RecRow(
                    item = item,
                    navController = navController,
                    onRowClick = {
                        // Open detail page
                        navController?.getBackStackEntry("home")
                            ?.savedStateHandle?.set("selectedTab", 1)
                        navController?.navigate("movie/${item.mId}")
                    },
                    onPlayClick = {
                        // Open player directly
                        navController?.getBackStackEntry("home")
                            ?.savedStateHandle?.set("selectedTab", 1)
                        navController?.navigate("playmovie/${item.mId}")
                    }
                )
                if (idx != items.lastIndex) Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}