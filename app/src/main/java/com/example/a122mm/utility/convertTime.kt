package com.example.a122mm.utility

fun formatDurationFromMinutes(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return buildString {
        if (hours > 0) append("${hours}h ")
        append("${remainingMinutes}m")
    }.trim()
}