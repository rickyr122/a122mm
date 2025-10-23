package com.example.a122mm.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.a122mm.R
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.dataclass.AuthNetwork
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(navController: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Build repo once (same pattern as your other screens)
    val repo = remember {
        AuthRepository(
            publicApi = AuthNetwork.publicAuthApi,
            authedApi = AuthNetwork.authedAuthApi(ctx),
            store = TokenStore(ctx)
        )
    }

    // UI state
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var email     by remember { mutableStateOf<String?>(null) }
    var username  by remember { mutableStateOf("") }
    var saving    by remember { mutableStateOf(false) }

    // Load current data
    LaunchedEffect(Unit) {
        // Profile picture (+ optional username if your endpoint returns it)
        repo.loadProfilePic().onSuccess { res ->
            avatarUrl = res.pp_link
            if (!res.username.isNullOrBlank()) username = res.username!!
        }

        // Canonical profile (email + username fallback)
        repo.profile().onSuccess { p ->
            email = p.email
            if (username.isBlank() && !p.username.isNullOrBlank()) {
                username = p.username!!
            }
        }
    }

    // If you want system back to return to Settings (drawer) instead of Home:
    // System back
    BackHandler {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set("open_settings_drawer", true)
        navController.popBackStack() // go back to Home
    }

    val contentPadding = 16.dp
    val sectionSpacing = 14.dp
    val cardSpacing    = 12.dp
    val maxContentWidth = 560.dp  // keeps content readable on tablets

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                TopAppBar(
                    title = {
                        Text(
                            "Account Settings",
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp) // move text slightly down
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("open_settings_drawer", true)
                                navController.popBackStack()
                            },
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp) // move arrow right & lower
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black
                    )
                )
            }
        },
        containerColor = Color.Black
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp) // keep side breathing room
            ) {
                // ↓↓↓ Reduced top space from header to card ↓↓↓
                Spacer(Modifier.height(2.dp))

                Surface(
                    color = Color(0xFFF5F5F5),
                    //shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight() // 👈 now fills remaining height
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween // distributes top content + Save button neatly
                    ) {
                        Column {
                            // Header Row: Avatar + "Change Profile Picture"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Toast.makeText(ctx, "Change Profile Picture", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                val imgMod = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape)

                                if (!avatarUrl.isNullOrBlank()) {
                                    AsyncImage(model = avatarUrl, contentDescription = "Avatar", modifier = imgMod)
                                } else {
                                    Image(
                                        painterResource(R.drawable.pp_default),
                                        contentDescription = "Avatar",
                                        modifier = imgMod
                                    )
                                }

                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Change Profile Picture",
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF7A7A7A))
                            }

                            Spacer(Modifier.height(16.dp))
                            Divider(color = Color(0x1A000000))
                            Spacer(Modifier.height(12.dp))

                            // Email
                            Text(
                                text = email ?: "Your registered email here",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(14.dp))

                            // Username
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Username", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black,
                                    focusedBorderColor = Color.Black,
                                    unfocusedBorderColor = Color(0xFFBDBDBD)
                                )
                            )

                            Spacer(Modifier.height(18.dp))
                            Divider(color = Color(0x1A000000))

                            Spacer(Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        //Toast.makeText(ctx, "Change password", Toast.LENGTH_SHORT).show()
                                        navController.navigate("change_password")

                                    }
                            ) {
                                Text(
                                    "Change password",
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF7A7A7A))
                            }
                        }

                        // Save button pinned bottom
                        Button(
                            onClick = {
                                if (saving) return@Button
                                saving = true
                                scope.launch {
                                    Toast.makeText(ctx, "Saved (stub)", Toast.LENGTH_SHORT).show()
                                    saving = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            enabled = !saving
                        ) {
                            Text(
                                if (saving) "Saving..." else "Save",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
