package com.example.a122mm.pages

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
import com.example.a122mm.components.ViewBanner
import com.example.a122mm.components.ViewContent
import com.example.a122mm.components.ViewContinue
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

//    LaunchedEffect(Unit) {
//        viewModel.loadSeriesCodes(type)
//    }
    //Spacer(modifier = Modifier.height(14.dp))

    Column (
        modifier = modifier // ✅ use the passed-in modifier
            .fillMaxSize()
//            .verticalScroll(scrollState)
    )  {
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
            allSections.forEach { section ->
                when (section) {
                    is Section.Continue -> ViewContinue(
                        modifier,
                        navController,
                        refreshTrigger = refreshTrigger.value,
                        onRefreshTriggered = { viewModel.triggerRefresh() },
                        currentTabIndex = 0,
                        type = type
                    )
                    is Section.Category -> ViewContent(
                        modifier,
                        section.code,
                        navController,
                        refreshTrigger = refreshTrigger.value,
                        onRefreshTriggered = { viewModel.triggerRefresh() },
                        currentTabIndex = 0,
                        type = type
                    )
                }
            }
        }
    }
}

//@Composable
//fun SeriesPage(modifier: Modifier = Modifier) {
//    Text(text = "Series Page")
//}