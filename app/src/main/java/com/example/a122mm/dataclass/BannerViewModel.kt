package com.example.a122mm.dataclass

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a122mm.components.ApiService
import com.example.a122mm.components.BannerResponse
import com.example.a122mm.utility.BannerStorage
import kotlinx.coroutines.launch


class BannerViewModel(
    private val context: Context
) : ViewModel() {

    var bannerData by mutableStateOf<BannerResponse?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadBanner()
    }

    fun loadBanner(type: String = "HOM") {
        viewModelScope.launch {
            val (cachedJson, cachedColor, expired) = BannerStorage.loadBanner(context, type)

            if (cachedJson != null && !expired) {
                bannerData = BannerResponse(
                    mId = cachedJson.getString("mId"),
                    cvrUrl = cachedJson.getString("cvrUrl"),
                    bdropUrl = cachedJson.getString("bdropUrl"),
                    logoUrl = cachedJson.getString("logoUrl"),
                    inList = if (cachedJson.has("inList")) cachedJson.getString("inList") else "0"
                )
                return@launch
            }

            try {
                val apiService = ApiClient.create(ApiService::class.java)
                bannerData = apiService.getBanner(type)
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.localizedMessage ?: "Unknown error"
            }
        }
    }
}

class BannerViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BannerViewModel(context) as T
    }
}
