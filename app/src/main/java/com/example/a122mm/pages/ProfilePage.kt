package com.example.a122mm.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.a122mm.auth.ProfileViewModel2
import com.example.a122mm.components.ProfileHeader
import com.example.a122mm.components.ViewContent
import com.example.a122mm.components.ViewContinue
import com.example.a122mm.components.ViewRecentWatch
import com.example.a122mm.dataclass.ProfileSection
import com.example.a122mm.dataclass.ProfileViewModel

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

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(32.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            val refreshTrigger = viewModel.refreshTrigger.collectAsState()
            allSections.forEach { section ->
                when (section) {
                    is ProfileSection.Continue -> ViewContinue(
                        modifier,
                        navController,
                        refreshTrigger = refreshTrigger.value,
                        onRefreshTriggered = { viewModel.triggerRefresh() },
                        currentTabIndex = 3,
                        type = type
                    )
                    is ProfileSection.Category -> ViewContent(
                        modifier,
                        section.code,
                        navController,
                        refreshTrigger = refreshTrigger.value,
                        onRefreshTriggered = { viewModel.triggerRefresh() },
                        currentTabIndex = 3,
                        type = type
                    )
                    is ProfileSection.RecentWatch -> ViewRecentWatch(
                        modifier,
                        navController,
                        refreshTrigger = refreshTrigger.value,
                        onRefreshTriggered = { viewModel.triggerRefresh() },
                        currentTabIndex = 3,
                        type = type
                    )
                }
            }

        }
    }
}