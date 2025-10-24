package com.example.a122mm.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.a122mm.auth.AuthApiService
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.dataclass.AuthNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChooseIconScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Build repo once (same pattern as your other screens)
    val repo = remember {
        AuthRepository(
            publicApi = AuthNetwork.publicAuthApi,
            authedApi = AuthNetwork.authedAuthApi(context),
            store = TokenStore(context)
        )
    }
    var userId by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        // Fetch the logged-in user's profile (authenticated via token)
        repo.profile()
            .onSuccess { profile ->
                userId = profile.id  // or profile.user_id depending on your response model
            }
            .onFailure {
                Log.e("ChooseIcon", "Failed to load user id: ${it.message}")
            }
    }


    var sections by remember { mutableStateOf<List<AuthApiService.IconSection>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var applying by remember { mutableStateOf(false) }

    // load sections once
    LaunchedEffect(Unit) {
        loading = true
        val res = repo.loadIconSections(userId)
        loading = false
        res.onSuccess { sections = it }
            .onFailure { Toast.makeText(context, it.message ?: "Load failed", Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Choose Profile Icon", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                backgroundColor = Color.Black,
                contentColor = Color.White,
                elevation = 4.dp
            )
        },
        backgroundColor = Color.Black
    ) { innerPadding ->
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(Color.Black)
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(Color.Black)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(sections) { section ->
                        Column {
                            Text(
                                text = section.title,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(section.icons) { icon ->
                                    Box(
                                        modifier = Modifier
                                            .size(92.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                                            .clickable(enabled = !applying) {
                                                // apply selection
                                                applying = true
                                                //val scope = rememberCoroutineScope()
                                                scope.launch(Dispatchers.IO) {
                                                    Log.d("API", "Sending user_id=$userId, icon_id=${icon.icon_id}, img_url=${icon.img_url}")

                                                    val result = repo.setProfilePicture(userId, icon)
                                                    withContext(Dispatchers.Main) {
                                                        applying = false
                                                        result.onSuccess { newUrl ->
                                                            Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                                                            // pass back to previous screen
                                                            navController.previousBackStackEntry
                                                                ?.savedStateHandle
                                                                ?.set("pp_link_updated", newUrl)
                                                            navController.popBackStack()
                                                        }.onFailure {
                                                            Toast.makeText(context, it.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        AsyncImage(
                                            model = icon.img_url,
                                            contentDescription = icon.title,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // small bottom spacer
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}
