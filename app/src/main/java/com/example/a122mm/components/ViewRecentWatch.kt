package com.example.a122mm.components

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.a122mm.R
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.dataclass.ApiClient
import com.example.a122mm.dataclass.AuthNetwork
import com.example.a122mm.helper.fixEncoding
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// API response model
data class RecentWatchResponse(
    val mId: String,
    val mTitle: String,
    val cvrUrl: String,
    val enLogo: String?,
    val gId: String,
    val gName: String,
    val mTitleOnly: String,
    val totalSeason: Int
)

// API Service
interface ApiServiceRecent {
    @GET("getrecentwatch")
    suspend fun getRecentWatch(
        @Query("user_id") userId: Int
    ): List<RecentWatchResponse>

    @FormUrlEncoded
    @POST("addwatchexclude")
    suspend fun addWatchExclude(          // ← rename to camelCase
        @Field("mId") mId: String
    ): Response<Unit>
}

// ViewModel
class RecentWatchViewModel : ViewModel() {
    private val apiService = ApiClient.create(ApiServiceRecent::class.java)

    private val _items = mutableStateOf<List<RecentWatchResponse>>(emptyList())
    val items: State<List<RecentWatchResponse>> = _items

//    init {
//        fetchItems(userId)
//    }

    fun fetchItems(userId: Int) {
        viewModelScope.launch {
            try {
                _items.value = apiService.getRecentWatch(userId)
            } catch (e: Exception) {
                e.printStackTrace()
                _items.value = emptyList()
            }
        }
    }

//    fun hideFromHistory(mId: String, onDone: () -> Unit) {
//        viewModelScope.launch {
//            try { apiService.addWatchExclude(mId) } catch (_: Exception) {}
//            fetchItems()
//            onDone()
//        }
//    }

    fun addWatchExclude(mId: String, userId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                apiService.addWatchExclude(mId)   // fire & forget, like addToMyList
            } catch (_: Exception) { /* you can log here */ }
            fetchItems(userId)                           // refresh the row
            onDone()
        }
    }

}

// UI
@OptIn(ExperimentalMaterial3Api::class)
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
    val context = LocalContext.current
    val repo = remember {
        AuthRepository(
            publicApi = AuthNetwork.publicAuthApi,
            authedApi = AuthNetwork.authedAuthApi(context),
            store = TokenStore(context)
        )
    }
    val userId = remember { repo.getUserId(context) }

    LaunchedEffect(Unit) {
        viewModel.fetchItems(userId)
    }

    LaunchedEffect(refreshTrigger) {
        viewModel.fetchItems(userId)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf<RecentWatchResponse?>(null) }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

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
                    .width(if (isTablet) 260.dp else 220.dp)  // card width
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
                                        selectedItem = item
                                        scope.launch { sheetState.show() }
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
                    val sTitle = if (item.totalSeason == 1) item.mTitleOnly else item.mTitle
                    Text(
                        text = sTitle.fixEncoding() ?: "Untitled",
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
    @OptIn(ExperimentalMaterial3Api::class)
    if (sheetState.isVisible && selectedItem != null) {
        ModalBottomSheet(
            onDismissRequest = { scope.launch { sheetState.hide() } },
            sheetState = sheetState,
            containerColor = Color(0xFF2B2B2B),
            contentColor = Color.White
        ) {
            Column(Modifier.padding(16.dp)) {
                // Header: title + close (like your screenshot)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedItem?.gName?.fixEncoding() ?: "",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { scope.launch { sheetState.hide() } }
                    )
                }
                Spacer(Modifier.height(12.dp))

                // 1) Rewatch Episode XX
                val rewatchLabel = "Rewatch " + (selectedItem?.mTitleOnly?.fixEncoding() ?: "Episode")
                BottomSheetItem(rewatchLabel, Icons.Filled.PlayArrow) {
                    // If you have a direct player route, call it here.
                    // Fallback: open details (user can hit Play)
                    // Write to the "home" destination's SavedStateHandle
                    navController.getBackStackEntry("home")
                        .savedStateHandle["selectedTab"] = 3  // 3 = Profile

                    navController.navigate("playmovie/${selectedItem?.mId}")
                    scope.launch { sheetState.hide() }
                }

                // 2) Episodes & Info
                BottomSheetItem("Episodes & Info", Icons.Outlined.Info) {
                    navController.navigate("movie/${selectedItem?.gId}")
                    scope.launch { sheetState.hide() }
                }

                // 3) Hide From Watch History
                BottomSheetItem(
                    "Hide From Watch History",
                    icon = painterResource(id = R.drawable.ic_cancel)
                ) {
                    selectedItem?.let { sel ->
                        viewModel.addWatchExclude(sel.mId, userId) {
                            Toast.makeText(context, "Hidden from watch history", Toast.LENGTH_SHORT).show()
                            scope.launch { sheetState.hide() }
                            onRefreshTriggered()
                        }
                    }
                }
            }
        }
    }
}