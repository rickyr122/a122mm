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

class SeriesViewModel : ViewModel() {
    var codes by mutableStateOf<List<Int>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    val allSections: List<Section> by derivedStateOf {
        val filtered = codes.filter { it != 0 }
        (listOf(Section.Continue) + filtered.map { Section.Category(it) }).shuffled()
    }

//    init {
//        viewModelScope.launch {
//            try {
//                val apiService = ApiClient.create(ApiHomeCodesService::class.java)
//                val response = apiService.getHomeCodes()
//                codes = response
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                isLoading = false
//            }
//        }
//    }

    private val _refreshTrigger = MutableStateFlow(0)
    //    val refreshTrigger: StateFlow<Boolean> = _refreshTrigger
    val refreshTrigger: StateFlow<Int> = _refreshTrigger

    fun triggerRefresh() {
//        _refreshTrigger.value = !_refreshTrigger.value // Toggle to trigger observers
        _refreshTrigger.value += 1
    }

    // Add this function to load codes with a type parameter
    fun loadSeriesCodes(type: String) {
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
        loadSeriesCodes("TVS")  // replace "default" with your actual default type if any
    }
}