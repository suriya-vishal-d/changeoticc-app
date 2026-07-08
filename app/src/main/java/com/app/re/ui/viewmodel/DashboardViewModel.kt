package com.app.re.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.re.data.ResumeRepository
import com.app.re.data.model.RepoStatsResponse
import com.app.re.util.SecurePrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import com.app.re.util.AppCache

data class DashboardInfo(
    val username: String,
    val repoName: String,
    val avatarInitial: Char,
    val portfolioUrl: String
)

class DashboardViewModel(private val repository: ResumeRepository = ResumeRepository()) : ViewModel() {

    private val _info = MutableStateFlow<DashboardInfo?>(null)
    val info: StateFlow<DashboardInfo?> = _info.asStateFlow()

    private val _repoStats = MutableStateFlow<RepoStatsResponse?>(null)
    val repoStats: StateFlow<RepoStatsResponse?> = _repoStats.asStateFlow()

    private val _isParseReady = MutableStateFlow(false)
    val isParseReady: StateFlow<Boolean> = _isParseReady.asStateFlow()

    init {
        loadInfo()
    }

    private fun loadInfo() {
        val username = SecurePrefsManager.getUsername() ?: return
        val repoName = SecurePrefsManager.getRepoName()?.trimEnd('/', '.') ?: return

        _info.value = DashboardInfo(
            username = username,
            repoName = repoName,
            avatarInitial = username.first().uppercaseChar(),
            portfolioUrl = buildPortfolioUrl(username, repoName)
        )

        viewModelScope.launch {
            try {
                _repoStats.value = repository.getRepoStats(repoName)
            } catch (e: Exception) {
                // Ignore stats fetch failure quietly
            }
        }

        // Start background parsing if not already started
        if (AppCache.parseDeferred == null) {
            val filePath = SecurePrefsManager.getFilePath()?.trimStart('/') ?: "index.html"
            val deferred = viewModelScope.async {
                val cached = SecurePrefsManager.getCachedParseResponse()
                var latestSha: String? = null
                
                try {
                    // Check if file has changed on GitHub
                    val fetchRes = repository.fetchResume(username, repoName, filePath)
                    latestSha = fetchRes.sha
                    if (cached != null && cached.sha == latestSha) {
                        return@async cached
                    }
                } catch (e: Exception) {
                    // Offline or repo access error, just fallback to cache if available
                    if (cached != null) return@async cached
                }

                // If no cache or sha mismatch, we must run the AI parser
                val response = repository.parseResume(username, repoName, filePath)
                // Cache it so next restart is fast
                SecurePrefsManager.saveCachedParseResponse(response)
                response
            }
            AppCache.parseDeferred = deferred
        }
        
        // Wait for it to finish so we can update isParseReady
        viewModelScope.launch {
            try {
                AppCache.parseDeferred?.await()
                _isParseReady.value = true
            } catch (e: Exception) {
                // Set to true even on error, so EditViewModel can show the error screen
                _isParseReady.value = true
            }
        }
    }

    /**
     * Builds the GitHub Pages URL.
     *
     * Two cases:
     *  - If repo name is already "username.github.io" → https://username.github.io
     *  - Otherwise (project pages) → https://username.github.io/reponame
     */
    fun buildPortfolioUrl(username: String, repoName: String): String {
        val lowerUser = username.lowercase()
        val r = repoName.trimEnd('/', '.')
        return if (r.lowercase() == "$lowerUser.github.io") {
            "https://$lowerUser.github.io"
        } else {
            "https://$lowerUser.github.io/$r"
        }
    }

    fun logout(onDone: () -> Unit) {
        SecurePrefsManager.clearAll()
        onDone()
    }
}
