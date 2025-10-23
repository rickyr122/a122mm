package com.example.a122mm.screen

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var signOutAll by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    var showCur by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConf by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }


    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    LaunchedEffect(Unit) { visible = true }

    BackHandler {
        scope.launch {
            visible = false
            delay(180)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            // 🔽 lower top bar with extra padding
            Column(modifier = Modifier.padding(top = 8.dp)) {
                TopAppBar(
                    title = {
                        Text(
                            "Change Password",
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp) // move text slightly down
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    visible = false
                                    delay(180)
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp) // move arrow right & lower
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            }
        },
        containerColor = Color.Black
    ) { inner ->
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = Color(0xFFF5F5F5),
                    tonalElevation = 2.dp,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top, //Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Protect your account with a unique password at least 6 characters long.",
                                color = Color(0xFF222222),
                                fontSize = if (isTablet) 18.sp else 16.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Spacer(Modifier.height(16.dp))

                            // ---------- Current Password
                            OutlinedTextField(
                                value = current,
                                onValueChange = { current = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp),
                                placeholder = { Text("Current Password", color = Color.DarkGray) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                visualTransformation = if (showCur) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showCur = !showCur }) {
                                        Icon(
                                            imageVector = if (showCur) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (showCur) "Hide password" else "Show password",
                                            tint = Color.Black
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black,
                                    focusedBorderColor = Color.Black,
                                    unfocusedBorderColor = Color(0xFFBDBDBD)
                                )
                            )

                            Spacer(Modifier.height(20.dp))

                            // ---------- New Password
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp),
                                placeholder = { Text("New password (6–60 characters)", color = Color.DarkGray) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showNew = !showNew }) {
                                        Icon(
                                            imageVector = if (showNew) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (showNew) "Hide password" else "Show password",
                                            tint = Color.Black
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black,
                                    focusedBorderColor = Color.Black,
                                    unfocusedBorderColor = Color(0xFFBDBDBD)
                                )
                            )

                            // ---------- Confirm Password
                            OutlinedTextField(
                                value = confirm,
                                onValueChange = { confirm = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                placeholder = { Text("Re-enter new password", color = Color.DarkGray) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                visualTransformation = if (showConf) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showConf = !showConf }) {
                                        Icon(
                                            imageVector = if (showConf) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (showConf) "Hide password" else "Show password",
                                            tint = Color.Black
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black,
                                    focusedBorderColor = Color.Black,
                                    unfocusedBorderColor = Color(0xFFBDBDBD)
                                )
                            )

                            Spacer(Modifier.height(12.dp))

                            // ---------- Checkbox (aligned left + black fill)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 0.dp, top = 4.dp)
                            ) {
                                // Turn off the 48dp minimum touch target so the visual box can sit flush
                                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                    Checkbox(
                                        checked = signOutAll,
                                        onCheckedChange = { signOutAll = it },
                                        modifier = Modifier.size(20.dp),    // visual box size (tight)
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color.Black,                 // black fill
                                            uncheckedColor = Color.Black.copy(alpha = 0.55f),
                                            checkmarkColor = Color.White
                                        )
                                    )
                                }

                                // Tune this gap to taste; 12.dp usually looks right next to text fields
                                Spacer(Modifier.width(12.dp))

                                Text(
                                    "Sign out all devices",
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Column {
                            Button(
                                onClick = {
                                    if (saving) return@Button
                                    when {
                                        current.isBlank() -> Toast.makeText(ctx, "Enter your current password", Toast.LENGTH_SHORT).show()
                                        newPass.length < 6 -> Toast.makeText(ctx, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                        newPass.length > 60 -> Toast.makeText(ctx, "New password must be <= 60 characters", Toast.LENGTH_SHORT).show()
                                        newPass != confirm -> Toast.makeText(ctx, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                        else -> {
                                            saving = true
                                            scope.launch {
                                                Toast.makeText(ctx, "Saved (stub)", Toast.LENGTH_SHORT).show()
                                                saving = false
                                                visible = false
                                                delay(180)
                                                navController.popBackStack()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(3.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                enabled = !saving
                            ) {
                                Text(if (saving) "Saving..." else "Save", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(Modifier.height(10.dp))

                            TextButton(
                                onClick = {
                                    scope.launch {
                                        visible = false
                                        delay(180)
                                        navController.popBackStack()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel", color = Color.Black, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}