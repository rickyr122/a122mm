package com.example.a122mm.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.a122mm.utility.formatDurationFromMinutes

@Composable
fun TvEpisodes(
    modifier: Modifier,
    episodes: List<EpisodeData>,
    seasons: List<String>,
    selectedSeasonIndex: Int,
    onSeasonSelected: (Int) -> Unit,
    gName: String,
    activeEpisodeIndex: Int,
    activeSeason: Int,
    navController: NavController
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp)) {
        // Top Row: Season selector or Group name + Info icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (seasons.size > 1) {
                // LEFT: Floating Season Selector Trigger
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF262626))
                            .clickable { expanded = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = seasons[selectedSeasonIndex],
                            color = Color.White,
                            fontSize = if (isTablet) 16.sp else 14.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Season",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Dialog (Floating Overlay) for Season Selection
                    if (expanded) {
                        Dialog(onDismissRequest = { expanded = false }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val scrollState = rememberScrollState()
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(1f)
                                        .verticalScroll(scrollState)
                                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    seasons.forEachIndexed { index, season ->
                                        val isSelected = index == selectedSeasonIndex

                                        val fontSize = when {
                                            isSelected && isTablet -> 28.sp // selected in tab
                                            isSelected && !isTablet -> 24.sp // selected in phone
                                            !isSelected && isTablet -> 26.sp // not selected in tab
                                            else -> 22.sp                   // selected in phone
                                        }

                                        Text(
                                            text = season,
                                            color = Color.White,
                                            fontSize = fontSize,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onSeasonSelected(index)
                                                    expanded = false
                                                }
                                                .padding(vertical = 12.dp, horizontal = 24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White)
                                            .clickable { expanded = false },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Close",
                                            tint = Color.Black,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
            } else {
                // LEFT: Show group name if only 1 season
                Text(
                    text = gName.replace("`", "'"),
                    color = Color.White,
                    fontSize = if (isTablet) 18.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis, // Truncate long text
                    modifier = Modifier
                        .weight(1f) // Let it take all available horizontal space
                        .padding(end = 8.dp) // Give some space before Info icon
                )
            }

            // RIGHT: Info icon
            val iconSz = if (isTablet) 32.dp else 28.dp
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color.White,
                modifier = Modifier
                    .size(iconSz)
                    .clickable {
                        // TODO: Show info dialog
                    }
            )
        }


        // Episodes List
        episodes.forEachIndexed { index, episode ->

            val isActive = selectedSeasonIndex == (activeSeason - 1) && index == activeEpisodeIndex

            //Log.d("EP_CHECK", "selectedSeasonIndex=$selectedSeasonIndex, activeSeason=$activeSeason, index=$index, activeEpisodeIndex=$activeEpisodeIndex")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) Color.DarkGray else Color.Transparent)
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (isTablet) 0.4f else 0.5f)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp)
                    ) {
                        AsyncImage(
                            model = episode.tvCvrUrl,
                            contentDescription = episode.tvId,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.DarkGray)
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(38.dp)
                                .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(50))
                                .border(2.dp, Color.White, shape = RoundedCornerShape(50))
                                .clickable{
                                    navController.navigate(
                                        "playmovie/${episode.tvId}"
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(30.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp).height(32.dp))

                    Column(modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)) {
                        Text(
                            text = episode.tvTitle.replace("`", "'"),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (isTablet) 18.sp else 14.sp
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = formatDurationFromMinutes(episode.tvDuration),
                            color = Color.Gray,
                            fontSize = if (isTablet) 14.sp else 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = episode.tvDescription.replace("`", "'"),
                    color = Color.LightGray,
                    fontSize = if (isTablet) 15.sp else 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}

data class EpisodeData(
    val tvId: String,
    val tvTitle: String,
    val tvDuration: Int,
    val tvCvrUrl: String,
    val tvDescription: String
)
