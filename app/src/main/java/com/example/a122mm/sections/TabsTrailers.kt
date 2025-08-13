package com.example.a122mm.sections

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.a122mm.helper.getDriveUrl

data class TrailerItem(
    val mId: String,
    val tTitle: String,
    val tVid: String,
    val tSrt: String,
    val tThumb: String
)

@Composable
fun TabTrailer(trailer: TrailerItem, navController: NavController) {
    Spacer(Modifier.height(8.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        val isImageLoaded = remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .clickable {
                    val encodedTitle = Uri.encode(trailer.tTitle)
                    navController.navigate("playvideo/${trailer.tVid}/${trailer.tSrt}/$encodedTitle")
                }
        ) {
            AsyncImage(
                model = getDriveUrl(trailer.tThumb),
                contentDescription = trailer.tTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                onSuccess = {
                    isImageLoaded.value = true
                },
                onLoading = {
                    isImageLoaded.value = false
                },
                onError = {
                    isImageLoaded.value = true // fallback: still allow click
                }
            )

            if (!isImageLoaded.value) {
                // 🔄 Circular Loading Spinner
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                // ▶️ Play Icon
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(50))
                        .border(2.dp, Color.White, shape = RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = trailer.tTitle,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
    Spacer(Modifier.height(8.dp))
}
