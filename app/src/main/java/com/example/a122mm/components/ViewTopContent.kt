package com.example.a122mm.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.a122mm.dataclass.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query

// ---------- Data class ----------
data class TopContentItem(
    val mId: String,
    val cvrUrl: String?,
    val bdropUrl: String?,
    val logoUrl: String?
)

// ---------- API ----------
interface ApiServiceTopContent {
    @GET("gettopcontent")
    suspend fun getTopContent(
        @Query("type") type: String
    ): List<TopContentItem>   // ✅ API returns array
}

// ---------- ViewModel ----------
class TopContentViewModel(
    private val type: String
) : ViewModel() {

    private val api = ApiClient.create(ApiServiceTopContent::class.java)

    private val _items = MutableStateFlow<List<TopContentItem>>(emptyList())
    val items: StateFlow<List<TopContentItem>> = _items

    fun load() {
        viewModelScope.launch {
            try {
                val res = api.getTopContent(type = type)

                // 🔍 Debug log
                println("DEBUG -> fetched ${res.size} items")
                res.forEach { println(it) }

                _items.value = res
            } catch (e: Exception) {
                e.printStackTrace()
                _items.value = emptyList()
            }
        }
    }
}

// ---------- UI ----------
@Composable
fun ViewTopContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    currentTabIndex: Int,
    refreshTrigger: Int = 0,
    type: String = "MOV"
) {
    val vm: TopContentViewModel = viewModel(
        key = "TopContent_$type",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TopContentViewModel(type) as T
            }
        }
    )

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(refreshTrigger) { vm.load() }

    val items by vm.items.collectAsState()

    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.8f),
                strokeWidth = 4.dp
            )
        }
        return
    }

    Spacer(Modifier.height(24.dp))

    val theTitle = when (type) {
        "HOM" -> "Top 10 TV Shows and Movies"
        "MOV" -> "Top 10 Movies"
        "TVG" -> "Top 10 TV Shows"
        else  -> "Top 10"
    }

    Text(
        text = theTitle,   // ✅ fixed title
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items.size) { idx ->
            val item = items[idx]
            TopCard(
                rank = idx + 1,
                poster = item,
                navController = navController,
                currentTabIndex = currentTabIndex
            )
        }
    }
}

@Composable
private fun TopCard(
    rank: Int,
    poster: TopContentItem,
    navController: NavController,
    currentTabIndex: Int
) {
    val context = LocalContext.current
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Poster sizing
    val cardWidth = if (isTablet) 160.dp else 120.dp
    val cardHeight = cardWidth * 3 / 2

    // Number area + overlap (tweak to taste)
    val numberSpace = if (rank >= 10) 64.dp else 72.dp
    val overlap = if (rank >= 10) 8.dp else 14.dp
    val totalWidth = numberSpace + cardWidth - overlap

    // Fonts & stroke
    val outlineSize = if (isTablet) 140.sp else 110.sp
    val fillSize = if (isTablet) 136.sp else 106.sp // slightly smaller to hide inner stroke
    val strokePx = with(density) { 3.dp.toPx() }

    // Per-digit spacing factor (0.90 = tighter)
    val trackingFactor = 0.90f

    val digits: List<String> = rank.toString().map { it.toString() }

    data class Glyph(
        val outline: androidx.compose.ui.text.TextLayoutResult,
        val fill: androidx.compose.ui.text.TextLayoutResult
    )

    // Measure all digits (outline & fill)
    val glyphs = remember(rank, outlineSize, fillSize) {
        digits.map { d ->
            val o = textMeasurer.measure(
                text = d,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = outlineSize,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            val f = textMeasurer.measure(
                text = d,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = fillSize,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Glyph(o, f)
        }
    }

    // Combined width with tracking (use outline widths)
    val combinedOutlineWidthPx: Float =
        glyphs.fold(0f) { acc, g -> acc + g.outline.size.width.toFloat() } * trackingFactor +
                if (glyphs.isNotEmpty()) glyphs.last().outline.size.width.toFloat() * (1 - trackingFactor) else 0f

    val imageRequest = remember(poster.cvrUrl) {
        ImageRequest.Builder(context)
            .data(poster.cvrUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Box(
        modifier = Modifier
            .height(cardHeight)
            .width(totalWidth)
    ) {
        // LEFT: rank number area (poster overlaps slightly from the right)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(numberSpace)
                .fillMaxHeight()
                .drawBehind {
                    // Center the whole number block
                    val startX: Float = (size.width - combinedOutlineWidthPx) / 2f
                    val maxGlyphHeight: Int = glyphs.maxOfOrNull { it.outline.size.height } ?: 0
                    val startY: Float = (size.height - maxGlyphHeight.toFloat()) / 2f

                    var cursorX: Float = startX
                    glyphs.forEachIndexed { index, g ->
                        // Outline (white border)
                        drawText(
                            textLayoutResult = g.outline,
                            topLeft = androidx.compose.ui.geometry.Offset(cursorX, startY),
                            color = Color.White,
                            drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = strokePx)
                        )

                        // Fill (black, slightly smaller to hide inner stroke)
                        val fillX: Float =
                            cursorX + (g.outline.size.width.toFloat() - g.fill.size.width.toFloat()) / 2f
                        val fillY: Float =
                            startY + (g.outline.size.height.toFloat() - g.fill.size.height.toFloat()) / 2f
                        drawText(
                            textLayoutResult = g.fill,
                            topLeft = androidx.compose.ui.geometry.Offset(fillX, fillY),
                            color = Color.Black
                        )

                        // Advance with tracking (tight spacing)
                        val advance: Float = if (index < glyphs.lastIndex)
                            g.outline.size.width.toFloat() * trackingFactor
                        else
                            g.outline.size.width.toFloat() // last digit, no shrink
                        cursorX += advance
                    }
                }
        )

        // RIGHT: poster card, overlapping into the number area
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-overlap))
                .width(cardWidth)
                .height(cardHeight)
                .clickable {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedTab", currentTabIndex)
                    navController.navigate("movie/${poster.mId}")
                }
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}