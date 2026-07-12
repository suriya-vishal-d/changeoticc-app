package com.app.re.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Gradient Brush helpers ─────────────────────────────────────────────────────

fun primaryGradientBrush(angle: Float = 135f): Brush =
    Brush.linearGradient(colors = PrimaryGradient)

val PrimaryGradientBrush: Brush
    get() = Brush.linearGradient(colors = PrimaryGradient)

val SubtleGradientBrush: Brush
    get() = Brush.linearGradient(
        colors = listOf(GradientStart.copy(alpha = 0.15f), GradientEnd.copy(alpha = 0.08f))
    )

// ── Modifier extensions ────────────────────────────────────────────────────────

/** Dark glass card: translucent elevated surface + subtle violet border */
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = DarkGlassStroke,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = DarkSurfaceElevated
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .border(borderWidth, borderColor, shape)

/** Glow ring drawn behind the composable */
fun Modifier.primaryGlow(
    radius: Dp = 24.dp,
    color: Color = DarkGlowPrimary
): Modifier = this.drawBehind {
    drawCircle(
        color = color,
        radius = (size.minDimension / 2) + radius.toPx()
    )
}

/** Gradient horizontal background */
fun Modifier.gradientBackground(
    brush: Brush = PrimaryGradientBrush
): Modifier = this.background(brush)

// ── Gradient Button ────────────────────────────────────────────────────────────

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 16.dp,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val brush = PrimaryGradientBrush
    val disabledBrush = Brush.linearGradient(
        colors = listOf(GradientStart.copy(alpha = 0.4f), GradientEnd.copy(alpha = 0.4f))
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(54.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = GradientEnd.copy(alpha = 0.4f),
                spotColor   = GradientEnd.copy(alpha = 0.4f)
            )
            .background(
                brush = if (enabled) brush else disabledBrush,
                shape = RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor        = Color.Transparent,
            contentColor          = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor   = Color.White.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text       = text,
            style      = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color      = Color.White
        )
    }
}

// ── Pulsing dot (used for "Connected" indicators) ──────────────────────────────

@Composable
fun PulsingDot(
    color: Color = AccentGreen,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.6f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 0.3f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(size * scale)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = alpha * 0.3f))
        )
        // Solid inner dot
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

// ── Section Label ──────────────────────────────────────────────────────────────

@Composable
fun GradientSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SubtleGradientBrush)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text  = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = ElectricAccent
        )
    }
}
