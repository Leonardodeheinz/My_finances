package com.example.my_finances.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.my_finances.data.repository.FirebaseAuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val user = authDataSource.currentUser
        val displayName = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"
        _uiState.update { it.copy(userName = displayName) }
    }
}
