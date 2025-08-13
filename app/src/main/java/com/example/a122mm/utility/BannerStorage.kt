package com.example.a122mm.utility

// BannerStorage.kt
import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONObject

object BannerStorage {
    private const val PREF_NAME = "banner_cache"
    private const val CACHE_DURATION = 60 * 60 * 1000L // 1 hour in ms

    private fun keyData(type: String) = "banner_data_$type"
    private fun keyColor(type: String) = "dominant_color_$type"
    private fun keyTime(type: String) = "banner_time_$type"

    fun saveBanner(context: Context, type: String, bannerJson: String, color: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(keyData(type), bannerJson)
            .putInt(keyColor(type), color)
            .putLong(keyTime(type), System.currentTimeMillis())
            .apply()
    }

    fun loadBanner(context: Context, type: String): Triple<JSONObject?, Color?, Boolean> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val time = prefs.getLong(keyTime(type), 0L)
        val now = System.currentTimeMillis()
        val expired = (now - time) > CACHE_DURATION

        val jsonString = prefs.getString(keyData(type), null)
        val colorInt = prefs.getInt(keyColor(type), 0)

        val bannerJson = jsonString?.let { JSONObject(it) }
        val color = if (colorInt != 0) Color(colorInt) else null

        return Triple(bannerJson, color, expired)
    }
}

//object BannerStorage {
//    private const val PREF_NAME = "banner_cache"
//    private const val KEY_DATA = "banner_data"
//    private const val KEY_COLOR = "dominant_color"
//    private const val KEY_TIMESTAMP = "banner_time"
//    private const val CACHE_DURATION = 60 * 60 * 1000L // 1 hour in ms
//
//    fun saveBanner(context: Context, type: String, bannerJson: String, color: Int) {
//        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        prefs.edit()
//            .putString("${type}_data", bannerJson)
//            .putInt("${type}_color", color)
//            .putLong("${type}_time", System.currentTimeMillis())
//            .apply()
//    }
////        prefs.edit()
////            .putString(KEY_DATA, bannerJson)
////            .putInt(KEY_COLOR, color)
////            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
////            .apply()
////    }
//
//    fun loadBanner(context: Context, type: String): Triple<JSONObject?, Color?, Boolean> {
//        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        val time = prefs.getLong("${type}_time", 0L)
//        val now = System.currentTimeMillis()
//        val expired = (now - time) > CACHE_DURATION
//
//        val jsonString = prefs.getString("${type}_data", null)
//        val colorInt = prefs.getInt("${type}_color", 0)
//
//        val bannerJson = jsonString?.let { JSONObject(it) }
//        val color = if (colorInt != 0) Color(colorInt) else null
//
//        return Triple(bannerJson, color, expired)
//    }
//
//
////    fun loadBanner(context: Context): Triple<JSONObject?, Color?, Boolean> {
////        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
////        val time = prefs.getLong(KEY_TIMESTAMP, 0L)
////        val now = System.currentTimeMillis()
////        val expired = (now - time) > CACHE_DURATION
////
////        val jsonString = prefs.getString(KEY_DATA, null)
////        val colorInt = prefs.getInt(KEY_COLOR, 0)
////
////        val bannerJson = jsonString?.let { JSONObject(it) }
////        val color = if (colorInt != 0) Color(colorInt) else null
////
////        return Triple(bannerJson, color, expired)
////    }
//}
