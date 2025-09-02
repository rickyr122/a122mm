package com.example.a122mm.helper

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object InListCache {
    private const val PREFS = "inlist_overrides"
    private val _map = MutableStateFlow<Map<String, String>>(emptyMap())
    val map: StateFlow<Map<String, String>> = _map

    private fun Context.prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun init(context: Context) {
        val m = context.prefs().all
            .mapNotNull { (k, v) -> (v as? String)?.let { k to it } }
            .toMap()
        _map.value = m
    }

    fun set(context: Context, mId: String, inList: String) {
        val newMap = _map.value.toMutableMap().apply { put(mId, inList) }
        _map.value = newMap
        context.prefs().edit().putString(mId, inList).apply()
    }

    fun get(context: Context, mId: String): String? {
        if (_map.value.isEmpty()) init(context)
        return _map.value[mId]
    }
}
