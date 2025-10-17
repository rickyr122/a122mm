package com.example.a122mm.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a122mm.utility.getDeviceId
import com.example.a122mm.utility.getDeviceName
import com.example.a122mm.utility.getDeviceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {
    val ui = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    fun doLogin(email: String, password: String) = viewModelScope.launch {
        ui.value = LoginUiState.Loading
        repo.login(email, password)
            .onSuccess { ui.value = LoginUiState.Success }
            .onFailure { ui.value = LoginUiState.Error(it.message ?: "Login error") }
    }

    fun doLogin(context: Context, email: String, password: String) = viewModelScope.launch {
        ui.value = LoginUiState.Loading
        repo.login(email, password)
            .onSuccess {
                // Fire and forget — don’t block UI
                viewModelScope.launch {
                    val did = getDeviceId(context)
                    val dname = getDeviceName()
                    val dtype = getDeviceType(context) // "phone" / "tablet" / "tv"
                    val clientTime = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                }
                ui.value = LoginUiState.Success
            }
            .onFailure { ui.value = LoginUiState.Error(it.message ?: "Login error") }
    }

}
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val msg: String) : LoginUiState
}

class SignUpViewModel(private val repo: AuthRepository) : ViewModel() {
    val ui = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    fun doSignUp(email: String, name: String, password: String, clientTime: String) = viewModelScope.launch {
        ui.value = SignUpUiState.Loading
        repo.signup(email, name, password, clientTime)
            .onSuccess { ui.value = SignUpUiState.Success }
            .onFailure { ui.value = SignUpUiState.Error(it.message ?: "Sign up error") }
    }
}
sealed interface SignUpUiState {
    data object Idle : SignUpUiState
    data object Loading : SignUpUiState
    data object Success : SignUpUiState
    data class Error(val msg: String) : SignUpUiState
}
