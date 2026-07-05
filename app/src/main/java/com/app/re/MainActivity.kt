package com.app.re

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.re.navigation.Routes
import com.app.re.ui.screens.DashboardScreen
import com.app.re.ui.screens.EditScreen
import com.app.re.ui.screens.LoginScreen
import com.app.re.ui.screens.SettingsScreen
import com.app.re.ui.screens.SetupScreen
import com.app.re.ui.screens.WelcomeScreen
import com.app.re.ui.theme.ResumeEditorTheme
import com.app.re.util.SecurePrefsManager
import com.app.re.util.ThemeManager

class MainActivity : ComponentActivity() {

    private var pendingAuthUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SecurePrefsManager.init(this)
        ThemeManager.init()
        pendingAuthUri = extractAuthRedirectUri(intent)

        val startDestination = if (SecurePrefsManager.hasSession()) {
            Routes.DASHBOARD
        } else {
            Routes.WELCOME
        }

        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()

            ResumeEditorTheme(
                darkTheme = when (themeMode) {
                    SecurePrefsManager.THEME_LIGHT -> false
                    SecurePrefsManager.THEME_DARK -> true
                    else -> androidx.compose.foundation.isSystemInDarkTheme()
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(Routes.WELCOME) {
                            WelcomeScreen(
                                onContinue = {
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(Routes.WELCOME) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onNavigateToSetup = {
                                    navController.navigate(Routes.SETUP) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                },
                                pendingAuthUri = pendingAuthUri,
                                onAuthUriHandled = { pendingAuthUri = null }
                            )
                        }
                        composable(Routes.SETUP) {
                            SetupScreen(
                                onNavigateToDashboard = {
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.SETUP) { inclusive = true }
                                    }
                                },
                                onLogout = {
                                    navController.navigate(Routes.WELCOME) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Routes.DASHBOARD) {
                            DashboardScreen(
                                onNavigateToEdit = {
                                    navController.navigate(Routes.EDIT)
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Routes.SETTINGS)
                                },
                                onNavigateToWelcome = {
                                    navController.navigate(Routes.WELCOME) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Routes.EDIT) {
                            EditScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Routes.SETTINGS) {
                            SettingsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToWelcome = {
                                    // Full back-stack clear — user cannot press back after logout
                                    navController.navigate(Routes.WELCOME) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingAuthUri = extractAuthRedirectUri(intent)
    }

    private fun extractAuthRedirectUri(intent: Intent?): Uri? {
        val data = intent?.data ?: return null
        if (data.scheme != "com.app.re" || data.host != "auth") return null
        if (data.path != "/success") return null
        return data
    }
}
