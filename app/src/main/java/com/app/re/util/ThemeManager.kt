package com.app.re.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private val _themeMode = MutableStateFlow(SecurePrefsManager.THEME_SYSTEM)
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun init() {
        _themeMode.value = SecurePrefsManager.getThemeMode()
    }

    fun setThemeMode(mode: Int) {
        SecurePrefsManager.setThemeMode(mode)
        _themeMode.value = mode
    }
}
