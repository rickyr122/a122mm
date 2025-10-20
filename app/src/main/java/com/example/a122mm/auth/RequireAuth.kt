package com.example.a122mm.auth

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.a122mm.dataclass.AuthNetwork.authedAuthApi
import com.example.a122mm.dataclass.AuthNetwork.publicAuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RequireAuth(
    navController: androidx.navigation.NavHostController,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Build repo once
    val repo = remember {
        AuthRepository(
            publicApi = publicAuthApi,
            authedApi = authedAuthApi(context),
            store = TokenStore(context)
        )
    }

    // On enter: check locally, then ping /me.php
    LaunchedEffect(Unit) {
        val hasLocal = withContext(Dispatchers.IO) { repo.hasSession() }
        if (!hasLocal) {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
            return@LaunchedEffect
        }
//        val ok = withContext(Dispatchers.IO) { repo.pingAuth() }
//        if (!ok) {
//            // Interceptor will already broadcast + clear; we still navigate defensively
//            Toast
//                .makeText(context, "Session invalid. Please sign in again.", android.widget.Toast.LENGTH_SHORT)
//                .show()
//            navController.navigate("login") { popUpTo(0) { inclusive = true } }
//        }

        val ok = withContext(Dispatchers.IO) { repo.pingAuthNoRefresh() }
        if (!ok) {
            Toast.makeText(context, "Session invalid. Please sign in again.", android.widget.Toast.LENGTH_SHORT).show()
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }

    }

    // While the check happens, you can render a cheap placeholder, but
    // in practice this is very fast; or just render content — player will
    // be immediately replaced if unauthorized.
    content()
}
