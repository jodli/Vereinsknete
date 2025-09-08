package de.yogaknete.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = YogaPurple,
    onPrimary = YogaPurple99,
    primaryContainer = YogaPurple90,
    onPrimaryContainer = YogaPurple10,
    secondary = YogaGold,
    onSecondary = YogaPurple99,
    background = YogaBackground,
    onBackground = YogaTextPrimary,
    surface = YogaSurface,
    onSurface = YogaTextPrimary,
    surfaceVariant = YogaPurpleSurface,
    onSurfaceVariant = YogaTextSecondary,
    error = YogaCoral
)

private val DarkColorScheme = darkColorScheme(
    primary = YogaPurple60,
    onPrimary = YogaPurple20,
    primaryContainer = YogaPurple30,
    onPrimaryContainer = YogaPurple90,
    secondary = YogaGold,
    onSecondary = YogaPurple20,
    background = YogaPurple10,
    onBackground = YogaPurple90,
    surface = YogaPurple20,
    onSurface = YogaPurple90,
    surfaceVariant = YogaPurple30,
    onSurfaceVariant = YogaPurple80,
    error = YogaCoral
)

@Composable
fun YogaKneteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
