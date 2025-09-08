package com.example.a122mm.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class RecItem(
    val id: String,
    val title: String,
    val posterUrl: String,
    val badge: String? = null
)

/**
 * Content-only Search page.
 * - By default, it renders WITHOUT its own header, so HomeScreen can own the Back + Search bar.
 * - If you ever want to use it standalone, set showHeader = true.
 */
@Composable
fun SearchPage(
    modifier: Modifier = Modifier,
    showHeader: Boolean = false,
    query: String = "",
    onQueryChange: (String) -> Unit = {}
) {
    val items = remember {
        listOf(
            RecItem("1","KPop Demon Hunters","https://image.tmdb.org/t/p/w500/9Gtg2DzBhmYamXBS1hKAhiwbBKS.jpg","TOP 10"),
            RecItem("2","Go Ahead","https://image.tmdb.org/t/p/w500/t/p2.jpg"),
            RecItem("3","Komang","https://image.tmdb.org/t/p/w500/t/p3.jpg"),
            RecItem("4","Bon Appétit, Your Majesty","https://image.tmdb.org/t/p/w500/t/p4.jpg","New Episode"),
            RecItem("5","Wednesday","https://image.tmdb.org/t/p/w500/t/p5.jpg","New Season"),
            RecItem("6","Wednesday","https://image.tmdb.org/t/p/w500/t/p5.jpg","New Season"),
            RecItem("7","KPop Demon Hunters","https://image.tmdb.org/t/p/w500/9Gtg2DzBhmYamXBS1hKAhiwbBKS.jpg","TOP 10")
        )
    }

    Column(
        modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (showHeader) {
            // Back row (with horizontal padding)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp)
                        .clickable { /* back */ }
                )
            }

            // Edge-to-edge grey search bar (real TextField)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF1A1A1A)) // grey strip across full width
                    .padding(horizontal = 16.dp, vertical = 8.dp) // inner padding for the field
            ) {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = {
                        Text(
                            text = "Search games, shows, movies…",
                            color = Color(0xFF8C8C8C),
                            fontSize = 16.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFF8C8C8C))
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A1A1A),
                        unfocusedContainerColor = Color(0xFF1A1A1A),
                        disabledContainerColor = Color(0xFF1A1A1A),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            Spacer(Modifier.height(24.dp))
        } else {
            // When HomeScreen owns the header, you can choose to keep or remove this spacer.
            Spacer(Modifier.height(24.dp))
        }

        // Content with normal margins
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                "Recommended TV Shows & Movies",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Spacer(Modifier.height(12.dp))

            items.forEachIndexed { index, item ->
                SearchRow(item = item, onPlay = { /* play item */ })
                if (index != items.lastIndex) Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun SearchRow(
    item: RecItem,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF222222))
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (!item.badge.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE50914)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item.badge,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        // Wrapped title (up to 2 lines)
        Text(
            item.title,
            color = Color.White,
            fontSize = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(10.dp))

        // Play button
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(1.dp, Color(0xFFB3B3B3), CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF141414))
                .clickable { onPlay() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.PlayArrow, contentDescription = "Play", tint = Color.White)
        }
    }
}
