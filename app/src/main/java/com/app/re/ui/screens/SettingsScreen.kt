package com.app.re.ui.screens

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.re.BuildConfig
import com.app.re.ui.theme.DarkGlassStroke
import com.app.re.ui.theme.DarkSurfaceElevated
import com.app.re.ui.theme.ElectricAccent
import com.app.re.ui.theme.GradientEnd
import com.app.re.ui.theme.GradientSectionLabel
import com.app.re.ui.theme.GradientStart
import com.app.re.ui.theme.glassCard
import com.app.re.ui.viewmodel.SettingsData
import com.app.re.ui.viewmodel.SettingsViewModel
import com.app.re.util.SecurePrefsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch { viewModel.savedEvent.collect  { msg -> snackbarHostState.showSnackbar(msg) } }
        launch { viewModel.rescanEvent.collect { msg -> snackbarHostState.showSnackbar(msg) } }
        launch { viewModel.logoutEvent.collect { onNavigateToWelcome() } }
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor   = DarkSurfaceElevated,
            title            = {
                Text(
                    "Disconnect GitHub?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text             = {
                Text(
                    "This will remove your GitHub connection and all saved data from this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton    = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                }) {
                    Text("Disconnect", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton    = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = ElectricAccent)
                }
            }
        )
    }

    // Theme dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor   = DarkSurfaceElevated,
            title            = {
                Text(
                    "Choose Theme",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text             = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ThemeOption(
                        label    = "System Default",
                        selected = settings.themeMode == SecurePrefsManager.THEME_SYSTEM,
                        onClick  = { viewModel.updateThemeMode(SecurePrefsManager.THEME_SYSTEM); showThemeDialog = false }
                    )
                    ThemeOption(
                        label    = "Light",
                        selected = settings.themeMode == SecurePrefsManager.THEME_LIGHT,
                        onClick  = { viewModel.updateThemeMode(SecurePrefsManager.THEME_LIGHT); showThemeDialog = false }
                    )
                    ThemeOption(
                        label    = "Dark",
                        selected = settings.themeMode == SecurePrefsManager.THEME_DARK,
                        onClick  = { viewModel.updateThemeMode(SecurePrefsManager.THEME_DARK); showThemeDialog = false }
                    )
                }
            },
            confirmButton    = {}
        )
    }

    Scaffold(
        modifier       = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar         = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title    = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint               = ElectricAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Background glow
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GradientStart.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // ── Account ───────────────────────────────────────────────
                SettingsSection(title = "Account") {
                    AccountRow(settings = settings)
                    HorizontalDivider(color = DarkGlassStroke, thickness = 0.5.dp)
                    SettingsRow(
                        icon       = null,
                        label      = "Disconnect GitHub",
                        labelColor = MaterialTheme.colorScheme.error,
                        onClick    = { showLogoutDialog = true }
                    )
                }

                // ── Portfolio ─────────────────────────────────────────────
                SettingsSection(title = "Portfolio") {
                    InlineEditRow(
                        label       = "Repository Name",
                        value       = settings.repoName,
                        placeholder = "e.g. portfolio",
                        onSave      = { newVal ->
                            val error = viewModel.updateRepoName(newVal)
                            if (error != null) scope.launch { snackbarHostState.showSnackbar(error) }
                        }
                    )
                    HorizontalDivider(color = DarkGlassStroke, thickness = 0.5.dp)
                    InlineEditRow(
                        label       = "Branch Name",
                        value       = settings.branchName,
                        placeholder = "main",
                        onSave      = { newVal ->
                            val error = viewModel.updateBranchName(newVal)
                            if (error != null) scope.launch { snackbarHostState.showSnackbar(error) }
                        }
                    )
                    HorizontalDivider(color = DarkGlassStroke, thickness = 0.5.dp)
                    InlineEditRow(
                        label       = "File Path",
                        value       = settings.filePath,
                        placeholder = "index.html",
                        onSave      = { newVal ->
                            val error = viewModel.updateFilePath(newVal)
                            if (error != null) scope.launch { snackbarHostState.showSnackbar(error) }
                        }
                    )
                    HorizontalDivider(color = DarkGlassStroke, thickness = 0.5.dp)
                    SettingsRow(
                        icon     = Icons.Default.Refresh,
                        label    = "Re-scan Portfolio",
                        sublabel = "Clear cached data and fetch fresh from GitHub",
                        onClick  = { viewModel.rescanPortfolio() }
                    )
                }

                // ── Appearance ────────────────────────────────────────────
                SettingsSection(title = "Appearance") {
                    SettingsRow(
                        icon = when (settings.themeMode) {
                            SecurePrefsManager.THEME_LIGHT -> Icons.Default.LightMode
                            SecurePrefsManager.THEME_DARK  -> Icons.Default.DarkMode
                            else                           -> Icons.Default.Brightness4
                        },
                        label    = "App Theme",
                        sublabel = when (settings.themeMode) {
                            SecurePrefsManager.THEME_LIGHT -> "Light"
                            SecurePrefsManager.THEME_DARK  -> "Dark"
                            else                           -> "System Default"
                        },
                        onClick = { showThemeDialog = true }
                    )
                }

                // ── App ───────────────────────────────────────────────────
                if (settings.githubRepoUrl.isNotBlank()) {
                    SettingsSection(title = "App") {
                        SettingsRow(
                            icon     = Icons.Default.OpenInNew,
                            label    = "Visit GitHub Repo",
                            sublabel = settings.githubRepoUrl,
                            onClick  = {
                                CustomTabsIntent.Builder().setShowTitle(true).build()
                                    .launchUrl(context, settings.githubRepoUrl.toUri())
                            }
                        )
                    }
                }

                // ── Footer ────────────────────────────────────────────────
                Text(
                    text     = "Portfolio Editor  v${BuildConfig.VERSION_NAME}",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Theme option ──────────────────────────────────────────────────────────────

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) ElectricAccent else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Settings section card ─────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GradientSectionLabel(text = title)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(
                    shape           = RoundedCornerShape(16.dp),
                    borderColor     = DarkGlassStroke,
                    backgroundColor = DarkSurfaceElevated
                )
        ) {
            content()
        }
    }
}

// ── Account row ───────────────────────────────────────────────────────────────

@Composable
private fun AccountRow(settings: SettingsData) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(GradientStart, GradientEnd))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = settings.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = settings.username.ifBlank { "Unknown" },
                style    = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text     = "github.com/${settings.username}",
                style    = MaterialTheme.typography.bodySmall,
                color    = ElectricAccent.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Generic settings row ──────────────────────────────────────────────────────

@Composable
private fun SettingsRow(
    icon: ImageVector?,
    label: String,
    sublabel: String? = null,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = label,
                    tint               = ElectricAccent,
                    modifier           = Modifier.size(16.dp)
                )
            }
        } else {
            Spacer(Modifier.size(32.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = labelColor
            )
            if (!sublabel.isNullOrBlank()) {
                Text(
                    text     = sublabel,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text("›", color = MaterialTheme.colorScheme.outline, fontSize = 20.sp)
    }
}

// ── Inline-edit row ───────────────────────────────────────────────────────────

@Composable
private fun InlineEditRow(
    label: String,
    value: String,
    placeholder: String,
    onSave: (String) -> Unit
) {
    var editing    by rememberSaveable { mutableStateOf(false) }
    var draftValue by rememberSaveable(value) { mutableStateOf(value) }

    if (editing) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ElectricAccent
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value         = draftValue,
                    onValueChange = { draftValue = it },
                    modifier      = Modifier.weight(1f),
                    placeholder   = { Text(placeholder, color = MaterialTheme.colorScheme.outline) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor    = ElectricAccent,
                        unfocusedBorderColor  = DarkGlassStroke,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        cursorColor           = ElectricAccent
                    )
                )
                IconButton(
                    onClick  = { onSave(draftValue); editing = false },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                ) {
                    Icon(Icons.Default.Check, "Save", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick  = { draftValue = value; editing = false },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkGlassStroke)
                ) {
                    Icon(Icons.Default.Close, "Cancel", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    } else {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .clickable { editing = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Edit,
                    contentDescription = "Edit $label",
                    tint               = ElectricAccent,
                    modifier           = Modifier.size(16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text     = value.ifBlank { placeholder },
                    style    = MaterialTheme.typography.bodySmall,
                    color    = if (value.isBlank()) MaterialTheme.colorScheme.outline else ElectricAccent.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text("›", color = MaterialTheme.colorScheme.outline, fontSize = 20.sp)
        }
    }
}
