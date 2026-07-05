package com.app.re.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// SetupScreen has its own file: SetupScreen.kt
// DashboardScreen has its own file: DashboardScreen.kt
// EditScreen has its own file: EditScreen.kt

@Composable
private fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title — coming soon",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
