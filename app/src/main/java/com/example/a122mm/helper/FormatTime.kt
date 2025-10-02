package com.example.a122mm.helper

fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds) // no leading zero on hours
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}