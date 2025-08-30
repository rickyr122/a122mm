// DiscoveryFilters.kt
package com.example.a122mm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class DiscoveryFilterDef(
    val code: String,
    val label: String,
    val emoji: String
)

@Composable
fun DiscoveryFilters(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onSelected: (index: Int, code: String) -> Unit
) {
    val items = remember {
        listOf(
            DiscoveryFilterDef("RECENT",    "Recently added",         "🆕"),
            DiscoveryFilterDef("SHOULD",    "You Should Watch This",  "⭐"),
            DiscoveryFilterDef("TOP10_MOV", "Top 10 Movies",          "🎬"),
            DiscoveryFilterDef("TOP10_TVG", "Top 10 Series",          "📺"),
        )
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val leftInsetPx = with(LocalDensity.current) { 16.dp.roundToPx() } // matches topBar start padding

    // Whenever selection changes, snap the selected pill to the left edge (fully visible)
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(index = selectedIndex, scrollOffset = -leftInsetPx)
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        // Parent topBar already has start=16.dp; only keep trailing padding here
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        itemsIndexed(items) { idx, item ->
            val isSelected = idx == selectedIndex
            val interactionSource = remember { MutableInteractionSource() }

            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .heightIn(min = 36.dp) // slightly smaller height if you like
                    .then(
                        if (!isSelected) Modifier
                            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(22.dp))
                        else Modifier
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onSelected(idx, item.code)
                        scope.launch {
                            listState.animateScrollToItem(idx, -leftInsetPx)
                        }
                    },
                shape = RoundedCornerShape(22.dp),
                color = if (isSelected) Color.White else Color.Black,
                tonalElevation = if (isSelected) 4.dp else 0.dp,
                shadowElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), // ✅ smaller padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp) // smaller circle to match tighter pill
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color(0xFF2A2A2A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.emoji,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.label,
                        color = if (isSelected) Color.Black else Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

        }
    }
}
