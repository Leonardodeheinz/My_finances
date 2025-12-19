package com.example.my_finances.ui.screens.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.repository.FirebaseAuthDataSource
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
        checkPasswordsMatch()
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
        checkPasswordsMatch()
    }

    fun onDisplayNameChange(displayName: String) {
        _uiState.update { it.copy(displayName = displayName) }
    }

    private fun checkPasswordsMatch() {
        val passwordsMatch = _uiState.value.password == _uiState.value.confirmPassword ||
                _uiState.value.confirmPassword.isEmpty()
        _uiState.update { it.copy(passwordsMatch = passwordsMatch) }
    }

    fun onRegisterClick() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        val displayName = _uiState.value.displayName.trim()

        // Validation
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all required fields") }
            return
        }

        if (!isValidEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }

        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        val passwordError = validatePassword(password)
        if (passwordError != null) {
            _uiState.update { it.copy(errorMessage = passwordError) }
            return
        }

        viewModelScope.launch {
            authDataSource.signUpWithEmail(email, password).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is AuthResult.Success -> {
                        // Update display name if provided
                        if (displayName.isNotEmpty()) {
                            try {
                                result.data.updateProfile(
                                    UserProfileChangeRequest.Builder()
                                        .setDisplayName(displayName)
                                        .build()
                                ).await()
                            } catch (e: Exception) {
                                // Continue even if display name update fails
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                navigateToHome = true
                            )
                        }
                    }
                    is AuthResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = mapFirebaseError(result.message)
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

    fun resetNavigation() {
        _uiState.update { it.copy(navigateToHome = false) }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
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

    private fun mapFirebaseError(message: String): String {
        return when {
            message.contains("email", ignoreCase = true) && message.contains("already", ignoreCase = true) ->
                "This email is already registered. Try logging in instead"
            message.contains("password", ignoreCase = true) && message.contains("weak", ignoreCase = true) ->
                "Password is too weak. Please use at least 8 characters"
            message.contains("invalid", ignoreCase = true) && message.contains("email", ignoreCase = true) ->
                "Please enter a valid email address"
            message.contains("network", ignoreCase = true) ->
                "Network error. Please check your connection"
            else -> message
        }
    }
}
