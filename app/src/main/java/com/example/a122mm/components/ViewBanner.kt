package com.example.a122mm.components

import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.a122mm.dataclass.NetworkModule
import com.example.a122mm.utility.BannerStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class BannerResponse(
    val mId: String,
    val cvrUrl: String,
    val bdropUrl: String,
    val logoUrl: String,
    val inList: String
)

interface ApiService {
    @GET("getbanner")
    suspend fun getBanner(
        @Query("type") type: String
    ): BannerResponse

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
fun ViewBanner(
    modifier: Modifier = Modifier,
    navController: NavController,
    onDominantColorExtracted: (Color) -> Unit,
    onMyListChanged: () -> Unit ,// 👈 new callback
    type: String,
    currentTabIndex: Int
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    val bannerAspectRatio = when {
        isTablet && isLandscape -> 21f / 9f
        isTablet && !isLandscape -> 16f / 9f
        !isTablet && isLandscape -> 2.5f
        else -> 3f / 4f
    }

    val api = NetworkModule.apiService
    var bannerData by remember { mutableStateOf<BannerResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    //var isInList by remember { mutableStateOf(false) } // ✅ Safe default

    // 👇 Local dominant color just for ViewBanner's gradient
    var localDominantColor by remember { mutableStateOf(Color(0xFF262626)) }

    val context = LocalContext.current

    LaunchedEffect(type) {
        val (cachedJson, cachedColor, expired) = BannerStorage.loadBanner(context, type)
        if (cachedJson != null && !expired) {
            val cachedBanner = BannerResponse(
                mId = cachedJson.getString("mId"),
                cvrUrl = cachedJson.getString("cvrUrl"),
                bdropUrl = cachedJson.getString("bdropUrl"),
                logoUrl = cachedJson.getString("logoUrl"),
                inList = if (cachedJson.has("inList")) cachedJson.getString("inList") else "0"
            )
            bannerData = cachedBanner
            //isInList = cachedBanner.inList == "1"
            localDominantColor = cachedColor ?: Color(0xFF262626)
            onDominantColorExtracted(localDominantColor)
        } else {
            try {
                val result = api.getBanner(type)
                bannerData = result
                //isInList = result.inList == "1"
                // Dominant color will be saved below
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
                e.printStackTrace()
            }
        }
    }


    when {
        bannerData != null -> {
            val banner = bannerData!!

            // Extract dominant color and apply to both local and HomeScreen
            DominantColorBanner(
                imageUrl = if (!isTablet && !isLandscape) banner.cvrUrl else banner.bdropUrl,
                onColorExtracted = {
                    localDominantColor = it
                    onDominantColorExtracted(it)

                    // Save to SharedPreferences
                    val bannerJson = org.json.JSONObject().apply {
                        put("mId", banner.mId)
                        put("cvrUrl", banner.cvrUrl)
                        put("bdropUrl", banner.bdropUrl)
                        put("logoUrl", banner.logoUrl)
                        put("inList", banner.inList)
                    }
                    BannerStorage.saveBanner(context, type, bannerJson.toString(), it.value.toInt())
                }
            )

            val contentWidthModifier = Modifier
                .fillMaxWidth(if (isTablet) 0.5f else 1f)
                .then(if (!isTablet) Modifier.padding(horizontal = 24.dp) else Modifier)
            val horizontalPadding = if (isTablet) 24.dp else 0.dp

            val backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, localDominantColor)
            )

            Box(
                modifier = Modifier
                    .then(
                        if (!isTablet && isLandscape) Modifier else Modifier.padding(horizontal = 16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(localDominantColor, Color.White.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        //Log.d("selectedTab", "selectedTab: $currentTabIndex")
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedTab", currentTabIndex)
                        navController.navigate("movie/${banner.mId}")
                    }
            ) {
                Box {
                    // Image + Gradient overlay
                    Box {
                        AsyncImage(
                            model = if (!isTablet && !isLandscape) banner.cvrUrl else banner.bdropUrl,
                            contentDescription = banner.mId,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(bannerAspectRatio)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(bannerAspectRatio)
                                .clip(RoundedCornerShape(16.dp))
                            //.background(backgroundBrush)
                        ){
                            // Overlay only 25% of the height (for vertical) or width (for horizontal)
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.50f) // Only 25% height for vertical gradient
                                        .align(Alignment.BottomCenter) // Adjust based on direction
                                        .background(backgroundBrush)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = horizontalPadding, bottom = 16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Title Image
                        Box(
                            modifier = contentWidthModifier.padding(bottom = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            AsyncImage(
                                model = banner.logoUrl,
                                contentDescription = "Title Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(3.5f),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Buttons
                        Box(modifier = contentWidthModifier) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { /* Play Clicked */ },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Play", color = Color.Black)
                                }
                                val scope = rememberCoroutineScope()
                                var isLoading by remember { mutableStateOf(false) }
                                val newInListValue = if (banner.inList == "1") "0" else "1"

                                Button(
                                    onClick = {
                                        isLoading = true
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val isCurrentlyInList = banner.inList == "1"
                                                val newInListValue = if (isCurrentlyInList) "0" else "1"
                                                val response = if (isCurrentlyInList) {
                                                    api.removeFromMyList(banner.mId)
                                                } else {
                                                    api.addToMyList(banner.mId)
                                                }

                                                if (response.isSuccessful) {
                                                    // ✅ Update cached inList value
                                                    val bannerJson = org.json.JSONObject().apply {
                                                        put("mId", banner.mId)
                                                        put("cvrUrl", banner.cvrUrl)
                                                        put("bdropUrl", banner.bdropUrl)
                                                        put("logoUrl", banner.logoUrl)
                                                        put("inList", newInListValue)
                                                    }
                                                    BannerStorage.saveBanner(context, type, bannerJson.toString(), localDominantColor.value.toInt())

                                                    withContext(Dispatchers.Main) {
                                                        bannerData = BannerResponse(
                                                            mId = banner.mId,
                                                            cvrUrl = banner.cvrUrl,
                                                            bdropUrl = banner.bdropUrl,
                                                            logoUrl = banner.logoUrl,
                                                            inList = newInListValue
                                                        )
                                                        onMyListChanged() // notify HomePage to refresh ViewContent
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e("MyListAPI", "Error", e)
                                            } finally {
                                                withContext(Dispatchers.Main) {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    }
                                    ,
                                    enabled = !isLoading, // ✅ disable while loading
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height(18.dp)
                                        )
                                    } else {
                                        val icon = if (banner.inList == "1") Icons.Filled.Check else Icons.Filled.Add
                                        Icon(icon, contentDescription = "My List", tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("My List", color = Color.White)
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

        errorMessage != null -> {
            Text("Error: $errorMessage")
        }

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
fun DominantColorBanner(
    imageUrl: String,
    onColorExtracted: (Color) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()

                val result = (loader.execute(request).drawable as? BitmapDrawable)?.bitmap

                result?.let { bitmap ->
                    Palette.from(bitmap).generate { palette ->
                        val swatch = palette?.dominantSwatch
                        if (swatch != null) {
                            val hsl = swatch.hsl
                            val lightness = hsl[2]

                            val color = if (lightness < 0.8f)
                                Color(swatch.rgb)
                            else
                                Color(0xFF262626) // fallback if too light

                            onColorExtracted(color)
                        }
                    }
                }
            } catch (e: Exception) {
                onColorExtracted(Color(0xFF262626))
            }
        }
    }
}