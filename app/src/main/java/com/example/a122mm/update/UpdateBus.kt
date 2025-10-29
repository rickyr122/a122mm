package com.example.a122mm.update

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object UpdateBus {
    private val _progress = MutableStateFlow(-1)   // -1 = idle, 0..100 downloading
    val progress = _progress.asStateFlow()

    fun setProgress(p: Int) { _progress.value = p }
    fun reset() { _progress.value = -1 }
}
