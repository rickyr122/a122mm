package com.example.a122mm.helper

import android.content.Context
import com.example.a122mm.utility.BannerStorage.loadBanner
import com.example.a122mm.utility.BannerStorage.saveBanner

fun updateInList(mId: String, inList: String, context: Context) {
    val types = listOf("HOM", "MOV", "TVG")

    for (type in types) {
        val (cachedJson, cachedColor, expired) = loadBanner(context, type)
        if (cachedJson != null && cachedJson.optString("mId") == mId) {
            cachedJson.put("inList", inList)
            saveBanner(
                context,
                type,
                cachedJson.toString(),
                cachedColor?.value?.toInt() ?: 0
            )
        }
    }
}