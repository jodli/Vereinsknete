package de.yogaknete.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val YogaDarkColorScheme = darkColorScheme(
    primary = YogaPurple80,
    secondary = YogaPurpleGrey80,
    tertiary = YogaAccent80,
    background = YogaOnSurface,
    surface = YogaOnSurface
)

private val YogaLightColorScheme = lightColorScheme(
    primary = YogaPurple40,
    secondary = YogaPurpleGrey40,
    tertiary = YogaAccent40,
    background = YogaBackground,
    surface = YogaSurface,
    onSurface = YogaOnSurface
)

@Composable
fun YogaKneteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Force our yoga colors instead of dynamic colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> YogaDarkColorScheme
        else -> YogaLightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
