    package com.example.a122mm

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import com.example.a122mm.auth.LogoutReason
import com.example.a122mm.auth.SessionManager.logoutFlow
import com.example.a122mm.ui.theme.A122mmTheme

    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            // Use splash theme (must be set in manifest) so the FIRST FRAME is black
            installSplashScreen()

            super.onCreate(savedInstanceState)

            // 1) Force black before Compose
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK))
            window.statusBarColor = android.graphics.Color.BLACK
            window.navigationBarColor = android.graphics.Color.BLACK

            // Preempt any pre-Compose tint to kill purple flash
            window.statusBarColor = android.graphics.Color.BLACK
            window.navigationBarColor = android.graphics.Color.BLACK

            // You already use edge-to-edge; keep it
            setDecorFitsSystemWindows(window, false)

            setContent {
                val dark = isSystemInDarkTheme()
                val navController = androidx.navigation.compose.rememberNavController()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black)
                ) {

                    val context = LocalContext.current

                    LaunchedEffect(Unit) {
                        logoutFlow.collect { reason ->
                            val msg = when (reason) {
                                LogoutReason.TOKEN_EXPIRED ->
                                    "Your session expired. Please sign in again."
                                LogoutReason.REMOTE_LOGOUT ->
                                    "You were logged out from another device."
                                else -> "You’ve been signed out."
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
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