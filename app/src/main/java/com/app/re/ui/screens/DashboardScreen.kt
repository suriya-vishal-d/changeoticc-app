package com.app.re.ui.screens

import android.content.Intent
import android.print.PrintManager
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    val isParseReady by viewModel.isParseReady.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val showPortfolioWarning = remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showPortfolioWarning.value) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPortfolioWarning.value = false },
            title = { Text("Note") },
            text = { Text("It may take a few minutes for recent updates to reflect on the live portfolio.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    com.app.re.util.SecurePrefsManager.setPortfolioUpdateAcknowledged(true)
                    showPortfolioWarning.value = false
                    val url = info?.portfolioUrl
                    if (url != null) {
                        CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build()
                            .launchUrl(context, url.toUri())
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            TopAppBar(
                title = {
                    // Show username in the top bar (like the wireframe "suriya-vishal-d")
                    info?.let { dashInfo ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Small avatar circle
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dashInfo.avatarInitial.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = dashInfo.username,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } ?: Text(
                        text = "Portfolio Editor",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    // Connected badge in top bar
                    ConnectedBadge()
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Scrollable content ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Hero Preview Card ──────────────────────────────────────
                info?.let { dashInfo ->
                    HeroPreviewCard(
                        dashInfo = dashInfo,
                        onViewLive = {
                            if (!com.app.re.util.SecurePrefsManager.isPortfolioUpdateAcknowledged()) {
                                showPortfolioWarning.value = true
                            } else {
                                CustomTabsIntent.Builder()
                                    .setShowTitle(true)
                                    .build()
                                    .launchUrl(context, dashInfo.portfolioUrl.toUri())
                            }
                        }
                    )
                }

                // ── Quick Actions: Edit + More Options ────────────────────
                info?.let { dashInfo ->
                    QuickActionsRow(
                        portfolioUrl = dashInfo.portfolioUrl,
                        onEdit = {
                            if (isParseReady) {
                                onNavigateToEdit()
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    "AI is analyzing your portfolio... Please wait a moment.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }

                // ── Pro Tip Card ───────────────────────────────────────────
                TipsSection()

                Spacer(modifier = Modifier.height(8.dp))

                // ── Last Updated Footer ────────────────────────────────────
                Text(
                    text = "Last updated: just now",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Miniature WebView Preview ──────────────────────────────────
            // The WebView is sized to fill the box exactly (fillMaxSize).
            // loadWithOverviewMode + useWideViewPort tell the engine to render
            // the full desktop-width page and then scale it DOWN to fit the
            // WebView's own width — which is exactly what a thumbnail preview needs.
            // No manual graphicsLayer tricks required.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled    = true
                            // "Overview mode" scales the page to fit the WebView width —
                            // this is the key setting that makes the thumbnail work.
                            settings.loadWithOverviewMode = true
                            // "Wide viewport" tells the engine to use the site's full
                            // desktop viewport width before scaling it down.
                            settings.useWideViewPort      = true
                            // Disable built-in zoom controls so the preview looks clean.
                            settings.builtInZoomControls  = false
                            settings.displayZoomControls  = false
                            // Prevent the WebView from intercepting all scroll gestures.
                            isVerticalScrollBarEnabled    = false
                            isHorizontalScrollBarEnabled  = false
                            webViewClient = WebViewClient()
                            loadUrl(dashInfo.portfolioUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Transparent overlay — captures taps so the WebView
                // doesn't swallow touch events on the preview card.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onViewLive() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Your portfolio is live!" label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Your portfolio is live! 🚀",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // "View Live Portfolio ↗" button
            Button(
                onClick = onViewLive,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View Live Portfolio  ↗",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

// ── Connected Badge ────────────────────────────────────────────────────────────

@Composable
private fun ConnectedBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF22C55E).copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(0xFF22C55E))
        )
        Text(
            text = "Connected to GitHub",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF22C55E)
        )
    }
}

// ── Quick Actions Row: icon-only chips ────────────────────────────────────────

@Composable
private fun QuickActionsRow(
    portfolioUrl: String,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Quick",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit
            QuickChipButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Edit,
                label = "Edit",
                onClick = onEdit
            )
            // More Options
            Box(modifier = Modifier.weight(1f)) {
                QuickChipButton(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.MoreVert,
                    label = "More",
                    onClick = { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            expanded = false
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, "Check out my portfolio: $portfolioUrl")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Portfolio"))
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy Link") },
                        onClick = {
                            expanded = false
                            val clipboard =
                                context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                        as android.content.ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText("Portfolio URL", portfolioUrl)
                            )
                            android.widget.Toast.makeText(
                                context,
                                "Link copied!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Download") },
                        onClick = {
                            expanded = false
                            downloadPortfolio(context, portfolioUrl)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun downloadPortfolio(context: android.content.Context, url: String?) {
    if (url == null) {
        android.widget.Toast.makeText(
            context,
            "Portfolio URL not available.",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        return
    }

    val webView = WebView(context)
    webView.settings.javaScriptEnabled = true

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, loadedUrl: String?) {
            // Inject CSS + JS to prepare the page for a full, clean PDF:
            //  1. Reveal every section / page div that may be JS-hidden.
            //  2. Remove navigation bars, sticky headers, and any element
            //     that looks like an admin or editor panel.
            //  3. Let the browser lay out everything as a continuous document.
            val prepareAndPrint = """
                (function() {
                    // ── 1. Inject print-safe stylesheet ──────────────────────
                    var style = document.createElement('style');
                    style.innerHTML = `
                        /* Force every section / page element to be visible */
                        section, .section,
                        [id*="section"], [class*="section"],
                        [id*="page"],    [class*="page"],
                        .tab-content,   .tab-pane,
                        [id*="about"],  [id*="projects"], [id*="contact"],
                        [id*="skills"], [id*="experience"], [id*="education"],
                        [class*="about"], [class*="projects"], [class*="contact"],
                        [class*="skills"], [class*="experience"], [class*="education"] {
                            display: block !important;
                            visibility: visible !important;
                            opacity: 1 !important;
                            height: auto !important;
                            overflow: visible !important;
                        }

                        /* Hide navigation, sticky bars, and admin-related elements */
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

                        /* Clean page breaks between major sections */
                        section, .section {
                            page-break-inside: avoid;
                        }
                    `;
                    document.head.appendChild(style);

                    // ── 2. Force-show any element hidden purely via inline style ──
                    // (common pattern: el.style.display = 'none' from a JS router)
                    var allEls = document.querySelectorAll(
                        'section, .section, [id*="section"], [class*="page"], ' +
                        '[id*="about"], [id*="projects"], [id*="contact"], ' +
                        '[id*="skills"], [id*="experience"], [id*="education"]'
                    );
                    allEls.forEach(function(el) {
                        if (el.style.display === 'none') {
                            el.style.setProperty('display', 'block', 'important');
                        }
                        if (el.style.visibility === 'hidden') {
                            el.style.setProperty('visibility', 'visible', 'important');
                        }
                    });
                })();
            """.trimIndent()

            // Run the prep script, wait a moment for layout to settle, then print
            view?.evaluateJavascript(prepareAndPrint) {
                // Small delay so the browser can reflow after revealing hidden sections
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        val printManager =
                            context.getSystemService(android.content.Context.PRINT_SERVICE)
                                    as android.print.PrintManager
                        val printAdapter =
                            view.createPrintDocumentAdapter("Portfolio PDF")
                        printManager.print(
                            "Portfolio PDF",
                            printAdapter,
                            android.print.PrintAttributes.Builder().build()
                        )
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            context,
                            "Could not start print: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
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
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = tween(100),
        label = "chipScale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ── Tips Section ──────────────────────────────────────────────────────────────

@Composable
private fun TipsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Tip",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "💡 Pro Tip",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Adding a detailed 'About Me' section can increase your profile engagement by up to 40%.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

