    package com.example.a122mm

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
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
            WindowCompat.setDecorFitsSystemWindows(window, false)

            setContent {
                val dark = isSystemInDarkTheme()

                Box(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black)
                ) {
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

                            AppNavigation(Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }