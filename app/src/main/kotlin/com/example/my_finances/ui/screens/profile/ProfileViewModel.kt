package com.example.my_finances.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.repository.FirebaseAuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val user = authDataSource.currentUser
        _uiState.update {
            it.copy(
                email = user?.email ?: "",
                displayName = user?.displayName ?: ""
            )
        }
    }

    fun onSignOutClick() {
        authDataSource.signOut()
        _uiState.update { it.copy(navigateToLogin = true) }
    }

    fun onDeleteAccountClick() {
        _uiState.update { it.copy(showDeleteAccountDialog = true) }
    }

    fun onDismissDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountDialog = false) }
    }

    fun confirmDeleteAccount() {
        viewModelScope.launch {
            authDataSource.deleteAccount().collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                showDeleteAccountDialog = false,
                                errorMessage = null
                            )
                        }
                    }
                    is AuthResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                navigateToLogin = true
                            )
                        }
                    }
                    is AuthResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showDeleteAccountDialog = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onPasswordChangeClick() {
        _uiState.update { it.copy(showPasswordChangeDialog = true) }
    }

    fun onDismissPasswordChangeDialog() {
        _uiState.update {
            it.copy(
                showPasswordChangeDialog = false,
                currentPassword = "",
                newPassword = "",
                confirmNewPassword = ""
            )
        }
    }

    fun onCurrentPasswordChange(password: String) {
        _uiState.update { it.copy(currentPassword = password) }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(newPassword = password) }
    }

    fun onConfirmNewPasswordChange(password: String) {
        _uiState.update { it.copy(confirmNewPassword = password) }
    }

    fun submitPasswordChange() {
        val currentPassword = _uiState.value.currentPassword
        val newPassword = _uiState.value.newPassword
        val confirmNewPassword = _uiState.value.confirmNewPassword

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        if (newPassword != confirmNewPassword) {
            _uiState.update { it.copy(errorMessage = "New passwords do not match") }
            return
        }

        val passwordError = validatePassword(newPassword)
        if (passwordError != null) {
            _uiState.update { it.copy(errorMessage = passwordError) }
            return
        }

        viewModelScope.launch {
            authDataSource.changePassword(currentPassword, newPassword).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                errorMessage = null
                            )
                        }
                    }
                    is AuthResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showPasswordChangeDialog = false,
                                currentPassword = "",
                                newPassword = "",
                                confirmNewPassword = "",
                                successMessage = "Password changed successfully"
                            )
                        }
                    }
                    is AuthResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = mapPasswordChangeError(result.message)
                            )
                        }
                    }
                }
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onSuccessMessageDismissed() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun resetNavigation() {
        _uiState.update { it.copy(navigateToLogin = false) }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.length < 8 -> "Password must be at least 8 characters"
            !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain at least one lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            else -> null
        }
    }

    private fun mapPasswordChangeError(message: String): String {
        return when {
            message.contains("wrong", ignoreCase = true) || message.contains("invalid", ignoreCase = true) ->
                "Current password is incorrect"
            message.contains("weak", ignoreCase = true) ->
                "New password is too weak"
            message.contains("recent", ignoreCase = true) ->
                "Please log in again to change your password"
            else -> message
        }
    }
}
