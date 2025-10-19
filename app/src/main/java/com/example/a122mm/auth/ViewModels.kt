package com.example.a122mm.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {
    val ui = MutableStateFlow<LoginUiState>(LoginUiState.Idle)

    fun doLogin(context: Context, email: String, password: String) = viewModelScope.launch {
        ui.value = LoginUiState.Loading
        repo.login(context, email, password)
            .onSuccess {
                ui.value = LoginUiState.Success
            }
            .onFailure { e ->
                ui.value = LoginUiState.Error(e.message ?: "Login failed")
            }
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

    fun doSignUp(email: String, name: String, password: String) = viewModelScope.launch {
        ui.value = SignUpUiState.Loading
        repo.signup(email, name, password)
            .onSuccess { ui.value = SignUpUiState.Success }
            .onFailure { e -> ui.value = SignUpUiState.Error(e.message ?: "Sign up error") }
    }
}

sealed interface SignUpUiState {
    data object Idle : SignUpUiState
    data object Loading : SignUpUiState
    data object Success : SignUpUiState
    data class Error(val msg: String) : SignUpUiState
}
