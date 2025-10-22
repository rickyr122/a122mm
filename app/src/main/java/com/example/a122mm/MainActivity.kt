    package com.example.a122mm

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.example.a122mm.auth.LogoutReason
import com.example.a122mm.auth.SessionManager.logoutFlow
import com.example.a122mm.ui.theme.A122mmTheme
import kotlinx.coroutines.delay

    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            // Use splash theme (must be set in manifest) so the FIRST FRAME is black
            installSplashScreen()

            super.onCreate(savedInstanceState)

            // 1) Force black before Compose
            window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))
            window.statusBarColor = android.graphics.Color.BLACK
            window.navigationBarColor = android.graphics.Color.BLACK

            // Preempt any pre-Compose tint to kill purple flash
            window.statusBarColor = android.graphics.Color.BLACK
            window.navigationBarColor = android.graphics.Color.BLACK

            // You already use edge-to-edge; keep it
            setDecorFitsSystemWindows(window, false)

            setContent {
                val dark = isSystemInDarkTheme()
                val navController = rememberNavController()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {

                    val context = LocalContext.current
                    val appContext = context.applicationContext  // <-- keep alive
                    val lifecycleOwner = LocalLifecycleOwner.current

                    val lastShownReason = remember { mutableStateOf<LogoutReason?>(null) }
                    val lastToastTime = remember { mutableStateOf(0L) }

                    LaunchedEffect(lifecycleOwner) {
                        logoutFlow.collect { reason ->
                            // Prevent duplicate messages within 2 seconds or same reason twice
                            val now = System.currentTimeMillis()
                            if (reason == lastShownReason.value && now - lastToastTime.value < 2000) {
                                return@collect
                            }

                            val msg = when (reason) {
                                LogoutReason.TOKEN_EXPIRED ->
                                    "Your session expired. Please sign in again."
                                LogoutReason.REMOTE_LOGOUT ->
                                    "Your session expired. Please sign in again."
                                    //"You were logged out from another device."
                                LogoutReason.MANUAL_LOGOUT ->
                                    "You’ve signed out successfully."
                                else ->
                                    "You’ve been signed out."
                            }

//                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            Toast.makeText(appContext, msg, Toast.LENGTH_LONG).show()
                            delay(350)

                            lastShownReason.value = reason
                            lastToastTime.value = now

                            // Only navigate if we’re not already on login
                            val current = navController.currentDestination?.route
                            if (current != "login") {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }


                    A122mmTheme {
                        // Keep your Scaffold / Nav
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            // Sync bars to theme right after composition starts
                            SideEffect {
                                // keep bars black; never flip to white
                                window.statusBarColor = Color.Black.toArgb()
                                window.navigationBarColor = Color.Black.toArgb()

                                val ctl = WindowCompat.getInsetsController(window, window.decorView)
                                // true = dark icons (for light bars), false = light icons
                                ctl.isAppearanceLightStatusBars = !dark
                                if (Build.VERSION.SDK_INT >= 26) {
                                    ctl.isAppearanceLightNavigationBars = !dark
                                }
                            }

                            AppNavigation(
                                navController = navController,
                                Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }