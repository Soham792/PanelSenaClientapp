package com.panelsena.client.core.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val PanelSenaClientColors = lightColorScheme(
    background = Color(0xFFF7F6F2),       // Warm off-white
    surface = Color(0xFFFFFFFF),          // Pure white cards
    primary = Color(0xFF1A1A2E),          // Deep navy — primary text, active nav
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF6C63FF),        // Purple accent
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFF5C842),         // Yellow accent
    onTertiary = Color(0xFF1A1A2E),
    onBackground = Color(0xFF1A1A2E),
    onSurface = Color(0xFF1A1A2E),
    outline = Color(0xFFE8E8E8),
)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun PanelSenaClientTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = PanelSenaClientColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = Color(0xFFF7F6F2).toArgb() // Warm off-white
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                windowInsetsController.isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PanelSenaClientTypography,
        shapes = PanelSenaClientShapes,
        content = content
    )
}
