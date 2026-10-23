package it.maestro.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette base iniziale per Maestro (verde tavolo da gioco e oro napoletano)
public val GreenPrimary: Color = Color(0xFF1B5E20)
public val GreenPrimaryDark: Color = Color(0xFF0D3311)
public val GoldAccent: Color = Color(0xFFFFD700)
public val SurfaceLight: Color = Color(0xFFFBFBFB)
public val SurfaceDark: Color = Color(0xFF121212)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GoldAccent,
    background = SurfaceLight,
    surface = SurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    secondary = GoldAccent,
    background = SurfaceDark,
    surface = SurfaceDark,
)

/**
 * Tema Material3 di default per l'applicazione Maestro.
 */
@Composable
public fun MaestroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
