package com.example.a122mm.helper

// TimeClient.kt
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun nowClientTimeString(): String =
    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

fun tzOffsetMinutesNow(): Int {
    val zone = ZoneId.systemDefault()
    val offset = zone.rules.getOffset(Instant.now())
    return offset.totalSeconds / 60
}
