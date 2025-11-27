package com.example.a122mm.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.ProfileViewModel2
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.components.ProfileHeader
import com.example.a122mm.components.ViewContent
import com.example.a122mm.components.ViewContinue
import com.example.a122mm.components.ViewRecentWatch
import com.example.a122mm.dataclass.AuthNetwork
import com.example.a122mm.dataclass.ProfileSection
import com.example.a122mm.dataclass.ProfileViewModel
import com.example.a122mm.utility.getDeviceId
import com.example.a122mm.utility.getDeviceName
import com.example.a122mm.utility.getDeviceType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    onDominantColorExtracted: (Color) -> Unit,
    viewModel: ProfileViewModel = viewModel(), // ViewModel only initialized once
    type: String = "PRO"
) {
    val isLoading = viewModel.isLoading
    val allSections = viewModel.allSections

// ✅ Force black once when ProfilePage first shows
    LaunchedEffect(Unit) {
        onDominantColorExtracted(Color.Black)
    }

// ✅ Re-assert black when ProfilePage resumes (e.g., device back)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onDominantColorExtracted(Color.Black)

                viewModel.triggerRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ✅ Keep your existing collector; just also set black when refreshing
    LaunchedEffect(navController) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("refreshContent", false).collect { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.triggerRefresh()               // bumps refreshTrigger
                onDominantColorExtracted(Color.Black)    // re-assert black here too
                handle.set("refreshContent", false)      // reset flag
            }
        }
    }

    val context = LocalContext.current
    val vm: ProfileViewModel2 = viewModel()

    // Build the repo once per composition
    val repo = remember {
        AuthRepository(
            publicApi = AuthNetwork.publicAuthApi,
            authedApi = AuthNetwork.authedAuthApi(context),
            store = TokenStore(context)
        )
    }

    LaunchedEffect(Unit) {
        val did = getDeviceId(context)
        if (!did.isNullOrBlank()) {
            val dname = getDeviceName()
            val dtype = getDeviceType(context)
            val clientTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            repo.registerDevice(did, dname, dtype, clientTime)
        } else {
            Log.w("DeviceRegister", "Skipped registerDevice: deviceId is null or empty")
        }
    }

    val needsRefresh = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<Boolean>("profile_needs_refresh", false)
        ?.collectAsState(initial = false)?.value

    LaunchedEffect(needsRefresh) {
        if (needsRefresh == true) {
            // call your repo.profile() / loadProfilePic() here
            // then reset the flag so it doesn’t loop
            navController.currentBackStackEntry?.savedStateHandle?.set("profile_needs_refresh", false)
        }
    }

    Column (
        modifier = modifier // ✅ use the passed-in modifier
            .fillMaxSize()
            .background(Color.Black)
        //.verticalScroll(scrollState)
    ) {
        ProfileHeader(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onDominantColorExtracted = { onDominantColorExtracted(Color.Black) },
            onLogoutClicked = {
                vm.logout(context)   // your ViewModel logout logic
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        )
        val refreshTrigger = viewModel.refreshTrigger.collectAsState()
        //var hasAnyRealData by remember(refreshTrigger.value) { mutableStateOf(false) }

        var hasContinue by remember(refreshTrigger.value) { mutableStateOf(false) }
        var hasCategory by remember(refreshTrigger.value) { mutableStateOf(false) }
        var hasRecent by remember(refreshTrigger.value) { mutableStateOf(false) }

        // 1) While loading: show spinner (but still compose sections below)
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(32.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

// 2) Always render sections so they can report hasData = true/false
        allSections.forEach { section ->
            when (section) {
                is ProfileSection.Continue -> ViewContinue(
                    modifier,
                    navController,
                    refreshTrigger = refreshTrigger.value,
                    onRefreshTriggered = { viewModel.triggerRefresh() },
                    currentTabIndex = 3,
                    type = type,
                    onHasData = { has ->
                        hasContinue = has
                    }
                )
                is ProfileSection.Category -> ViewContent(
                    modifier,
                    section.code,
                    navController,
                    refreshTrigger = refreshTrigger.value,
                    onRefreshTriggered = { viewModel.triggerRefresh() },
                    currentTabIndex = 3,
                    type = type,
                    onHasData = { has ->
                        hasCategory = has
                    }
                )
                is ProfileSection.RecentWatch -> ViewRecentWatch(
                    modifier,
                    navController,
                    refreshTrigger = refreshTrigger.value,
                    onRefreshTriggered = { viewModel.triggerRefresh() },
                    currentTabIndex = 3,
                    type = type,
                    onHasData = { has ->
                        hasRecent = has
                    }
                )
            }
        }

// 3) When NOT loading and NO section reported any data → show fallback card
        if (!isLoading && !hasContinue && !hasCategory && !hasRecent) {
            DownloadsForYouEmptyCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

    }
}

@Composable
fun DownloadsForYouEmptyCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color(0xFF111111)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Turn on Downloads for You",
            color = Color.White,
            // use your own typography
        )
        Text(
            text = "We’ll download movies and shows just for you, so you’ll always have something to watch.",
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        // buttons “Set Up” & “Find More to Download” can go here
    }
}
