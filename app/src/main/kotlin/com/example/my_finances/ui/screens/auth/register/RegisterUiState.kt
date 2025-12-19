package com.example.my_finances.ui.screens.auth.register

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val passwordsMatch: Boolean = true,
    val navigateToHome: Boolean = false
)
