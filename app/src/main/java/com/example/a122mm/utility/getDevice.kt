package com.example.a122mm.utility

import android.app.UiModeManager
import android.content.Context
import android.content.Context.UI_MODE_SERVICE
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings

fun getDeviceId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        ?: "unknown-device"
}

fun getDeviceName(): String {
    val manu = Build.MANUFACTURER?.trim().orEmpty()
    val model = Build.MODEL?.trim().orEmpty()
    return listOf(manu, model).filter { it.isNotBlank() }.joinToString(" ")
}

/** Returns one of: "tv", "tablet", "phone" */
fun getDeviceType(context: Context): String {
    val uiMode = (context.getSystemService(UI_MODE_SERVICE) as UiModeManager)
        .currentModeType
    if (uiMode == Configuration.UI_MODE_TYPE_TELEVISION) return "tv"

    val cfg = context.resources.configuration
    return if (cfg.smallestScreenWidthDp >= 600) "tablet" else "phone"
}
