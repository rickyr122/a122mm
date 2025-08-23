package com.example.a122mm.dataclass

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var codes by mutableStateOf<List<Int>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    val blocked = setOf(0, 17)
    val allSections: List<ProfileSection> by derivedStateOf {
        val filtered = codes.filterNot { it in blocked }
        listOf(
            ProfileSection.Continue,
            ProfileSection.RecentWatch
        ) + filtered.map { ProfileSection.Category(it) }
    }

    private val _refreshTrigger = MutableStateFlow(0)
    //    val refreshTrigger: StateFlow<Boolean> = _refreshTrigger
    val refreshTrigger: StateFlow<Int> = _refreshTrigger

    fun triggerRefresh() {
//        _refreshTrigger.value = !_refreshTrigger.value // Toggle to trigger observers
        _refreshTrigger.value += 1
    }

    // Add this function to load codes with a type parameter
    fun loadProfileCodes(type: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val apiService = ApiClient.create(ApiHomeCodesService::class.java)
                val response = apiService.getHomeCodes(type)
                codes = response
            } catch (e: Exception) {
                e.printStackTrace()
                codes = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    // Optional: call loadHomeCodes with a default type on init
    init {
        loadProfileCodes("PRO")  // replace "default" with your actual default type if any
    }
}