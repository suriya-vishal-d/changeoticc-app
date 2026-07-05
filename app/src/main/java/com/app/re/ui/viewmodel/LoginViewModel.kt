package com.app.re.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.app.re.util.SecurePrefsManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _openCustomTab = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val openCustomTab: SharedFlow<String> = _openCustomTab.asSharedFlow()

    private var customTabLaunched = false

    fun onLoginClick() {
        if (_uiState.value is LoginUiState.Loading) return

        _uiState.value = LoginUiState.Loading
        customTabLaunched = true
        _openCustomTab.tryEmit(com.app.re.util.Constants.GITHUB_AUTH_URL)
    }

    fun onAuthRedirect(uri: Uri) {
        val username = uri.getQueryParameter("githubUsername")
        val jwt = uri.getQueryParameter("jwt")
        val avatarUrl = uri.getQueryParameter("avatarUrl")

        if (username.isNullOrBlank() || jwt.isNullOrBlank()) {
            _uiState.value = LoginUiState.Error("Authentication failed. Missing credentials.")
            customTabLaunched = false
            return
        }

        SecurePrefsManager.saveJwt(jwt)
        SecurePrefsManager.saveUsername(username)
        SecurePrefsManager.saveAvatarUrl(avatarUrl.orEmpty())

        customTabLaunched = false
        _uiState.value = LoginUiState.Success(token = jwt, username = username)
    }

    fun onAuthCancelledIfStillWaiting() {
        if (_uiState.value is LoginUiState.Loading && customTabLaunched) {
            _uiState.value = LoginUiState.Idle
            customTabLaunched = false
        }
    }

    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }

    fun resetAfterNavigation() {
        if (_uiState.value is LoginUiState.Success) {
            _uiState.value = LoginUiState.Idle
        }
    }
}

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val token: String, val username: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
