// SeriesPage.kt
package com.example.a122mm.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.components.PosterViewModel2
import com.example.a122mm.components.ViewBanner
import com.example.a122mm.components.ViewContent
import com.example.a122mm.components.ViewContinue
import com.example.a122mm.components.ViewTopContent
import com.example.a122mm.dataclass.AuthNetwork
import com.example.a122mm.dataclass.Section
import com.example.a122mm.dataclass.SeriesViewModel

@Composable
fun SeriesPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    onDominantColorExtracted: (Color) -> Unit,
    viewModel: SeriesViewModel = viewModel(), // ViewModel only initialized once
    type: String = "TVG",
) {
    val isLoading = viewModel.isLoading
    val allSections = viewModel.allSections

    // Continue Watching VM + state
    val continueVM: PosterViewModel2 = viewModel()
    val posters by continueVM.posters2

    val context = LocalContext.current

    val repo = remember {
        AuthRepository(
            publicApi = AuthNetwork.publicAuthApi,
            authedApi = AuthNetwork.authedAuthApi(context),
            store = TokenStore(context)
        )
    }
    val userId = remember { repo.getUserId(context) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        ViewBanner(
            modifier,
            navController,
            onDominantColorExtracted,
            onMyListChanged = { viewModel.triggerRefresh() },
            type = type,
            currentTabIndex = 0
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(32.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            val refreshTrigger = viewModel.refreshTrigger.collectAsState()

            // Fetch/refresh Continue Watching whenever type or refreshTrigger changes
            LaunchedEffect(type, refreshTrigger.value) {
                continueVM.fetchPosters(type, userId)
            }

            allSections.forEach { section ->
                when (section) {
                    is Section.Continue -> {
                        if (posters.isNotEmpty()) {
                            ViewContinue(
                                modifier = modifier,
                                navController = navController,
                                refreshTrigger = refreshTrigger.value,
                                onRefreshTriggered = { viewModel.triggerRefresh() },
                                currentTabIndex = 0,
                                viewModel = continueVM,
                                type = type
                            )
                        }
                    }
                    is Section.Category -> ViewContent(
                        modifier,
                        section.code,
                        navController,
                        refreshTrigger = refreshTrigger.value,
                        onRefreshTriggered = { viewModel.triggerRefresh() },
                        currentTabIndex = 0,
                        type = type
                    )
                    is Section.TopContent -> ViewTopContent(
                        modifier,
                        navController,
                        currentTabIndex = 0,
                        refreshTrigger = refreshTrigger.value,
                        type = type
                    )
                }
            }
        }
    }
}