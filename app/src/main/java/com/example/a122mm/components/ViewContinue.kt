package com.example.a122mm.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.a122mm.dataclass.ApiClient
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

data class ContinueWatchingResponseContent(
    val mId: String,
    val mTitle: String,
    val cvrUrl: String,
    val cPercent: Double?,
    val seasonNum: String?,
    val duration: Int
)

interface ApiServiceContent2 {
    @GET("getcontinuewatching")
    suspend fun getContinueWatching(): List<ContinueWatchingResponseContent>

    @FormUrlEncoded
    @POST("removecontinue")
    suspend fun removecontinue(@Field("mId") mId: String): RemoveResponse
}

data class RemoveResponse(val success: Boolean)

class PosterViewModel2 : ViewModel() {
    private val apiService = ApiClient.create(ApiServiceContent2::class.java)

//    var posters2 by mutableStateOf<List<ContinueWatchingResponseContent>>(emptyList())
//        private set

    private val _posters2 = mutableStateOf<List<ContinueWatchingResponseContent>>(emptyList())
    val posters2: State<List<ContinueWatchingResponseContent>> = _posters2

    init {
        fetchPosters()
    }

    fun removeItemFromContinue(mId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = apiService.removecontinue(mId)
                Log.d("REMOVE", "Success: ${result.success}, Message: $mId")
                if (result.success) {
//                    posters2 = posters2.filterNot { it.mId == mId }
//                    onComplete()

                    // ✅ Reload from server instead of modifying list manually
                    fetchPosters() // your function to refresh the list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onComplete() // still dismiss bottom sheet even on error
            }
        }
    }

    fun fetchPosters() {
        viewModelScope.launch {
            try {
//                posters2 = apiService.getContinueWatching()
                _posters2.value = apiService.getContinueWatching() // 👈 HERE
            } catch (e: Exception) {
//                posters2 = emptyList()
                _posters2.value = emptyList()
                e.printStackTrace()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewContinue(
    modifier: Modifier = Modifier,
    navController: NavController,
    refreshTrigger: Int,
    onRefreshTriggered: () -> Unit,
    currentTabIndex: Int,
    viewModel: PosterViewModel2 = viewModel(),
    type: String
) {
    val posters by viewModel.posters2

    LaunchedEffect(refreshTrigger) {
        viewModel.fetchPosters() // this will re-fetch the list every time refreshTrigger toggles
    }


    if (posters.isEmpty()) {
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

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var selectedPoster by remember { mutableStateOf<ContinueWatchingResponseContent?>(null) }
    val scope = rememberCoroutineScope()

    val systemUiController = rememberSystemUiController()

    LaunchedEffect(sheetState.isVisible) {
        // Always force black navigation bar (even when sheet appears)
        systemUiController.setNavigationBarColor(
            color = Color.Black,
            darkIcons = false // IMPORTANT: darkIcons must be false to force black nav bar on Android 12+
        )
    }


    SideEffect {
        systemUiController.setNavigationBarColor(Color.Black)
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Continue Watching",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posters) { poster ->
            val progress = (poster.cPercent ?: 0.0).toFloat()
            val seasonInfo = poster.seasonNum ?: ""

            val posterWidth = if (isTablet) 150.dp else 100.dp
            val posterHeight = (posterWidth * 3 / 2) + 35.dp  // maintain 2:3 aspect ratio

            Box(
                modifier = Modifier
                    .width(posterWidth)
                    .height(posterHeight)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f) // maintain 3:4 poster image ratio
                ) {
                    AsyncImage(
                        model = poster.cvrUrl,
                        contentDescription = poster.mId,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                // Play Icon (center with ring and offset)
                Box(
                    modifier = Modifier
                        .size(52.dp) // total button size including ring
                        .align(Alignment.Center)
                        .offset(y = (-24).dp) // move upward
                        .border(1.dp, Color.White, CircleShape) // thin white ring
                        .background(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp) // icon size
                    )
                }

                if (seasonInfo.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp) // total gradient height
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 40.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 1.5f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.BottomCenter // keeps text at bottom
                    ) {
                        Text(
                            text = seasonInfo,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 2.dp), // minor spacing from bottom
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(1f, 1f),
                                    blurRadius = 4f
                                ),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                // Bottom black box
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color(0xFF262626), RoundedCornerShape(0.dp))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Red progress bar (dynamic, ensures visibility)
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                        ) {
                            val fullWidth = maxWidth // maxWidth is the width of the parent Box

                            // Background track
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Gray)
                            )

                            val barWidth = if (progress > 0f) {
                                (progress * fullWidth.value).dp.coerceAtLeast(2.dp)
                            } else {
                                0.dp
                            }

                            if (barWidth > 0.dp) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(barWidth)
                                        .background(Color.Red)
                                )
                            }
                        }

                        // Icons row (Info + More)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top =10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Info",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp) // slightly bigger
                                                    .clickable {
                                                        //Log.d("ViewContinue", "Tapped Info for mId: ${poster.mId}")
                                                        navController.currentBackStackEntry
                                                            ?.savedStateHandle
                                                            ?.set("selectedTab", currentTabIndex)
                                                        navController.navigate("movie/${poster.mId}")
                                                    }
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            selectedPoster = poster
                                            coroutineScope.launch { sheetState.show() }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (sheetState.isVisible && selectedPoster != null) {
        ModalBottomSheet(
            onDismissRequest = { scope.launch { sheetState.hide() } },
            sheetState = sheetState,
            containerColor = Color(0xFF2B2B2B), // <-- necessary
            contentColor = Color.White
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = selectedPoster?.mTitle?.replace("`", "'") ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                BottomSheetItem("Episodes & Info", Icons.Outlined.Info) {
                    navController.navigate("movie/${selectedPoster?.mId}")
                    coroutineScope.launch { sheetState.hide() }
                }

//                BottomSheetItem("Download Episode", Icons.Default.Download) { /* TODO */ }
//                BottomSheetItem("Not For Me", Icons.Default.ThumbDown) { /* TODO */ }
//                BottomSheetItem("I Like It", Icons.Default.ThumbUp) { /* TODO */ }
//                BottomSheetItem("Love This", Icons.Default.ThumbUpAlt) { /* TODO */ }
                BottomSheetItem("Remove From Row", Icons.Filled.Close) {
                    selectedPoster?.let { poster ->
                        viewModel.removeItemFromContinue(poster.mId) {
                            coroutineScope.launch {
                                sheetState.hide()
                                onRefreshTriggered()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSheetItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = Color.White)
        Spacer(Modifier.width(16.dp))
        Text(text = label, color = Color.White, fontSize = 16.sp)
    }
}


