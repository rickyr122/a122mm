package com.example.a122mm.helper

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomSlider(
    progress: Float,
    onSeekChanged: (Float) -> Unit,
    onSeekStart: () -> Unit,
    onSeekFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    val noRippleSource = remember { MutableInteractionSource() }

    Slider(
        value = progress.coerceIn(0f, 1f),
        onValueChange = {
            if (!isDragging) {
                isDragging = true
                onSeekStart()
            }
            onSeekChanged(it)
        },
        onValueChangeFinished = {
            isDragging = false
            onSeekFinished()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        colors = SliderDefaults.colors(
            thumbColor = Color.Red,
            activeTrackColor = Color.Red,
            inactiveTrackColor = Color.Gray
        )
    )
}