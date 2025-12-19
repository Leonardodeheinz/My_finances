package com.example.my_finances.ui.screens.profile

data class ProfileUiState(
    val email: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showPasswordChangeDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val navigateToLogin: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = ""
)
