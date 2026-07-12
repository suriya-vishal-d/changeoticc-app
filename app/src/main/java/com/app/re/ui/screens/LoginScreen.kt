package com.app.re.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.re.R
import com.app.re.ui.theme.AccentCyan
import com.app.re.ui.theme.DarkGlassStroke
import com.app.re.ui.theme.DarkGlowPrimary
import com.app.re.ui.theme.DarkSurfaceElevated
import com.app.re.ui.theme.ElectricAccent
import com.app.re.ui.theme.GradientButton
import com.app.re.ui.theme.GradientEnd
import com.app.re.ui.theme.GradientStart
import com.app.re.ui.theme.glassCard
import com.app.re.ui.viewmodel.LoginUiState
import com.app.re.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToSetup: () -> Unit,
    pendingAuthUri: Uri?,
    onAuthUriHandled: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentUiState by rememberUpdatedState(uiState)

    LaunchedEffect(pendingAuthUri) {
        pendingAuthUri?.let { uri ->
            viewModel.onAuthRedirect(uri)
            onAuthUriHandled()
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> {
                onNavigateToSetup()
                viewModel.resetAfterNavigation()
            }
            is LoginUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearError()
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        viewModel.openCustomTab.collect { url ->
            if (!context.canOpenCustomTab()) {
                viewModel.onAuthCancelledIfStillWaiting()
                snackbarHostState.showSnackbar(
                    "No browser found. Please install Chrome to sign in with GitHub."
                )
                return@collect
            }
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, url.toUri())
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && currentUiState is LoginUiState.Loading) {
                lifecycleOwner.lifecycleScope.launch {
                    delay(400)
                    viewModel.onAuthCancelledIfStillWaiting()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier      = modifier.fillMaxSize(),
        snackbarHost  = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Background gradient blobs ────────────────────────────────
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GradientStart.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ── Header ───────────────────────────────────────────────
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(DarkGlowPrimary, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(listOf(GradientStart, GradientEnd))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter           = painterResource(R.drawable.ic_github),
                            contentDescription = null,
                            modifier          = Modifier.size(38.dp),
                            tint              = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text  = "Connect GitHub",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 30.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text      = "Sign in with GitHub to access and edit your portfolio",
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // ── Feature cards ────────────────────────────────────────
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        icon  = Icons.Default.Lock,
                        text  = "Your credentials are stored securely on your device"
                    )
                    InfoCard(
                        icon  = Icons.Default.SmartToy,
                        text  = "AI reads your HTML so you don't have to"
                    )
                    InfoCard(
                        icon  = Icons.Default.RocketLaunch,
                        text  = "Changes go live on GitHub Pages instantly"
                    )
                }

                // ── CTA section ──────────────────────────────────────────
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState is LoginUiState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(GradientStart.copy(alpha = 0.6f), GradientEnd.copy(alpha = 0.6f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(24.dp),
                                color       = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        }
                    } else {
                        GradientButton(
                            text    = "Continue with GitHub",
                            onClick = viewModel::onLoginClick,
                            enabled = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painter           = painterResource(R.drawable.ic_github),
                                    contentDescription = null,
                                    modifier          = Modifier.size(22.dp),
                                    tint              = Color.White
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text      = "You will be redirected to GitHub to authorize this app",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(
                shape           = RoundedCornerShape(16.dp),
                borderColor     = DarkGlassStroke,
                backgroundColor = DarkSurfaceElevated
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ElectricAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector       = icon,
                contentDescription = null,
                modifier          = Modifier.size(18.dp),
                tint              = ElectricAccent
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
        )
    }
}

private fun android.content.Context.canOpenCustomTab(): Boolean {
    val customTabPackage = CustomTabsClient.getPackageName(this, null)
    if (customTabPackage != null) return true
    val browserIntent    = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
    val resolvedActivity = packageManager.resolveActivity(browserIntent, 0)
    return resolvedActivity != null
}
