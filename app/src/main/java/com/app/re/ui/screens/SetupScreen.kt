package com.app.re.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.re.ui.theme.AccentGreen
import com.app.re.ui.theme.DarkGlassStroke
import com.app.re.ui.theme.DarkSurfaceElevated
import com.app.re.ui.theme.ElectricAccent
import com.app.re.ui.theme.GradientButton
import com.app.re.ui.theme.GradientEnd
import com.app.re.ui.theme.GradientSectionLabel
import com.app.re.ui.theme.GradientStart
import com.app.re.ui.theme.glassCard
import com.app.re.ui.viewmodel.SetupUiState
import com.app.re.ui.viewmodel.SetupViewModel
import com.app.re.util.SecurePrefsManager

@Composable
fun SetupScreen(
    onNavigateToDashboard: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetupViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var repoName   by rememberSaveable { mutableStateOf("") }
    var branchName by rememberSaveable { mutableStateOf("main") }
    var filePath   by rememberSaveable { mutableStateOf("index.html") }

    val username = remember { SecurePrefsManager.getUsername() ?: "there" }

    LaunchedEffect(uiState) {
        // Navigation is handled by the "Go to Dashboard" button click
    }

    Scaffold(
        modifier       = modifier.fillMaxSize(),
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Background gradient blob
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GradientStart.copy(alpha = 0.12f),
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
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ── Header ───────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(
                        modifier            = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text  = "Hey, $username! 👋",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text  = "Let's find your portfolio. One-time setup.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = {
                        SecurePrefsManager.clearAll()
                        onLogout()
                    }) {
                        Text("Switch Account", color = ElectricAccent)
                    }
                }

                // ── Form card ────────────────────────────────────────────
                val isFormEnabled = uiState !is SetupUiState.Loading && uiState !is SetupUiState.Success

                GradientSectionLabel("Repository Details")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(
                            shape           = RoundedCornerShape(20.dp),
                            borderColor     = DarkGlassStroke,
                            backgroundColor = DarkSurfaceElevated
                        )
                ) {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Repository name
                        SetupField(
                            label       = "Repository Name",
                            value       = repoName,
                            onValueChange = { repoName = it },
                            placeholder = "e.g. portfolio, resume, username.github.io",
                            enabled     = isFormEnabled
                        )
                        // Branch
                        SetupField(
                            label       = "Branch Name",
                            value       = branchName,
                            onValueChange = { branchName = it },
                            placeholder = "e.g. main, master, dev",
                            enabled     = isFormEnabled
                        )
                        // File path
                        SetupField(
                            label         = "File Path",
                            value         = filePath,
                            onValueChange = { filePath = it },
                            placeholder   = "index.html",
                            enabled       = isFormEnabled,
                            supportText   = "Include subfolders if needed — e.g. src/index.html"
                        )

                        // Find button
                        if (uiState is SetupUiState.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
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
                                text     = "Find My Portfolio",
                                onClick  = { viewModel.findPortfolio(repoName, filePath, branchName) },
                                enabled  = isFormEnabled,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ── Result cards ─────────────────────────────────────────
                when (val state = uiState) {
                    is SetupUiState.Success -> {
                        SuccessResultCard(
                            detectedName    = state.detectedName,
                            onGoToDashboard = onNavigateToDashboard
                        )
                    }
                    is SetupUiState.Error -> {
                        ErrorResultCard(
                            message   = state.message,
                            onTryAgain = { viewModel.resetError() }
                        )
                    }
                    else -> Unit
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── Reusable field ─────────────────────────────────────────────────────────────

@Composable
private fun SetupField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    supportText: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color      = ElectricAccent
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = {
                Text(
                    text  = placeholder,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            enabled       = enabled,
            singleLine    = true,
            shape         = RoundedCornerShape(12.dp),
            supportingText = supportText?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = ElectricAccent,
                unfocusedBorderColor  = DarkGlassStroke,
                disabledBorderColor   = DarkGlassStroke,
                focusedContainerColor = DarkSurfaceElevated,
                unfocusedContainerColor = DarkSurfaceElevated,
                disabledContainerColor  = DarkSurfaceElevated.copy(alpha = 0.5f),
                cursorColor           = ElectricAccent,
                focusedTextColor      = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor    = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// ── Success Result Card ───────────────────────────────────────────────────────

@Composable
private fun SuccessResultCard(
    detectedName: String,
    onGoToDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AccentGreen.copy(alpha = 0.10f))
            .border(1.dp, AccentGreen.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint               = AccentGreen,
                modifier           = Modifier.size(44.dp)
            )
            Text(
                text      = "Portfolio found! Ready to edit.",
                style     = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color     = AccentGreen,
                textAlign = TextAlign.Center
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text  = "Detected: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = detectedName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            GradientButton(
                text     = "Go to Dashboard →",
                onClick  = onGoToDashboard,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Error Result Card ─────────────────────────────────────────────────────────

@Composable
private fun ErrorResultCard(
    message: String,
    onTryAgain: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Error,
                contentDescription = "Error",
                tint               = MaterialTheme.colorScheme.error,
                modifier           = Modifier.size(40.dp)
            )
            Text(
                text      = message,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onTryAgain) {
                Text(
                    text  = "Try Again",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
