package com.example.a122mm.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileViewModel2 : ViewModel() {

        fun logout(context: Context) {
            viewModelScope.launch {
                val tokenStore = TokenStore(context)
                tokenStore.clear()  // 🧹 this wipes both access & refresh tokens
            }
        }
    }