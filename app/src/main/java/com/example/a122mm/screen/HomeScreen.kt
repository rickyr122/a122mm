package com.example.a122mm.screen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.a122mm.R
import com.example.a122mm.dataclass.BottomNavItem
import com.example.a122mm.helper.setScreenOrientation
import com.example.a122mm.pages.FavoritesPage
import com.example.a122mm.pages.HomePage
import com.example.a122mm.pages.MoviePage
import com.example.a122mm.pages.ProfilePage
import com.example.a122mm.pages.SearchPage
import com.example.a122mm.pages.SeriesPage
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController) {
    var selectedItem by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val scrollOffset = scrollState.value
    //val topBarAlpha = (scrollOffset.coerceIn(0, 300) / 300f) * 0.8f

    // Restore selected tab when coming back from detail page
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getLiveData<Int>("selectedTab")?.observeForever { tabIndex ->
            selectedItem = tabIndex
        }
    }

//    val navItems = listOf(
//        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
//        BottomNavItem("Movies", Icons.Filled.Movie, Icons.Outlined.Movie),
//        BottomNavItem("TV Shows", Icons.Filled.Tv, Icons.Outlined.Tv),
//        BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, isImage = true)
//    )

    val navItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem( "Search",Icons.Filled.Search, Icons.Outlined.Search),
        BottomNavItem("Highlights", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
        BottomNavItem("My Room", Icons.Filled.Person, Icons.Outlined.Person, isImage = true)
    )

    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val navBars = WindowInsets.navigationBars.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val endPadding: Dp = with(LocalDensity.current) {
        maxOf(systemBars.calculateEndPadding(layoutDirection), 8.dp)
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    val bannerAspectRatio = when {
        isTablet && isLandscape -> 21f / 9f
        isTablet && !isLandscape -> 16f / 9f
        !isTablet && isLandscape -> 2.5f
        else -> 3f / 4f
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!isTablet) {
            context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    val view = LocalView.current
    val activity = LocalContext.current as Activity

    SideEffect {
        val window = activity.window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.BLACK

        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
    }

    // Dominant color and smooth transition
    var dominantColor by remember { mutableStateOf(Color(0xFF262626)) }

    val animatedTopColor by animateColorAsState(
        targetValue = if (scrollOffset > 500) Color.Black else dominantColor,
        animationSpec = tween(durationMillis = 500),
        label = "AnimatedTopColor"
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(animatedTopColor, Color.Black)
    )

    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    var logoBottomPx by remember { mutableStateOf(0) }
    var selectorTopPx by remember { mutableStateOf(0) }
    var selectorBottomPx by remember { mutableStateOf(0) }

    val density = LocalDensity.current

    // NEW: track if selector is touching logo bottom
    var selectorTouchesLogo by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalAnimationApi::class)
            val isSelectorHidden = selectorBottomPx <= logoBottomPx

            val topBarAlpha = when {
                isSelectorHidden -> 0.5f  // Selector hidden: topBar at 80% opacity
                selectorTouchesLogo -> 0.8f  // Selector visible but touching logo: full opacity
                else -> (scrollState.value.coerceIn(0, 300) / 300f) * 0.8f  // Normal scroll-based alpha
            }

            val isBackgroundBlack = scrollOffset > 600

            val targetTopBarColor = if (isBackgroundBlack) {
                Color.Black.copy(alpha = 0.8f)
            } else {
                when {
                    isSelectorHidden -> dominantColor.copy(alpha = 0.8f)
                    selectorTouchesLogo -> dominantColor.copy(alpha = 1f)
                    else -> dominantColor.copy(alpha = (scrollState.value.coerceIn(0, 300) / 300f) * 0.8f)
                }
            }

            val animatedTopBarColor by animateColorAsState(
                targetValue = targetTopBarColor,
                animationSpec = tween(durationMillis = 500)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(animatedTopBarColor)
                    .padding(
                        top = systemBars.calculateTopPadding(),
                        start = 16.dp,
                        end = endPadding
                    )
            ) {
                // Logo + Search Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.a122mm_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                                    .size(48.dp)
                                    .onGloballyPositioned { coordinates ->
                                        val position = coordinates.positionInWindow()
                                        val height = coordinates.size.height
                                        logoBottomPx = (position.y + height).toInt()
                                    }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = navBars.calculateStartPadding(layoutDirection),
                        end = navBars.calculateEndPadding(layoutDirection)
                    )
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedItem == index
                    val isProfile = item.label == "My Room"

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedItem = index },
                        icon = {
                            if (item.isImage) {
                                Image(
                                    painter = painterResource(id = R.drawable.spiderman),
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isSelected && isProfile) Modifier.border(1.dp, Color.White, CircleShape)
                                            else Modifier
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) Color.White else Color.Gray
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Black
                        )
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .background(
                    if (selectedItem == 0) backgroundBrush
                    else SolidColor(Color.Black)
                )
        ) {

            ContentScreen(
                modifier = Modifier
                    .padding(
                        start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                //.padding(top = contentTopPadding), // <-- add this,
                navController = navController,
                selectedCategory = selectedCategory,
                onSelectedCategoryChange = { selectedCategory = it },
                selectedIndex = selectedItem,
                scrollState = scrollState,
                onDominantColorExtracted = { color -> dominantColor = color },
                logoBottomPx = logoBottomPx,
                onSelectorBottomChange = { bottomPx -> selectorBottomPx = bottomPx },
                onSelectorTouchChange = { touching ->
                    selectorTouchesLogo = touching
                }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    selectedCategory: String?,
    onSelectedCategoryChange: (String?) -> Unit,
    selectedIndex: Int,
    scrollState: ScrollState,
    onDominantColorExtracted: (Color) -> Unit,
    logoBottomPx: Int,
    onSelectorTouchChange: (Boolean) -> Unit,  // NEW
    onSelectorBottomChange: (Int) -> Unit
) {
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current
    val navBars = WindowInsets.navigationBars.asPaddingValues()
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val coroutineScope = rememberCoroutineScope()

    // Remember last tab & filter to detect what changed
    var lastIndex by remember { mutableStateOf(selectedIndex) }
    var lastCategory by remember { mutableStateOf(selectedCategory) }

    // Decide if change is a tab switch or filter change
    val isFilterChange = selectedIndex == 0 && selectedCategory != lastCategory
    val isTabChange = selectedIndex != lastIndex

    // Update trackers
    LaunchedEffect(selectedIndex, selectedCategory) {
        lastIndex = selectedIndex
        lastCategory = selectedCategory
    }

    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val isFilterVisible = selectedIndex == 0
    val filterHeight = if (isFilterVisible) 6.dp + 2.dp else 0.dp
    val contentTopPadding = filterHeight //+ 8.dp //systemBars.calculateTopPadding() + 56.dp + filterHeight

    //var logoBottomPx by remember { mutableStateOf(0) }
    val context = LocalContext.current
    //val coroutineScope = rememberCoroutineScope()

    var selectorTopPx by remember { mutableStateOf(0) }
    var selectorBottomPx by remember { mutableStateOf(0) }

    var hasToastShown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
    ) {
        Spacer(
            modifier = Modifier
                .height(systemBars.calculateTopPadding() + 64.dp)
        )
        // --- Selector moved here, scrolls with content ---
        if (selectedIndex == 0) {
            AnimatedContent(
                targetState = selectedCategory,
                label = "CategoryFilterAnimation"
            ) { category ->

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInWindow()
                            val height = coordinates.size.height
                            val bottomPx = (position.y + height).toInt()
                            selectorTopPx = position.y.toInt()
                            selectorBottomPx = (position.y + height).toInt()

                            // Notify parent if selector top touches or goes above logo bottom
                            onSelectorBottomChange(bottomPx)
                            onSelectorTouchChange(selectorTopPx <= logoBottomPx)

//                            if (selectorTopPx <= logoBottomPx && !hasToastShown) {
//                                hasToastShown = true
//                                Toast.makeText(context, "Selector touches logo bottom!", Toast.LENGTH_SHORT).show()
//                            } else if (selectorTopPx > logoBottomPx) {
//                                hasToastShown = false
//                            }
                        }
                        .graphicsLayer {
                            alpha = if (selectorBottomPx <= logoBottomPx) 0f else 1f
                        }
                ) {
                    if (category == null) {
                        listOf("TV Shows", "Movies").forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color.White, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.Transparent)
                                    .clickable {
                                        onSelectedCategoryChange(cat)
                                        coroutineScope.launch { scrollState.animateScrollTo(0) }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cat, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color.White, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(category, color = Color.Black, fontSize = 14.sp)
                        }

                        Box(
                            modifier = Modifier
                                .border(1.dp, Color.White, CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    onSelectedCategoryChange(null)
                                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("X", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // --- Page content below ---
        AnimatedContent(
            targetState = Pair(selectedIndex, selectedCategory),
            transitionSpec = {
                if (isFilterChange && selectedCategory == null) {
                    (slideInVertically(initialOffsetY = { -40 }) + fadeIn()) with
                            (slideOutVertically(targetOffsetY = { 40 }) + fadeOut())
                } else if (isFilterChange) {
                    (slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth / 4 }) + fadeIn()) with
                            (slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth / 4 }) + fadeOut())
                } else if (isTabChange) {
                    (slideInVertically(initialOffsetY = { fullHeight -> fullHeight / 6 }) + fadeIn()) with
                            (slideOutVertically(targetOffsetY = { fullHeight -> -fullHeight / 6 }) + fadeOut())
                } else {
                    fadeIn() with fadeOut()
                }
            },
            label = "PageChangeAnimation"
        ) { (index, category) ->
            val pageModifier = Modifier
                .padding(top = contentTopPadding)
                .fillMaxSize()

            when (index) {
                0 -> when (category) {
                    "Movies" -> MoviePage(pageModifier, navController, onDominantColorExtracted, type = "MOV")
                    "TV Shows" -> SeriesPage(pageModifier, navController, onDominantColorExtracted, type = "TVG")
                    else -> HomePage(pageModifier, navController, onDominantColorExtracted, type = "HOM")
                }
                1 -> SearchPage(pageModifier)
                2 -> FavoritesPage(pageModifier)
                3 -> ProfilePage(pageModifier,navController, onDominantColorExtracted, type = "PRO")
            }
        }
    }
}
