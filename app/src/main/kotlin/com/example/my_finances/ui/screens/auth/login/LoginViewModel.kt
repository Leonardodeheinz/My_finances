package com.example.my_finances.ui.screens.auth.login

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
class LoginViewModel @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onLoginClick() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        if (!isValidEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }

        viewModelScope.launch {
            authDataSource.signInWithEmail(email, password).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is AuthResult.Success -> {
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

    fun onForgotPasswordClick() {
        val email = _uiState.value.email.trim()

        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address") }
            return
        }

        if (!isValidEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }

        viewModelScope.launch {
            authDataSource.sendPasswordResetEmail(email).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is AuthResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                passwordResetEmailSent = true
                            )
                        }
                    }
                    is AuthResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
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

    fun onPasswordResetDialogDismissed() {
        _uiState.update { it.copy(passwordResetEmailSent = false) }
    }

    fun resetNavigation() {
        _uiState.update { it.copy(navigateToHome = false) }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun mapFirebaseError(message: String): String {
        return when {
            message.contains("password", ignoreCase = true) && message.contains("wrong", ignoreCase = true) ->
                "Invalid email or password"
            message.contains("user", ignoreCase = true) && message.contains("not found", ignoreCase = true) ->
                "No account found with this email"
            message.contains("too many", ignoreCase = true) && message.contains("request", ignoreCase = true) ->
                "Too many failed attempts. Please try again later"
            message.contains("network", ignoreCase = true) ->
                "Network error. Please check your connection"
            message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ->
                "Invalid email or password"
            else -> message
        }
    }
}
