package com.example.a122mm.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.dataclass.AuthNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SplashRoute(nav: NavHostController) {
    val context = LocalContext.current

    // Build the repo once here
    val repo = remember {
        AuthRepository(
            publicApi = AuthNetwork.publicAuthApi,
            authedApi = AuthNetwork.authedAuthApi(context),
            store = TokenStore(context)
        )
    }

    LaunchedEffect(Unit) {
        try {
            // 1) Cheap local check: do we have tokens?
            val hasLocal = withContext(Dispatchers.IO) { repo.hasSession() }

            if (!hasLocal) {
                nav.navigate("login") { popUpTo("splash") { inclusive = true } }
                return@LaunchedEffect
            }

            // 2) Server check: is token still valid?
//            val ok = withContext(Dispatchers.IO) { repo.pingAuth() }
            val ok = withContext(Dispatchers.IO) { repo.pingAuthNoRefresh() }

            if (ok) {
                // --- BEGIN: update check ---
                // Build update repo (Retrofit instance to Cloudflare)
                val retrofit = retrofit2.Retrofit.Builder()
                    .baseUrl("https://videos.122movies.my.id/")
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build()
                val updateApi = retrofit.create(com.example.a122mm.update.UpdateApiService::class.java)
                val updateRepo = com.example.a122mm.update.UpdateRepository(updateApi)

                // Read current installed version
                val pkgInfo = withContext(Dispatchers.IO) {
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
                val currentCode = pkgInfo.longVersionCode

                // Fetch remote version.json
                val remote = withContext(Dispatchers.IO) { updateRepo.fetchRemote().getOrNull() }

                if (remote != null && updateRepo.needsUpdate(remote, currentCode)) {
                    val isForced = updateRepo.isForced(remote, currentCode)
                    if (isForced) {
                        // Go to Home and tell it to open Settings and start forced update
                        nav.navigate("home") { popUpTo("splash") { inclusive = true } }
                        nav.getBackStackEntry("home").savedStateHandle["open_settings_drawer"] = true
                        nav.getBackStackEntry("home").savedStateHandle["forced_update_apk_url"] = remote.apkUrl
                        return@LaunchedEffect
                    }
                }
                // --- END: update check ---
                nav.navigate("home") { popUpTo("splash") { inclusive = true } }
            } else {
                // 401 will also be caught by your interceptor (which clears tokens)
                nav.navigate("login") { popUpTo("splash") { inclusive = true } }
            }
        } catch (t: Throwable) {
            // On any unexpected error, play it safe and go to login
            nav.navigate("login") { popUpTo("splash") { inclusive = true } }
        }
    }

    // Optional: minimal UI while we decide
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

