package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ArcadeColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = NeonOrange,
    onSecondary = Color.Black,
    tertiary = NeonGold,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = SpaceSurface,
    onSurface = TextPrimary
)

@Composable
fun AstroStrikeTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = ArcadeColorScheme,
        typography = Typography,
        content = content
    )
}
