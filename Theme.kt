package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FixaColorScheme = darkColorScheme(
    primary = FixaPrimary,
    onPrimary = FixaOnPrimary,
    primaryContainer = FixaPrimaryContainer,
    onPrimaryContainer = FixaOnPrimaryContainer,
    secondary = FixaSecondary,
    onSecondary = FixaOnSecondary,
    secondaryContainer = FixaSecondaryContainer,
    onSecondaryContainer = FixaOnSecondaryContainer,
    tertiary = FixaTertiary,
    onTertiary = FixaOnTertiary,
    tertiaryContainer = FixaTertiaryContainer,
    onTertiaryContainer = FixaOnTertiaryContainer,
    background = FixaBackground,
    onBackground = FixaOnBackground,
    surface = FixaSurface,
    onSurface = FixaOnSurface,
    surfaceVariant = FixaSurfaceVariant,
    onSurfaceVariant = FixaOnSurfaceVariant,
    outline = FixaOutline,
    error = FixaError
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = FixaBackground.toArgb()
                window.navigationBarColor = FixaBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = FixaColorScheme,
        typography = Typography,
        content = content
    )
}
