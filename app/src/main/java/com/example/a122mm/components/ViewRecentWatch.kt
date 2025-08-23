package com.example.a122mm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.launch
import retrofit2.http.GET

// API response model
data class RecentWatchResponse(
    val mId: String,
    val mTitle: String,
    val cvrUrl: String,
    val enLogo: String?,
    val gId: String,
)

// API Service
interface ApiServiceRecent {
    @GET("getrecentwatch")
    suspend fun getRecentWatch(): List<RecentWatchResponse>
}

// ViewModel
class RecentWatchViewModel : ViewModel() {
    private val apiService = ApiClient.create(ApiServiceRecent::class.java)

    private val _items = mutableStateOf<List<RecentWatchResponse>>(emptyList())
    val items: State<List<RecentWatchResponse>> = _items

    init {
        fetchItems()
    }

    fun fetchItems() {
        viewModelScope.launch {
            try {
                _items.value = apiService.getRecentWatch()
            } catch (e: Exception) {
                e.printStackTrace()
                _items.value = emptyList()
            }
        }
    }
}

// UI
@Composable
fun ViewRecentWatch(
    modifier: Modifier = Modifier,
    navController: NavController,
    refreshTrigger: Int,
    onRefreshTriggered: () -> Unit,
    currentTabIndex: Int,
    viewModel: RecentWatchViewModel = viewModel(),
    type: String
) {
    val items by viewModel.items

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

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Recently Watched",
        fontSize = 16.sp,
        color = Color.White,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Column(
                modifier = Modifier
                    .width(220.dp)  // card width
            ) {
                // 🔹 Image part
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f) // keep ratio strictly on the image
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .clickable {
                            //Log.d("ViewContinue", "Tapped Info for mId: ${poster.mId}")
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedTab", currentTabIndex)
                            navController.navigate("movie/${item.gId}")
                        }
                ) {
                    val imageModel = remember(item.cvrUrl) { item.cvrUrl ?: "" }
                    AsyncImage(
                        model = imageModel,
                        contentDescription = item.mId ?: "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay (logo + three dots)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(70.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent ,              // fade up
                                        Color.Black.copy(alpha = 0.6f) // bottom darker

                                    )
                                )
                            )
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize()
                                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom     // ⬅️ aligns them on the same line
                        ) {
                            // 🔹 Logo on the left (max 30% of image width)
                            if (!item.enLogo.isNullOrBlank()) {
                                val logoModel = remember(item.enLogo) { item.enLogo }
                                AsyncImage(
                                    model = logoModel,
                                    contentDescription = "Logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)  // max 30% width
                                        .wrapContentHeight()
                                )
                            }

                            // 🔹 Three dots on the right
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        // TODO: handle menu
                                    }
                            )
                        }
                    }



                }

                // 🔹 Black title bar under the image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(
                            color = Color(0xFF262626),
                            shape = RoundedCornerShape(
                                bottomStart = 10.dp,
                                bottomEnd = 10.dp
                            )
                        ),
                    contentAlignment = Alignment.Center   // center horizontally + vertically
                ) {
                    Text(
                        text = item.mTitle.fixEncoding() ?: "Untitled",
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,     // ⬅️ adds "…" when text is too long
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }


            }
        }
    }

}
