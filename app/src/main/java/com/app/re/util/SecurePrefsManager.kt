package com.app.re.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePrefsManager {

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return

        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveJwt(jwt: String) {
        prefs.edit().putString(KEY_JWT, jwt).apply()
    }

    fun getJwt(): String? = prefs.getString(KEY_JWT, null)?.takeIf { it.isNotBlank() }

    fun saveUsername(username: String) {
        prefs.edit().putString(KEY_GITHUB_USERNAME, username).apply()
    }

    fun getUsername(): String? = prefs.getString(KEY_GITHUB_USERNAME, null)?.takeIf { it.isNotBlank() }

    fun saveAvatarUrl(url: String) {
        prefs.edit().putString(KEY_AVATAR_URL, url).apply()
    }

    fun getAvatarUrl(): String? = prefs.getString(KEY_AVATAR_URL, null)?.takeIf { it.isNotBlank() }

    fun saveRepoName(repo: String) {
        prefs.edit().putString(KEY_REPO_NAME, repo).apply()
    }

    fun getRepoName(): String? = prefs.getString(KEY_REPO_NAME, null)?.takeIf { it.isNotBlank() }

    fun saveBranchName(branch: String) {
        prefs.edit().putString(KEY_BRANCH_NAME, branch).apply()
    }

    fun getBranchName(): String = prefs.getString(KEY_BRANCH_NAME, "main") ?: "main"

    fun saveFilePath(path: String) {
        prefs.edit().putString(KEY_FILE_PATH, path).apply()
    }

    fun getFilePath(): String? = prefs.getString(KEY_FILE_PATH, null)?.takeIf { it.isNotBlank() }

    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

    fun setFirstLaunchDone() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun saveLastUpdated(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_UPDATED, timestamp).apply()
    }

    fun getLastUpdated(): Long = prefs.getLong(KEY_LAST_UPDATED, 0L)

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun getThemeMode(): Int = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)

    fun setPortfolioUpdateAcknowledged(ack: Boolean) {
        prefs.edit().putBoolean(KEY_PORTFOLIO_UPDATE_ACKNOWLEDGED, ack).apply()
    }

    fun isPortfolioUpdateAcknowledged(): Boolean = prefs.getBoolean(KEY_PORTFOLIO_UPDATE_ACKNOWLEDGED, true)

    fun hasSession(): Boolean = getJwt() != null && getRepoName() != null

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    private const val PREFS_FILE_NAME = "portfolio_editor_session"
    private const val KEY_JWT = "jwt"
    private const val KEY_GITHUB_USERNAME = "github_username"
    private const val KEY_AVATAR_URL = "avatar_url"
    private const val KEY_REPO_NAME = "repo_name"
    private const val KEY_BRANCH_NAME = "branch_name"
    private const val KEY_FILE_PATH = "file_path"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    private const val KEY_LAST_UPDATED = "last_updated"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_PORTFOLIO_UPDATE_ACKNOWLEDGED = "portfolio_update_acknowledged"
}

