package com.example.my_finances.ui.screens.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.my_finances.ui.components.AuthTextField
import com.example.my_finances.ui.components.LoadingDialog
import com.example.my_finances.ui.components.PasswordTextField
import com.example.my_finances.ui.components.PrimaryButton

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            onNavigateToHome()
            viewModel.resetNavigation()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    if (uiState.isLoading) {
        LoadingDialog()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign up to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthTextField(
            value = uiState.displayName,
            onValueChange = viewModel::onDisplayNameChange,
            label = "Display Name (Optional)",
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
            keyboardType = KeyboardType.Email,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = "Confirm Password",
            isError = !uiState.passwordsMatch && uiState.confirmPassword.isNotEmpty(),
            errorMessage = if (!uiState.passwordsMatch && uiState.confirmPassword.isNotEmpty()) "Passwords do not match" else null,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Password must be at least 8 characters with uppercase, lowercase, and a number",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "Register",
            onClick = viewModel::onRegisterClick,
            enabled = !uiState.isLoading && uiState.passwordsMatch
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToLogin,
            enabled = !uiState.isLoading
        ) {
            Text("Already have an account? Login")
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    ) { data ->
        Snackbar(snackbarData = data)
    }
}
