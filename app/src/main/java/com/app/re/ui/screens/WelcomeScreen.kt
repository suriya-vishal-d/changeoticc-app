package com.app.re.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.re.ui.theme.AccentGreen
import com.app.re.ui.theme.DarkGlassStroke
import com.app.re.ui.theme.DarkGlowPrimary
import com.app.re.ui.theme.DarkSurfaceElevated
import com.app.re.ui.theme.ElectricAccent
import com.app.re.ui.theme.GradientButton
import com.app.re.ui.theme.GradientEnd
import com.app.re.ui.theme.GradientStart
import com.app.re.ui.theme.PrimaryGradientBrush
import com.app.re.ui.theme.glassCard

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var termsAccepted by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient gradient blob top-right
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GradientStart.copy(alpha = 0.18f),
                            GradientEnd.copy(alpha = 0.0f)
                        )
                    )
                )
        )
        // Ambient blob bottom-left
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GradientEnd.copy(alpha = 0.12f),
                            GradientEnd.copy(alpha = 0.0f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glowing icon container
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(DarkGlowPrimary, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(GradientStart, GradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector     = Icons.Default.Code,
                        contentDescription = null,
                        modifier        = Modifier.size(44.dp),
                        tint            = androidx.compose.ui.graphics.Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text  = "Portfolio Editor",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text  = "Edit your GitHub portfolio from your phone",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Terms card ──────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(
                            shape           = RoundedCornerShape(20.dp),
                            borderColor     = DarkGlassStroke,
                            backgroundColor = DarkSurfaceElevated
                        )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text  = "Before you continue",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ElectricAccent
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        TermsBullet("Your portfolio HTML will be sent to an AI model to extract your details")
                        TermsBullet("Your GitHub account will be connected to this app")
                        TermsBullet("Changes you make will be committed directly to your GitHub repository")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked         = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors          = CheckboxDefaults.colors(
                            checkedColor   = ElectricAccent,
                            checkmarkColor = MaterialTheme.colorScheme.background
                        )
                    )
                    Text(
                        text     = "I understand and agree to these terms",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // ── CTA button ──────────────────────────────────────────────────
            GradientButton(
                text    = "Continue",
                onClick = onContinue,
                enabled = termsAccepted,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TermsBullet(text: String) {
    Row(
        modifier          = Modifier.padding(bottom = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(ElectricAccent)
                .padding(top = 6.dp)
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )
    }
}
