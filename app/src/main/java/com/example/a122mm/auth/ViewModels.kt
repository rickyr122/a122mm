package com.example.a122mm.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {
    val ui = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    fun doLogin(email: String, password: String) = viewModelScope.launch {
        ui.value = LoginUiState.Loading
        repo.login(email, password)
            .onSuccess { ui.value = LoginUiState.Success }
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
    fun doSignUp(email: String, name: String, password: String) = viewModelScope.launch {
        ui.value = SignUpUiState.Loading
        repo.signup(email, name, password)
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
