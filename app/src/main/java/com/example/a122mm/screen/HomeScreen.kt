package com.example.a122mm.screen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.a122mm.R
import com.example.a122mm.auth.ProfileViewModel2
import com.example.a122mm.dataclass.BottomNavItem
import com.example.a122mm.helper.setScreenOrientation
import com.example.a122mm.pages.HighlightsPage
import com.example.a122mm.pages.HomePage
import com.example.a122mm.pages.MoviePage
import com.example.a122mm.pages.ProfilePage
import com.example.a122mm.pages.SearchPage
import com.example.a122mm.pages.SeriesPage
import com.example.a122mm.utility.getDeviceId
import kotlinx.coroutines.launch

// Local state to hold devices
data class UiDevice(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,     // phone/tablet/tv
    val lastActive: String,     // formatted
    val isThisDevice: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController) {
    var selectedItem by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val scrollOffset = scrollState.value
    val isHomeTab = selectedItem == 0

    // Search-mode state
    var lastNonSearchTab by rememberSaveable { mutableStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // 🔎 Focus control for Search TextField
    val focusRequester = remember { FocusRequester() }

    // System back = exit search
    BackHandler(enabled = selectedItem == 1) {
        selectedItem = lastNonSearchTab
        searchQuery = ""
    }

    // Restore selected tab when coming back from detail page
//    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
//    LaunchedEffect(savedStateHandle) {
//        savedStateHandle?.getLiveData<Int>("selectedTab")?.observeForever { tabIndex ->
//            selectedItem = tabIndex
//        }
//    }

    LaunchedEffect(Unit) {
        navController.getBackStackEntry("home")
            .savedStateHandle
            .getLiveData<Int>("selectedTab")
            .observeForever { tabIndex ->
                selectedItem = tabIndex
            }
    }

    // Highlights state
    var highlightsSelected by rememberSaveable { mutableStateOf(0) }
    var highlightsCode by rememberSaveable { mutableStateOf("RECENT") }

    val navItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Search", Icons.Filled.Search, Icons.Outlined.Search),
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

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (!isTablet) context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
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
    var hasDominant by remember { mutableStateOf(false) }

    val animatedTopColor by animateColorAsState(
        targetValue = if (scrollOffset > 500) Color.Black else dominantColor,
        animationSpec = tween(durationMillis = 500),
        label = "AnimatedTopColor"
    )

    // GRADIENT that will sit behind the whole Scaffold
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(animatedTopColor, Color.Black)
    )

    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    var logoBottomPx by remember { mutableStateOf(0) }
    var selectorBottomPx by remember { mutableStateOf(0) }
    var selectorTouchesLogo by remember { mutableStateOf(false) }
    var pillsHeightPx by remember { mutableStateOf(0) }

    val isSelectorHidden = selectorBottomPx <= logoBottomPx
    val isBackgroundBlack = scrollOffset > 600

    val targetTopBarColor =
        if (selectedItem == 2) {
            Color.Black
        } else if (selectedItem == 1) {
            Color.Black
        } else if (!isHomeTab) {
            Color.Black.copy(alpha = 0.8f)
        } else {
            if (isBackgroundBlack) {
                Color.Black.copy(alpha = 0.8f)
            } else {
                when {
                    isSelectorHidden    -> dominantColor.copy(alpha = 0.8f)
                    selectorTouchesLogo -> dominantColor.copy(alpha = 1f)
                    else -> dominantColor.copy(
                        alpha = (scrollState.value.coerceIn(0, 300) / 300f) * 0.8f
                    )
                }
            }
        }

    val animatedTopBarColor by animateColorAsState(
        targetValue = targetTopBarColor,
        animationSpec = tween(durationMillis = 500)
    )

    val focusManager = LocalFocusManager.current

    var showLogoutSheet by rememberSaveable { mutableStateOf(false) }

    var showSettings by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showSettings) { showSettings = false }

    val openSignalFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("open_settings_drawer", false)

    val openSignal = openSignalFlow?.collectAsState()

    LaunchedEffect(openSignal?.value) {
        if (openSignal?.value == true) {
            showSettings = true  // open SettingsDrawer
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("open_settings_drawer", false) // reset the flag
        }
    }



    LaunchedEffect(selectedItem, scrollState.isScrollInProgress) {
        if (selectedItem == 1 && scrollState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    // Build repo once per composition (same way you do elsewhere)
    //val context = LocalContext.current
    val repo = remember {
        com.example.a122mm.auth.AuthRepository(
            publicApi = com.example.a122mm.dataclass.AuthNetwork.publicAuthApi,
            authedApi = com.example.a122mm.dataclass.AuthNetwork.authedAuthApi(context),
            store = com.example.a122mm.auth.TokenStore(context)
        )
    }
//    // ✅ Auto-check session validity when HomeScreen opens
//    LaunchedEffect(Unit) {
//        try {
//            val hasLocal = withContext(Dispatchers.IO) { repo.hasSession() }
//            if (!hasLocal) {
//                navController.navigate("login") {
//                    popUpTo("home") { inclusive = true }
//                }
//                return@LaunchedEffect
//            }
//
//            val ok = withContext(Dispatchers.IO) { repo.pingAuth() }
//            if (!ok) {
//                Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
//                navController.navigate("login") {
//                    popUpTo("home") { inclusive = true }
//                }
//            }
//        } catch (t: Throwable) {
//            Toast.makeText(context, "Session check failed.", Toast.LENGTH_SHORT).show()
//            navController.navigate("login") {
//                popUpTo("home") { inclusive = true }
//            }
//        }
//    }

    var profileUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repo.loadProfilePic() // Result<ProfilePicRes>
            .onSuccess { res -> profileUrl = res.pp_link }   // <-- use field
            .onFailure { profileUrl = null }
    }

    // ====== LAYOUT ======
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush) // gradient behind EVERYTHING
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                @OptIn(ExperimentalAnimationApi::class)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(animatedTopBarColor)
                        .padding(
                            top = systemBars.calculateTopPadding(),
                            start = if (selectedItem == 1) 0.dp else 16.dp,
                            end = if (selectedItem == 1) 0.dp else endPadding
                        )
                ) {
                    if (selectedItem == 1) {
                        // Row 1: Back (padded)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // (Back icon intentionally commented out as in your file)
                        }

                        // Row 2: Full-bleed grey search bar
                        val searchRowHeight = 56.dp

                        // 🔥 Request focus every time Search tab is active.
                        // Scope this effect INSIDE the branch so TextField exists first.
                        LaunchedEffect(Unit) {
                            // optional tiny yield if some devices race; uncomment if needed:
                            // delay(30)
                            focusRequester.requestFocus()
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(searchRowHeight)
                                .background(Color(0xFF1A1A1A))
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(searchRowHeight)
                                    .align(Alignment.CenterStart)
                                    .focusRequester(focusRequester), // ← attach requester
                                placeholder = {
                                    Text(
                                        "Search shows, movies, and more…",
                                        color = Color(0xFF8C8C8C),
                                        fontSize = 16.sp
                                    )
                                },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = Color(0xFF8C8C8C)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        Text(
                                            "X",
                                            color = Color(0xFFB3B3B3),
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    searchQuery = ""  // clears input
                                                    // “X” disappears automatically when text is empty
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    } else {
                        // Default top row with logo
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

                            if (selectedItem == 3) {
                                Icon(
                                    imageVector = Icons.Filled.Menu, // or painterResource(R.drawable.ic_menu)
                                    contentDescription = "Menu",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(if (isTablet) 36.dp else 28.dp)
                                        .clickable { showSettings = true  }
                                )
                            }
                        }

                        // HIGHLIGHTS pills (tab 2)
                        if (selectedItem == 2) {
                            val coroutineScope = rememberCoroutineScope()
                            com.example.a122mm.components.DiscoveryFilters(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .onGloballyPositioned { coords ->
                                        pillsHeightPx = coords.size.height
                                    },
                                selectedIndex = highlightsSelected,
                                onSelected = { idx, code ->
                                    highlightsSelected = idx
                                    highlightsCode = code
                                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                                }
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            },

            bottomBar = {
                NoRippleBottomBar(
                    items = navItems,
                    selectedIndex = selectedItem,
                    onSelect = { index ->
                        if (index == 1) {
                            if (selectedItem != 1) lastNonSearchTab = selectedItem
                            selectedItem = 1
                        } else {
                            lastNonSearchTab = index
                            selectedItem = index
                        }
                    },
                    profileUrl = profileUrl,   // ✅ pass the fetched URL here
                    modifier = Modifier
                        .padding(
                            start = navBars.calculateStartPadding(layoutDirection),
                            end = navBars.calculateEndPadding(layoutDirection)
                        )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .background(if (selectedItem == 0) Color.Transparent else Color.Black)
                    .then(
                        if (selectedItem == 1) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { focusManager.clearFocus() }  // hide cursor + keyboard
                                )
                            }
                        } else Modifier
                    )
            ) {
                ContentScreen(
                    modifier = Modifier
                        .padding(
                            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                            bottom = innerPadding.calculateBottomPadding(),
                            top = if (selectedItem == 2) with(LocalDensity.current) { pillsHeightPx.toDp() } else 0.dp
                        ),
                    navController = navController,
                    selectedCategory = selectedCategory,
                    onSelectedCategoryChange = { selectedCategory = it },
                    selectedIndex = selectedItem,
                    scrollState = scrollState,
                    onDominantColorExtracted = { color ->
                        if (isHomeTab) {
                            dominantColor = color
                            hasDominant = true
                        }
                    },
                    logoBottomPx = logoBottomPx,
                    onSelectorBottomChange = { selectorBottomPx = it },
                    onSelectorTouchChange = { selectorTouchesLogo = it },
                    highlightsCode = highlightsCode,
                    searchQuery = searchQuery
                )
            }
        }

        AnimatedVisibility(
            visible = showSettings,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showSettings = false }  // tap outside to close
            )
        }

        // Panel itself (on top; separate AnimatedVisibility so it slides independently)
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit  = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            // The sheet content
            Box(
                modifier = Modifier
                    .fillMaxSize()          // full screen
                    .background(Color.Black) // solid like your screenshot
                    .padding(WindowInsets.systemBars.asPaddingValues()) // ⬅️ add this line
            ) {
                SettingsDrawer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    onBack = { showSettings = false },
                    onAccount = {
                        showSettings = false
                        navController.navigate("settings")
                    },
                    onDeviceManager = { /* TODO: navigate */ },
                    onLogout = { showLogoutSheet = true }
                )
            }
        }
        val vm: ProfileViewModel2 = viewModel()
        //val isTall = isTablet && isLandscape
        if (showLogoutSheet) {

            BackHandler(enabled = true) {
                showLogoutSheet = false
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
            ) {
                // SCRIM
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { showLogoutSheet = false }
                )

                // 🔥 ANIMATED Bottom Sheet
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F0F0F), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    ) {
                        val screenHeight = maxHeight

                        // 1) Compute your target height (you liked 70% vs 45%)
                        val targetHeight = when {
                            isTablet && isLandscape -> screenHeight * 0.70f
                            isTablet && !isLandscape -> screenHeight * 0.45f
                            else -> screenHeight * 0.55f
                        }

                        // 2) Animate between heights
                        val animatedHeight by animateDpAsState(
                            targetValue = targetHeight,
                            // pick one:
                            animationSpec = tween(durationMillis = 300)                      // smooth tween
                            // animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f) // “springy” feel
                        )

                        // 3) Apply animated height
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(animatedHeight)               // ← animated
                                .align(Alignment.BottomCenter)
                                .offset(y = (-24).dp)
                                .imePadding()
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                                .animateContentSize()                 // optional: animates internal reflows
                        ) {
                            ConfirmLogoutContent(
                                onClose = { showLogoutSheet = false },
                                onConfirm = {
                                    showLogoutSheet = false
                                    vm.logout(context)
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun SettingsDrawer(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onAccount: () -> Unit,
    onDeviceManager: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0F0F0F))
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // Header: back + title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .padding(start = 8.dp) // ⬅️ added left padding
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() }
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Settings",
                color = Color.White,
                fontSize = 18.sp
            )
        }

        Divider(color = Color(0x22FFFFFF))
        Spacer(Modifier.height(8.dp))

        // Account Settings
        ListItem(
            headlineContent = { Text("Account Settings", color = Color.White) },
            supportingContent = { Text("Email & account details", color = Color(0xFFB3B3B3)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Color(0x66FFFFFF)
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { onAccount() }
        )

        Divider(color = Color(0x22FFFFFF))

        // === Device Manager (expand/collapse) ===
        var devicesExpanded by rememberSaveable { mutableStateOf(false) }
        val context = LocalContext.current

    // Build repo once here (same pattern you used above)
        val repo = remember {
            com.example.a122mm.auth.AuthRepository(
                publicApi = com.example.a122mm.dataclass.AuthNetwork.publicAuthApi,
                authedApi = com.example.a122mm.dataclass.AuthNetwork.authedAuthApi(context),
                store = com.example.a122mm.auth.TokenStore(context)
            )
        }



        var devices by remember { mutableStateOf<List<UiDevice>>(emptyList()) }
        var loadingDevices by remember { mutableStateOf(false) }
        var loadError by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

        // Expand/collapse header
        ListItem(
            headlineContent = { Text("Device Manager", color = Color.White) },
            supportingContent = { Text("Manage logged-in devices", color = Color(0xFFB3B3B3)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Devices,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = Color(0x66FFFFFF),
                    modifier = Modifier.graphicsLayer {
                        rotationZ = if (devicesExpanded) 180f else 0f
                    }
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable {
                devicesExpanded = !devicesExpanded
                if (devicesExpanded && devices.isEmpty() && !loadingDevices) {
                    loadingDevices = true
                    loadError = null
                    // fire-and-forget load
                    scope.launch {
                        val thisId = getDeviceId(context)
                        repo.listDevices()
                            .onSuccess { list ->
                                devices = list.map {
                                    UiDevice(
                                        deviceId = it.device_id,
                                        deviceName = it.device_name,
                                        deviceType = it.device_type,
                                        lastActive = it.last_active,
                                        isThisDevice = (it.device_id == thisId)
                                    )
                                }
                            }
                            .onFailure { e -> loadError = e.message ?: "Failed to load devices" }
                        loadingDevices = false
                    }
                }
            }
        )

        // Expanded content
        AnimatedVisibility(visible = devicesExpanded) {
            Column(Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(12.dp))

                // This Device card
                val thisDevice = devices.firstOrNull { it.isThisDevice }
                if (thisDevice != null) {
                    DeviceCard(
                        title = "This Device",
                        device = thisDevice,
                        showIndicator = true,
                        onLogout = {
                            scope.launch {
//                                try {
//                                    val thisId = getDeviceId(context)
//                                    // tell server to revoke & delete this device row
//                                    repo.logoutDevice(thisId).onFailure { throw it }
//                                } catch (_: Exception) { /* optional toast/log */ }
//                                // now do your existing local clear + nav
                                onLogout()
                            }
                        }

                    )
                }

                // Other Devices card
                val others = devices.filter { !it.isThisDevice }
                if (others.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFF151515),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Other Devices",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            // No extra Spacer here 👇
                            others.forEach { dev ->
                                DeviceRow(
                                    device = dev,
                                    onLogout = {
                                        scope.launch {
                                            loadingDevices = true
                                            loadError = null
                                            try {
                                                repo.logoutDevice(dev.deviceId).onFailure { throw it }
                                                val thisId = getDeviceId(context)
                                                repo.listDevices()
                                                    .onSuccess { list ->
                                                        devices = list.map {
                                                            UiDevice(
                                                                deviceId = it.device_id,
                                                                deviceName = it.device_name,
                                                                deviceType = it.device_type,
                                                                lastActive = it.last_active,
                                                                isThisDevice = (it.device_id == thisId)
                                                            )
                                                        }
                                                    }
                                                    .onFailure { throw it }
                                            } catch (e: Exception) {
                                                loadError = e.message ?: "Failed to refresh"
                                            } finally {
                                                loadingDevices = false
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (loadingDevices) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White)
                    }
                }
                loadError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color(0xFFFF6B6B), fontSize = 12.sp)
                }

                Spacer(Modifier.height(8.dp))
            }
        }


        Spacer(Modifier.weight(1f))
        //Divider(color = Color(0x22FFFFFF))
        //Spacer(Modifier.height(12.dp))

        // 🔥 SIGN OUT button (red block)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFFE50914), shape = RoundedCornerShape(3.dp)) // Netflix red
                .clickable {
                    scope.launch {
//                    try {
//                        val thisId = getDeviceId(context)
//                        // tell server to revoke & delete this device row
//                        repo.logoutDevice(thisId).onFailure { throw it }
//                    } catch (_: Exception) { /* optional toast/log */ }
                    // now do your existing local clear + nav
                    onLogout()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Sign Out",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Version info
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Version: 1.0.0 build 1",
            color = Color(0xFFAAAAAA),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun ConfirmLogoutContent(
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Close (X)
        Box(Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xAAFFFFFF),
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .clickable { onClose() }
            )
        }

        Spacer(Modifier.height(8.dp))

        // Illustration / icon
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Color.Transparent, RoundedCornerShape(16.dp)), // transparent!
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                tint = Color(0xFFE56B6F),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = "Are you sure you want to sign out?",
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "You will be asked to sign in again to watch your favourites.",
            color = Color(0xFFB3B3B3),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))

        // 🔴 Red Sign Out Button
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(3.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
        ) {
            Text("Sign Out", color = Color.White, fontSize = 16.sp)
        }

        Spacer(Modifier.height(8.dp))

        // ⚪ Cancel Button
        TextButton(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Cancel", color = Color(0xFFB3B3B3), fontSize = 16.sp)
        }
//        val configuration = LocalConfiguration.current
//        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
//        val isTablet = configuration.screenWidthDp >= 600
//
//        /* 👇 Force extra safe zone to clear any taskbar / gesture bar */
//        Spacer(Modifier.height(if (isTablet && isLandscape) 100.dp else 0.dp)) // <- fixed cushion
//        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
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
    onSelectorTouchChange: (Boolean) -> Unit,
    onSelectorBottomChange: (Int) -> Unit,
    highlightsCode: String,
    searchQuery: String
) {
    val layoutDirection = LocalLayoutDirection.current
    val systemBars = WindowInsets.systemBars.asPaddingValues()

    var lastIndex by remember { mutableStateOf(selectedIndex) }
    var lastCategory by remember { mutableStateOf(selectedCategory) }
    val isFilterChange = selectedIndex == 0 && selectedCategory != lastCategory
    val isTabChange = selectedIndex != lastIndex

    LaunchedEffect(selectedIndex, selectedCategory) {
        lastIndex = selectedIndex
        lastCategory = selectedCategory
    }

    var selectorTopPx by remember { mutableStateOf(0) }
    var selectorBottomPx by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
    ) {
        val extraTopBarHeight = when (selectedIndex) {
            1 -> 112.dp // back row + search row
            else -> 64.dp
        }

        Spacer(
            modifier = Modifier.height(systemBars.calculateTopPadding() + extraTopBarHeight)
        )

        if (selectedIndex == 0) {
            AnimatedContent(targetState = selectedCategory, label = "CategoryFilterAnimation") { category ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInWindow()
                            val height = coordinates.size.height
                            val bottomPx = (position.y + height).toInt()
                            selectorTopPx = position.y.toInt()
                            selectorBottomPx = bottomPx
                            onSelectorBottomChange(bottomPx)
                            onSelectorTouchChange(selectorTopPx <= logoBottomPx)
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
                                    .clickable { onSelectedCategoryChange(cat) }
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
                                .clickable { onSelectedCategoryChange(null) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("X", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

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
            val pageModifier = Modifier.fillMaxSize()
            when (index) {
                0 -> when (category) {
                    "Movies" -> MoviePage(pageModifier, navController, onDominantColorExtracted, type = "MOV")
                    "TV Shows" -> SeriesPage(pageModifier, navController, onDominantColorExtracted, type = "TVG")
                    else -> HomePage(pageModifier, navController, onDominantColorExtracted, type = "HOM")
                }
                1 -> SearchPage(
                    modifier = pageModifier,
                    showHeader = false,
                    navController,
                    query = searchQuery
//                    onQueryChange = { /* handled in top bar already; wire later if needed */ }
                )
                2 -> HighlightsPage(modifier = pageModifier, activeCode = highlightsCode, navController = navController )
                3 -> ProfilePage(pageModifier, navController, onDominantColorExtracted, type = "PRO")
            }
        }
    }
}

@Composable
fun NoRippleBottomBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    profileUrl: String?,                // ✅ new param
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .navigationBarsPadding()   // keep above system nav bar
            .height(72.dp)             // 72dp is Material3 default bar height
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedIndex == index
            val isProfile = item.label == "My Room"

            // each cell gets equal space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        indication = null, // no ripple
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (item.isImage) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profileUrl ?: R.drawable.pp_default)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected && isProfile)
                                        Modifier.border(1.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                        )
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = if (isSelected) Color.White else Color.Gray,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = item.label,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    title: String,
    device: UiDevice,
    showIndicator: Boolean,
    onLogout: () -> Unit
) {
    Surface(
        color = Color(0xFF151515),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            DeviceRow(device = device, showIndicator = showIndicator, onLogout = onLogout)
        }
    }
}

@Composable
private fun DeviceRow(
    device: UiDevice,
    showIndicator: Boolean = false,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // === Outer container for icon + badge ===
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (device.deviceType) {
                    "tv" -> {
                        Icon(
                            imageVector = Icons.Outlined.VideoLibrary,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    "tablet" -> {
                        Image(
                            painter = painterResource(id = R.drawable.tablet_ic),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    "phone" -> {
                        Image(
                            painter = painterResource(id = R.drawable.phone_ic),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ✅ Lower badge that "touches" the top-right corner of icon
            if (showIndicator) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)  // ↓ move slightly down & left
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF44D37E))
                        .border(1.dp, Color.Black, CircleShape)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                device.deviceName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Last used : ${device.lastActive}",
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp
            )
        }

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2A2A2A),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Log Out", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}