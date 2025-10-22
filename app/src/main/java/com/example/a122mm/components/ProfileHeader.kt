package com.example.a122mm.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.a122mm.R
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.dataclass.AuthNetwork

@Composable
fun ProfileHeader(
    modifier: Modifier = Modifier,
    onDominantColorExtracted: (Color) -> Unit,
    onLogoutClicked: () -> Unit
) {
    // keep HomeScreen in black
    LaunchedEffect(Unit) { onDominantColorExtracted(Color.Black) }

    val ctx = LocalContext.current
    val repo = remember {
        AuthRepository(
            publicApi = AuthNetwork.publicAuthApi,
            authedApi = AuthNetwork.authedAuthApi(ctx),
            store = TokenStore(ctx)
        )
    }

    val configuration = LocalConfiguration.current
    //val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var displayName by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Call the repository
        val r = repo.loadProfilePic()
        Log.d("ProfileHeader", "res=${r.getOrNull()}")   // 👈 put this here

        r.onSuccess { res ->
            avatarUrl = res.pp_link
            displayName = res.username   // <- now provided by API
        }.onFailure {
            avatarUrl = null
            displayName = null
        }

        loading = false
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imageMod = Modifier
            .size(if (isTablet) 128.dp else 90.dp)
            .clip(CircleShape)
            .border(2.dp, Color.White, CircleShape)

        when {
            loading -> {
                // lightweight placeholder
                Box(imageMod.background(Color(0x22FFFFFF)))
            }
            !avatarUrl.isNullOrBlank() -> {
                // Uses Coil; add `io.coil-kt:coil-compose` in dependencies if not present
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile picture",
                    modifier = imageMod
                )
            }
            else -> {
                Image(
                    painter = painterResource(id = R.drawable.spiderman),
                    contentDescription = "Profile picture",
                    modifier = imageMod
                )
            }
        }

        Text(
            text = displayName?.takeIf { it.isNotBlank() } ?: "",
            fontSize = if (isTablet) 28.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
