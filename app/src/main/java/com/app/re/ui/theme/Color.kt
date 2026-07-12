package com.app.re.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand Gradient Accent (Electric Blue) ─────────────────────────────────────
val GradientStart   = Color(0xFF0EA5E9) // Sky blue
val GradientMid     = Color(0xFF2563EB) // Royal blue
val GradientEnd     = Color(0xFF1D4ED8) // Deep blue
val ElectricAccent  = Color(0xFF60A5FA) // Light electric blue (glow / text)

// ── Dark Theme ─────────────────────────────────────────────────────────────────
val DarkPrimary         = Color(0xFF3B82F6) // Vibrant blue — primary actions
val DarkOnPrimary       = Color(0xFFFFFFFF)
val DarkBackground      = Color(0xFF08090F) // Near-black with a cool blue tint
val DarkOnBackground    = Color(0xFFE8F0FF) // Slightly blue-tinted white
val DarkSurface         = Color(0xFF0E111C) // Dark navy-black surface
val DarkSurfaceVariant  = Color(0xFF141929) // Elevated surface — slightly lighter
val DarkSurfaceElevated = Color(0xFF1A2038) // Card backgrounds
val DarkOnSurface       = Color(0xFFE8F0FF)
val DarkOutline         = Color(0xFF252D45) // Subtle border
val DarkGlassStroke     = Color(0xFF2A3A60) // Glass card border
val DarkGlowPrimary     = Color(0x333B82F6) // 20% opacity blue glow

// ── Light Theme ────────────────────────────────────────────────────────────────
val LightPrimary         = Color(0xFF2563EB) // Royal blue
val LightOnPrimary       = Color(0xFFFFFFFF)
val LightBackground      = Color(0xFFF0F5FF) // Soft sky-blue-white
val LightOnBackground    = Color(0xFF0F1A35)
val LightSurface         = Color(0xFFFFFFFF)
val LightSurfaceVariant  = Color(0xFFDBEAFE) // Soft blue tint
val LightSurfaceElevated = Color(0xFFE8F0FE)
val LightOnSurface       = Color(0xFF0F1A35)
val LightOutline         = Color(0xFFBFD3F8)
val LightGlassStroke     = Color(0xFF93B4F5)

// ── Semantic / Accent ──────────────────────────────────────────────────────────
val AccentGreen   = Color(0xFF22C55E) // Connected green
val AccentYellow  = Color(0xFFFBBF24) // Warnings / tips
val AccentCyan    = Color(0xFF06B6D4) // Informational highlight
val ErrorRed      = Color(0xFFEF4444)

// ── Gradient pairs (used in GradientUtils) ────────────────────────────────────
val PrimaryGradient    = listOf(GradientStart, GradientEnd)
val PrimaryGradientExt = listOf(GradientStart, GradientMid, GradientEnd)