package com.example.a122mm.pages

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.a122mm.dataclass.ApiClient
import com.example.a122mm.helper.fixEncoding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query

// ---------- DTO ----------
data class RecommendedItem(
    val mId: String,
    val mTitle: String,
    val cvrUrl: String,
    val cTag: String?
)

// ---------- Retrofit service ----------
interface ApiServiceRecommended {
    @GET("get_recommended.php")  // or "get_recommended" if you’re using rewrite rules
    suspend fun getRecommended(
        @Query("limit") limit: Int = 40
    ): Map<String, @JvmSuppressWildcards Any>
}

// ---------- ViewModel ----------
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

// ---------- UI ----------
@Composable
fun SearchPage(
    modifier: Modifier = Modifier,
    showHeader: Boolean = false,
    navController: NavController? = null
) {
    val vm: SearchViewModel = viewModel()
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

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

        Text(
            text = "Recommended TV Shows & Movies",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        when {
            loading -> {
                Box(
                    Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
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
                    RecRow(item = item, navController) {
//                        navController?.currentBackStackEntry
//                            ?.savedStateHandle
//                            ?.set("selectedTab", 1)
//                        navController?.navigate("movie/${item.mId}")
                    }
                    if (idx != items.lastIndex) Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RecRow(
    item: RecommendedItem,
    navController: NavController?,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster
        val cardSz = if (isTablet) 160.dp else 120.dp
        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier
                .width(cardSz)                   // pick your width
                .aspectRatio(16f / 9f)           // enforce 16:9 ratio
                .clickable {
                    //Log.d("mId selected", "selected mId: ${item.mId}")
                    navController?.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedTab", 1)
                    navController?.navigate("movie/${item.mId}")
                }
        ) {
            AsyncImage(
                model = item.cvrUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }


        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)
                        .clickable {
                            //Log.d("mId selected", "selected mId: ${item.mId}")
                            navController?.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedTab", 1)
                            navController?.navigate("movie/${item.mId}")
                        }) {
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

        // ✅ Play button kept
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .clip(CircleShape)
                .background(Color.Black)
                .clickable { onClick() },
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
