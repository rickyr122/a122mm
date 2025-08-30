package com.example.a122mm.pages

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter

private const val TMDB_IMG = "https://image.tmdb.org/t/p/w500"

data class HighlightItem(
    val title: String,
    val posterPath: String
)

@Composable
fun HighlightsPage(
    modifier: Modifier = Modifier,
    activeCode: String = "RECENT"
) {
    val items = when (activeCode) {
        "RECENT" -> listOf(
            HighlightItem("Dune: Part Two", "/8bcoRX3hQRHufLPSDREdvr3YMXx.jpg"),
            HighlightItem("Furiosa: A Mad Max Saga", "/iADOJ8Zymht2JPMoy3R7xceZprc.jpg"),
            HighlightItem("Inside Out 2", "/gMB8vgHu2B6SxjZM3V5hFv1cHZo.jpg"),
        )

        "SHOULD" -> listOf(
            HighlightItem("The Shawshank Redemption", "/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg"),
            HighlightItem("Interstellar", "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg"),
            HighlightItem("Parasite", "/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg"),
        )

        "TOP10_MOV" -> listOf(
            HighlightItem("The Dark Knight", "/qJ2tW6WMUDux911r6m7haRef0WH.jpg"),
            HighlightItem("Inception", "/kqjL17yufvn9OVLyXYpvtyrFfak.jpg"),
            HighlightItem("Avengers: Endgame", "/or06FN3Dka5tukK1e9sl16pB3iy.jpg"),
        )

        "TOP10_TVG" -> listOf(
            HighlightItem("Breaking Bad", "/ggFHVNu6YYI5L9pCfOacjizRGt.jpg"),
            HighlightItem("Game of Thrones", "/u3bZgnGQ9T01sWNhyveQz0wH0Hl.jpg"),
            HighlightItem("The Last of Us", "/uKvVjHNqB5VmOrdxqAt2F7J78ED.jpg"),
        )

        else -> emptyList()
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    val contentModifier = if (isTablet && isLandscape) {
        Modifier
            .fillMaxWidth(0.7f)
        //.align(Alignment.TopCenter)
    } else {
        Modifier
            .fillMaxWidth()
        //.align(Alignment.TopCenter)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = contentModifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items.forEach { item ->
                HighlightCard(item)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HighlightCard(item: HighlightItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = "$TMDB_IMG${item.posterPath}",
                contentDescription = item.title,
                modifier = Modifier
                    .aspectRatio(16f / 9f)
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Error) {
                        // TODO: optional placeholder UI
                    }
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
