package com.example.a122mm.helper

import android.app.Activity
import android.content.Context

fun Context.setScreenOrientation(orientation: Int) {
    val activity = this as? Activity
    activity?.requestedOrientation = orientation
}