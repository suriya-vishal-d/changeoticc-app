import re

with open('app/src/main/java/com/app/re/ui/screens/EditScreen.kt', 'r') as f:
    code = f.read()

# Top app bar
code = code.replace("TopAppBarDefaults.topAppBarColors(containerColor = Color.White)", "TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)")

# Scaffold background
code = code.replace("containerColor = Color(0xFFF5F5F7)", "containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background")

# General text
code = code.replace("color = Color.Black", "color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface")
code = code.replace("tint = Color.Black", "tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface")

# Gray text
code = code.replace("color = Color.Gray", "color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant")
code = code.replace("tint = Color.Gray", "tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant")
code = code.replace("color = Color.LightGray", "color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f)")
code = code.replace("color = Color(0xFF333333)", "color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface")
code = code.replace("tint = Color(0xFFBBBBBB)", "tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant")

# Card backgrounds
code = code.replace("containerColor = Color.White", "containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface")
code = code.replace("Color(0xFFF5F5F7)", "androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant")
code = code.replace("Color.White", "androidx.compose.material3.MaterialTheme.colorScheme.surface")

# Buttons (Black -> Primary)
code = code.replace("containerColor = Color.Black", "containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary")
code = code.replace("contentColor = Color.White", "contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary")
code = code.replace("ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)", "ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary)")
code = code.replace("disabledContainerColor = Color(0xFF424242)", "disabledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha=0.5f)")
code = code.replace("disabledContentColor = Color.White", "disabledContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.copy(alpha=0.5f)")
code = code.replace("Color(0xFF16A34A)", "androidx.compose.material3.MaterialTheme.colorScheme.primary")

# Error red
code = code.replace("Color(0xFFEF4444)", "androidx.compose.material3.MaterialTheme.colorScheme.error")
code = code.replace("Color(0xFF991B1B)", "androidx.compose.material3.MaterialTheme.colorScheme.error")

# Borders
code = code.replace("Color(0xFFDDDDDD)", "androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant")
code = code.replace("Color(0xFFE5E5E5)", "androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant")
code = code.replace("Color(0xFFEEEEEE)", "androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant")
code = code.replace("Color(0xFFF0F0F0)", "androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant")

# Fix fields
code = code.replace("focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface", "focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary")
code = code.replace("focusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface", "focusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface")
code = code.replace("unfocusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface", "unfocusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface")

# The overlay for image (needs to be dark regardless of theme)
code = code.replace("Color.Black.copy(alpha = 0.45f)", "androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f)")
code = code.replace("background(Color.Black)", "background(androidx.compose.ui.graphics.Color.Black)")
code = code.replace("border(2.dp, Color.White", "border(2.dp, androidx.compose.ui.graphics.Color.White")
code = code.replace("tint = Color.White", "tint = androidx.compose.ui.graphics.Color.White")

with open('app/src/main/java/com/app/re/ui/screens/EditScreen.kt', 'w') as f:
    f.write(code)

print("Done replacing.")
