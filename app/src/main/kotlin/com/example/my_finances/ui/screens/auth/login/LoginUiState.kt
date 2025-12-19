package com.example.my_finances.ui.screens.auth.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateToHome: Boolean = false,
    val passwordResetEmailSent: Boolean = false
)
