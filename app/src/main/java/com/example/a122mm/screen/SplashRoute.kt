package com.example.a122mm.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.dataclass.AuthNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SplashRoute(nav: NavHostController) {
    LaunchedEffect(Unit) {
        val context = nav.context
        val repo = AuthRepository(
            publicApi = AuthNetwork.publicAuthApi,
            authedApi = AuthNetwork.authedAuthApi(context),
            store = TokenStore(context)
        )
        val has = withContext(Dispatchers.IO) { repo.hasSession() }
        if (has) nav.navigate("home") { popUpTo("splash") { inclusive = true } }
        else nav.navigate("login") { popUpTo("splash") { inclusive = true } }
    }
}
