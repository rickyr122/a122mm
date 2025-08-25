package com.example.a122mm.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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

    Column (
        modifier = modifier // ✅ use the passed-in modifier
            .fillMaxSize()
            .background(Color.Black)
        //.verticalScroll(scrollState)
    ) {
        ProfileHeader(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onDominantColorExtracted = { onDominantColorExtracted(Color.Black) }
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