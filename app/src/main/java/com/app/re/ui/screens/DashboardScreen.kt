package com.app.re.ui.screens

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.re.ui.theme.AccentCyan
import com.app.re.ui.theme.AccentGreen
import com.app.re.ui.theme.AccentYellow
import com.app.re.ui.theme.DarkGlassStroke
import com.app.re.ui.theme.DarkGlowPrimary
import com.app.re.ui.theme.DarkSurfaceElevated
import com.app.re.ui.theme.ElectricAccent
import com.app.re.ui.theme.GradientButton
import com.app.re.ui.theme.GradientEnd
import com.app.re.ui.theme.GradientSectionLabel
import com.app.re.ui.theme.GradientStart
import com.app.re.ui.theme.PulsingDot
import com.app.re.ui.theme.glassCard
import com.app.re.ui.viewmodel.DashboardInfo
import com.app.re.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToEdit: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val info by viewModel.info.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val showPortfolioWarning = remember { mutableStateOf(false) }

    if (showPortfolioWarning.value) {
        AlertDialog(
            onDismissRequest = { showPortfolioWarning.value = false },
            title            = { Text("Note") },
            text             = {
                Text("It may take a few minutes for recent updates to reflect on the live portfolio.")
            },
            confirmButton    = {
                TextButton(onClick = {
                    com.app.re.util.SecurePrefsManager.setPortfolioUpdateAcknowledged(true)
                    showPortfolioWarning.value = false
                    val url = info?.portfolioUrl
                    if (url != null) {
                        CustomTabsIntent.Builder().setShowTitle(true).build()
                            .launchUrl(context, url.toUri())
                    }
                }) { Text("OK") }
            }
        )
    }

    Scaffold(
        modifier       = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar         = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title    = {
                    info?.let { dashInfo ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar circle with gradient
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(GradientStart, GradientEnd))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text  = dashInfo.avatarInitial.toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text      = dashInfo.username,
                                style     = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color     = MaterialTheme.colorScheme.onSurface,
                                maxLines  = 1,
                                overflow  = TextOverflow.Ellipsis
                            )
                        }
                    } ?: Text(
                        text  = "Portfolio Editor",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions  = {
                    ConnectedBadge()
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector       = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint              = MaterialTheme.colorScheme.onSurfaceVariant
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
            // Subtle ambient glow behind hero card
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GradientStart.copy(alpha = 0.10f),
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
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // ── Hero Preview Card ────────────────────────────────────
                info?.let { dashInfo ->
                    HeroPreviewCard(
                        dashInfo   = dashInfo,
                        onViewLive = {
                            if (!com.app.re.util.SecurePrefsManager.isPortfolioUpdateAcknowledged()) {
                                showPortfolioWarning.value = true
                            } else {
                                CustomTabsIntent.Builder().setShowTitle(true).build()
                                    .launchUrl(context, dashInfo.portfolioUrl.toUri())
                            }
                        }
                    )
                }

                // ── Quick Actions ────────────────────────────────────────
                info?.let { dashInfo ->
                    QuickActionsRow(
                        portfolioUrl = dashInfo.portfolioUrl,
                        onEdit       = onNavigateToEdit
                    )
                }

                // ── Tips ─────────────────────────────────────────────────
                TipsSection()

                // ── Footer ───────────────────────────────────────────────
                Text(
                    text     = "Last updated: just now",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// ── Hero Preview Card ──────────────────────────────────────────────────────────

@Composable
private fun HeroPreviewCard(
    dashInfo: DashboardInfo,
    onViewLive: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 16.dp,
                shape        = RoundedCornerShape(24.dp),
                ambientColor = GradientStart.copy(alpha = 0.3f),
                spotColor    = GradientEnd.copy(alpha = 0.3f)
            )
            .glassCard(
                shape           = RoundedCornerShape(24.dp),
                borderColor     = DarkGlassStroke,
                backgroundColor = DarkSurfaceElevated
            )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gradient top strip label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                    .padding(bottom = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            // WebView preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        1.dp,
                        DarkGlassStroke,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                AndroidView(
                    factory  = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled    = true
                            settings.cacheMode            = android.webkit.WebSettings.LOAD_NO_CACHE
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort      = true
                            settings.builtInZoomControls  = false
                            settings.displayZoomControls  = false
                            isVerticalScrollBarEnabled    = false
                            isHorizontalScrollBarEnabled  = false
                            webViewClient = WebViewClient()
                            clearCache(true)
                            loadUrl(dashInfo.portfolioUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                // Transparent tap overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { onViewLive() }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Status row
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingDot(color = AccentGreen, size = 9.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text  = "Your portfolio is live! 🚀",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // View live button
            GradientButton(
                text    = "View Live Portfolio  ↗",
                onClick = onViewLive,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector       = Icons.Default.Language,
                        contentDescription = null,
                        modifier          = Modifier.size(18.dp),
                        tint              = Color.White
                    )
                }
            )
        }
    }
}

// ── Connected Badge ────────────────────────────────────────────────────────────

@Composable
private fun ConnectedBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AccentGreen.copy(alpha = 0.12f))
            .border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PulsingDot(color = AccentGreen, size = 7.dp)
        Text(
            text  = "Connected",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = AccentGreen
        )
    }
}

// ── Quick Actions Row ──────────────────────────────────────────────────────────

@Composable
private fun QuickActionsRow(
    portfolioUrl: String,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Column {
        GradientSectionLabel(text = "Quick Actions")
        Spacer(Modifier.height(12.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickChipButton(
                modifier = Modifier.weight(1f),
                icon     = Icons.Default.Edit,
                label    = "Edit",
                onClick  = onEdit,
                accent   = ElectricAccent
            )
            Box(modifier = Modifier.weight(1f)) {
                QuickChipButton(
                    modifier = Modifier.fillMaxWidth(),
                    icon     = Icons.Default.MoreVert,
                    label    = "More",
                    onClick  = { expanded = true },
                    accent   = AccentCyan
                )
                DropdownMenu(
                    expanded          = expanded,
                    onDismissRequest  = { expanded = false },
                    modifier          = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text          = { Text("Share") },
                        onClick       = {
                            expanded = false
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, "Check out my portfolio: $portfolioUrl")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Portfolio"))
                        },
                        leadingIcon   = {
                            Icon(Icons.Default.Share, null, tint = ElectricAccent)
                        }
                    )
                    DropdownMenuItem(
                        text          = { Text("Copy Link") },
                        onClick       = {
                            expanded = false
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                    as android.content.ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText("Portfolio URL", portfolioUrl)
                            )
                            android.widget.Toast.makeText(context, "Link copied!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        leadingIcon   = {
                            Icon(Icons.Default.ContentCopy, null, tint = ElectricAccent)
                        }
                    )
                    DropdownMenuItem(
                        text          = { Text("Download PDF") },
                        onClick       = {
                            expanded = false
                            downloadPortfolio(context, portfolioUrl)
                        },
                        leadingIcon   = {
                            Icon(Icons.Default.Download, null, tint = ElectricAccent)
                        }
                    )
                }
            }
        }
    }
}

private fun downloadPortfolio(context: android.content.Context, url: String?) {
    if (url == null) {
        android.widget.Toast.makeText(context, "Portfolio URL not available.", android.widget.Toast.LENGTH_SHORT).show()
        return
    }
    val webView = WebView(context)
    webView.settings.javaScriptEnabled = true
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, loadedUrl: String?) {
            val prepareAndPrint = """
                (function() {
                    var style = document.createElement('style');
                    style.innerHTML = `
                        section, .section,
                        [id*="section"], [class*="section"],
                        [id*="page"], [class*="page"],
                        .tab-content, .tab-pane,
                        [id*="about"], [id*="projects"], [id*="contact"],
                        [id*="skills"], [id*="experience"], [id*="education"],
                        [class*="about"], [class*="projects"], [class*="contact"],
                        [class*="skills"], [class*="experience"], [class*="education"] {
                            display: block !important;
                            visibility: visible !important;
                            opacity: 1 !important;
                            height: auto !important;
                            overflow: visible !important;
                        }
                        nav, header nav, .navbar, .nav-bar, .navigation,
                        .sticky, [class*="sticky"],
                        .sidebar, .side-bar,
                        .fab, .floating-action,
                        [id*="admin"], [class*="admin"],
                        [id*="editor"], [class*="editor"],
                        [id*="dashboard"], [class*="dashboard"],
                        .back-to-top, [class*="scroll-top"],
                        .cookie-banner, .cookie-notice {
                            display: none !important;
                        }
                        section, .section { page-break-inside: avoid; }
                    `;
                    document.head.appendChild(style);
                    var allEls = document.querySelectorAll(
                        'section, .section, [id*="section"], [class*="page"], ' +
                        '[id*="about"], [id*="projects"], [id*="contact"], ' +
                        '[id*="skills"], [id*="experience"], [id*="education"]'
                    );
                    allEls.forEach(function(el) {
                        if (el.style.display === 'none') el.style.setProperty('display', 'block', 'important');
                        if (el.style.visibility === 'hidden') el.style.setProperty('visibility', 'visible', 'important');
                    });
                })();
            """.trimIndent()
            view?.evaluateJavascript(prepareAndPrint) {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
                        val printAdapter = view.createPrintDocumentAdapter("Portfolio PDF")
                        printManager.print("Portfolio PDF", printAdapter, android.print.PrintAttributes.Builder().build())
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Could not start print: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }, 800L)
            }
        }
    }
    webView.loadUrl(url)
}

@Composable
private fun QuickChipButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    accent: Color = ElectricAccent,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue  = if (isPressed) 0.93f else 1f,
        animationSpec = tween(100),
        label        = "chipScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation    = 6.dp,
                shape        = RoundedCornerShape(16.dp),
                ambientColor = accent.copy(alpha = 0.15f),
                spotColor    = accent.copy(alpha = 0.15f)
            )
            .glassCard(
                shape           = RoundedCornerShape(16.dp),
                borderColor     = DarkGlassStroke,
                backgroundColor = DarkSurfaceElevated
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = label,
                    tint               = accent,
                    modifier           = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ── Tips Section ───────────────────────────────────────────────────────────────

@Composable
private fun TipsSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(GradientStart.copy(alpha = 0.18f), GradientEnd.copy(alpha = 0.10f))
                )
            )
            .border(1.dp, DarkGlassStroke, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentYellow.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Lightbulb,
                    contentDescription = "Tip",
                    tint               = AccentYellow,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text  = "Pro Tip",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentYellow
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = "Adding a detailed 'About Me' section can increase your profile engagement by up to 40%.",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
