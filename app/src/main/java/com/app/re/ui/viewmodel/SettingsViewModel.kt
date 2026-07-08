package com.app.re.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.app.re.util.AppCache
import com.app.re.util.SecurePrefsManager
import com.app.re.util.ThemeManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsData(
    val username: String,
    val repoName: String,
    val branchName: String,
    val filePath: String,
    val portfolioUrl: String,
    val githubRepoUrl: String,
    val themeMode: Int
)

class SettingsViewModel : ViewModel() {

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    /** One-shot event: emitted when settings are saved, to show a snackbar */
    private val _savedEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val savedEvent: SharedFlow<String> = _savedEvent.asSharedFlow()

    /** One-shot event: emitted after logout to trigger navigation */
    private val _logoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    /** One-shot event: emitted after re-scan to show confirmation snackbar */
    private val _rescanEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val rescanEvent: SharedFlow<String> = _rescanEvent.asSharedFlow()

    // ── Repo name update ──────────────────────────────────────────────────────

    fun updateRepoName(newRepo: String): String? {
        val trimmed = newRepo.trim()
        if (trimmed.isBlank()) return "Repository name cannot be empty."
        if (trimmed.contains(" ")) return "Repository name cannot contain spaces."

        SecurePrefsManager.saveRepoName(trimmed)
        _settings.value = loadSettings()
        _savedEvent.tryEmit("Settings saved. Changes will apply next time you edit your portfolio.")
        return null // null = no error
    }

    // ── Branch name update ───────────────────────────────────────────────────

    fun updateBranchName(newBranch: String): String? {
        val trimmed = newBranch.trim()
        if (trimmed.isBlank()) return "Branch name cannot be empty."

        SecurePrefsManager.saveBranchName(trimmed)
        _settings.value = loadSettings()
        _savedEvent.tryEmit("Settings saved. Changes will apply next time you edit your portfolio.")
        return null
    }

    // ── File path update ──────────────────────────────────────────────────────

    fun updateFilePath(newPath: String): String? {
        val trimmed = newPath.trim()
        if (trimmed.isBlank()) return "File path cannot be empty."
        if (!trimmed.endsWith(".html")) return "File path must end with .html"

        SecurePrefsManager.saveFilePath(trimmed)
        _settings.value = loadSettings()
        _savedEvent.tryEmit("Settings saved. Changes will apply next time you edit your portfolio.")
        return null
    }

    // ── Re-scan portfolio ─────────────────────────────────────────────────────

    /**
     * Clears only the in-process cached parse result.
     * Next time the user opens Edit screen, it will re-call the parse API.
     */
    fun rescanPortfolio() {
        AppCache.clear()
        _rescanEvent.tryEmit("Cache cleared. Your portfolio will be re-scanned next time you edit.")
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    fun logout() {
        AppCache.clear()
        SecurePrefsManager.clearAll()
        _logoutEvent.tryEmit(Unit)
    }

    // ── Theme mode update ─────────────────────────────────────────────────────

    fun updateThemeMode(mode: Int) {
        ThemeManager.setThemeMode(mode)
        _settings.value = loadSettings()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun loadSettings(): SettingsData {
        val username = SecurePrefsManager.getUsername().orEmpty()
        val repo = SecurePrefsManager.getRepoName().orEmpty()
        return SettingsData(
            username = username,
            repoName = repo,
            branchName = SecurePrefsManager.getBranchName(),
            filePath = SecurePrefsManager.getFilePath().orEmpty(),
            portfolioUrl = buildPortfolioUrl(username, repo),
            githubRepoUrl = if (username.isNotBlank() && repo.isNotBlank())
                "https://github.com/$username/$repo" else "",
            themeMode = SecurePrefsManager.getThemeMode()
        )
    }

    private fun buildPortfolioUrl(username: String, repo: String): String {
        val u = username.lowercase()
        val r = repo.lowercase().trimEnd('/')
        return if (r == "$u.github.io") "https://$u.github.io" else "https://$u.github.io/$r"
    }
}
