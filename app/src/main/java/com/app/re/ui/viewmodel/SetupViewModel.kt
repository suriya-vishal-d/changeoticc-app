package com.app.re.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.re.data.ResumeRepository
import com.app.re.data.model.ParseResponse
import com.app.re.util.AppCache
import com.app.re.util.SecurePrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SetupViewModel(
    private val repository: ResumeRepository = ResumeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SetupUiState>(SetupUiState.Idle)
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    // Holds the full parsed response in memory so Dashboard can use it without re-parsing
    private var _cachedParseResponse: ParseResponse? = null
    val cachedParseResponse: ParseResponse? get() = _cachedParseResponse

    fun findPortfolio(repo: String, filePath: String, branch: String) {
        // --- Input Validation & Sanitization ---
        val trimmedRepo = repo.trim().trimEnd('/', '.')
        val trimmedPath = filePath.trim().trimStart('/')
        val trimmedBranch = branch.trim()

        if (trimmedRepo.isBlank()) {
            _uiState.value = SetupUiState.Error("Repository name cannot be empty.")
            return
        }
        if (trimmedRepo.contains(" ")) {
            _uiState.value = SetupUiState.Error("Repository name cannot contain spaces.")
            return
        }
        if (trimmedBranch.isBlank()) {
            _uiState.value = SetupUiState.Error("Branch name cannot be empty.")
            return
        }
        if (trimmedPath.isBlank()) {
            _uiState.value = SetupUiState.Error("File path cannot be empty.")
            return
        }
        if (!trimmedPath.endsWith(".html")) {
            _uiState.value = SetupUiState.Error("File path must end with .html")
            return
        }

        val owner = SecurePrefsManager.getUsername()
        if (owner.isNullOrBlank()) {
            _uiState.value = SetupUiState.Error("Session expired. Please log in again.")
            return
        }

        _uiState.value = SetupUiState.Loading

        viewModelScope.launch {
            try {
                val response = repository.parseResume(
                    owner = owner,
                    repo = trimmedRepo,
                    filePath = trimmedPath,
                    branch = trimmedBranch
                )

                // Persist setup details so app skips this screen on next launch
                SecurePrefsManager.saveRepoName(trimmedRepo)
                SecurePrefsManager.saveFilePath(trimmedPath)
                SecurePrefsManager.saveBranchName(trimmedBranch)
                SecurePrefsManager.setFirstLaunchDone()

                // Cache in both the local property and the process-level AppCache
                // so EditViewModel can access it without re-calling the API.
                _cachedParseResponse = response
                AppCache.parseResponse = response

                val detectedName = response.resumeData.name?.takeIf { it.isNotBlank() }
                    ?: "Your Portfolio"

                _uiState.value = SetupUiState.Success(detectedName = detectedName)

            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("404") == true ->
                        "File not found. Check your repo name and file path."
                    e.message?.contains("401") == true || e.message?.contains("403") == true ->
                        "Access denied. Your GitHub token may have expired."
                    e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("Failed to connect") == true ->
                        "Cannot reach server. Check your network connection."
                    else -> e.message ?: "Something went wrong. Please try again."
                }
                _uiState.value = SetupUiState.Error(message)
            }
        }
    }

    fun resetError() {
        if (_uiState.value is SetupUiState.Error) {
            _uiState.value = SetupUiState.Idle
        }
    }
}

sealed class SetupUiState {
    data object Idle : SetupUiState()
    data object Loading : SetupUiState()
    data class Success(val detectedName: String) : SetupUiState()
    data class Error(val message: String) : SetupUiState()
}
