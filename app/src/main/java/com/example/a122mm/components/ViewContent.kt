package com.example.a122mm.components

import android.content.Context
import android.graphics.pdf.LoadParams
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.dataclass.ApiClient
import com.example.a122mm.dataclass.AuthNetwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query

@Immutable
data class UnwatchedResponseContent(
    val m_id: String,
    val cvrUrl: String,
    //val CatTitle: String,
    val title: String
)

data class HomeMenuResponse(
    val title: String,
    val items: List<UnwatchedResponseContent>
)

interface ApiServiceContent {
    @GET("gethomemenu")
    suspend fun getHomeMenu(
        @Query("code") code: Int,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("user_id") userId: Int
    ): HomeMenuResponse // List<UnwatchedResponseContent>
}

class PosterPagingSource(
    private val apiService: ApiServiceContent,
    private val code: Int,
    private val userId: Int
) : PagingSource<Int, UnwatchedResponseContent>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UnwatchedResponseContent> {
        return try {
            val page = params.key ?: 1
            val response = apiService.getHomeMenu(code, page, params.loadSize, userId)
            LoadResult.Page(
                data = response.items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.items.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UnwatchedResponseContent>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}

class PosterViewModel(
    private val code: Int,
    private val appContext: Context
) : ViewModel() {

    private val apiService = ApiClient.create(ApiServiceContent::class.java)
    private val repo = AuthRepository(
        publicApi = AuthNetwork.publicAuthApi,
        authedApi = AuthNetwork.authedAuthApi(appContext),
        store = TokenStore(appContext)
    )

    private val _title = MutableStateFlow("Loading...")
    val title: StateFlow<String> = _title

    private val _userId = MutableStateFlow(0)
    val userId: StateFlow<Int> = _userId

    // Expose the pager lazily after we have a real user_id.
    // Your Composable can observe this and start collecting when it's non-null.
    private val _pager =
        MutableStateFlow<Flow<PagingData<UnwatchedResponseContent>>?>(null)
    val pager: StateFlow<Flow<PagingData<UnwatchedResponseContent>>?> = _pager

    init {
        viewModelScope.launch {
            // 1) Get real user_id (same pattern as ChooseIconScreen)
            repo.profile()
                .onSuccess { profile ->
                    val uid = profile.id
                    _userId.value = uid

                    // persist for reuse if you want
                    appContext.getSharedPreferences("auth_prefs", 0)
                        .edit().putInt("user_id", uid).apply()

                    // 2) Fetch the section title using the real user_id
                    runCatching {
                        apiService.getHomeMenu(
                            code = code,
                            page = 1,
                            pageSize = 1,
                            userId = uid
                        )
                    }.onSuccess { res ->
                        _title.value = res.title
                    }.onFailure {
                        _title.value = "Unknown"
                        Log.e("HomeMenuDebug", "getHomeMenu (title) failed", it)
                    }

                    // 3) Build Pager AFTER we have user_id so every load carries it
                    _pager.value = Pager(
                        config = PagingConfig(pageSize = 20, initialLoadSize = 20),
                        pagingSourceFactory = {
                            PosterPagingSource(apiService, code, uid)
                        }
                    ).flow.cachedIn(viewModelScope)
                }
                .onFailure {
                    Log.e("HomeMenuDebug", "Failed to fetch profile: ${it.message}")
                    _title.value = "Unknown"
                }
        }
    }

    fun refreshTitle() {
        viewModelScope.launch {
            val uid = _userId.value
            if (uid == 0) return@launch

            runCatching {
                apiService.getHomeMenu(
                    code = code,
                    page = 1,
                    pageSize = 1,
                    userId = uid
                )
            }.onSuccess { res ->
                _title.value = res.title
            }.onFailure { e ->
                Log.e("HomeMenuDebug", "refreshTitle failed", e)
                // Jangan paksa "Unknown" lagi di sini, biarkan title lama kalau ada
            }
        }
    }


    class Factory(
        private val code: Int,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PosterViewModel(code, appContext.applicationContext) as T
        }
    }
}

@Composable
fun ViewContent(
    modifier: Modifier = Modifier,
    code: Int,
    navController: NavController,
    refreshTrigger: Int,
    onRefreshTriggered: () -> Unit,
    currentTabIndex: Int,
    type: String,
    onHasData: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: PosterViewModel = viewModel(
        key = "PosterViewModel_$code",
        factory = PosterViewModel.Factory(code, context)
    )

    val pagerFlow = viewModel.pager.collectAsStateWithLifecycle().value
    val posters = pagerFlow?.collectAsLazyPagingItems()

   // val items = pagerFlow?.collectAsLazyPagingItems()

    if (posters == null) {
        onHasData(false)
        return
    }

    LaunchedEffect(refreshTrigger) {
        viewModel.refreshTitle()
        posters.refresh() // refresh paging data when triggered
    }

    val refreshState = posters.loadState.refresh
    val isEmpty = posters.itemCount == 0


//    if (isEmpty == true && (refreshState is LoadState.NotLoading || refreshState is LoadState.Error)) {
//        // no data for this category -> don't render title/LazyRow at all
//        return
//    }

    when {
        // First load still running and we have no items yet
        isEmpty && refreshState is LoadState.Loading -> {
            onHasData(false)
            // You can choose to draw a small inline loader here or just return.
            return
        }

        // Load finished (or errored) and still no items -> this section is empty
        isEmpty && (refreshState is LoadState.NotLoading || refreshState is LoadState.Error) -> {
            onHasData(false)
            return
        }

        // We have at least one item
        else -> {
            onHasData(true)
        }
    }



//    if (posters.itemCount == 0 && posters.loadState.refresh is LoadState.Loading) {
//    if (posters == null) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(16f / 9f),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator(
//                color = Color.White.copy(alpha = 0.8f),
//                strokeWidth = 4.dp
//            )
//        }
//        return
//    }

    Spacer(modifier = Modifier.height(24.dp))

//    val title = posters.peek(0)?.CatTitle ?: "Movies"
    val titleState = viewModel.title.collectAsStateWithLifecycle()
    val title = titleState.value

    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posters.itemCount) { index ->
            posters[index]?.let { poster ->
                PosterCard(poster, navController, currentTabIndex)
            }
        }

        if (posters.loadState.append is LoadState.Loading) {
            item {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PosterCard(
    poster: UnwatchedResponseContent,
    navController: NavController,
    currentTabIndex: Int
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val posterWidth = if (isTablet) 150.dp else 100.dp
    val posterHeight = posterWidth * 3 / 2

    val imageRequest = remember(poster.cvrUrl) {
        ImageRequest.Builder(context)
            .data(poster.cvrUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Card(
        modifier = Modifier
            .width(posterWidth)
            .height(posterHeight)
            .clickable {
                        //Log.d("selectedTab", "selectedTab: $currentTabIndex")
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedTab", currentTabIndex)
                        navController.navigate("movie/${poster.m_id}")
                       },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = poster.m_id,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
