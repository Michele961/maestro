package it.maestro.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette ufficiale Maestro v1.0 (Guida di Stile, Circolo Italiano Tradizionale)
public val TableFeltGreenLight: Color = Color(0xFF1E4D2B)
public val TableFeltGreenDark: Color = Color(0xFF14381E)
public val WoodSurfaceDark: Color = Color(0xFF3B2213)
public val WoodSurfaceLight: Color = Color(0xFF5A3821)
public val ModalBackgroundWarm: Color = Color(0xFFFDFBF7)

// Pulsanti Principali
public val ButtonGreenTop: Color = Color(0xFF2E8B57)
public val ButtonGreenBottom: Color = Color(0xFF1F683D)
public val ButtonGoldBorder: Color = Color(0xFFD4AF37)

public val ButtonBlueTop: Color = Color(0xFF1E5A8A)
public val ButtonBlueBottom: Color = Color(0xFF164366)

public val ButtonAmberTop: Color = Color(0xFFA05A2C)
public val ButtonAmberBottom: Color = Color(0xFF7D3F19)

// Metriche e Segnalazioni Game Review
public val GradeMagistraleBlue: Color = Color(0xFF1E88E5)
public val GradeNaturaleGreen: Color = Color(0xFF43A047)
public val GradeImprecisioneYellow: Color = Color(0xFFFBC02D)
public val GradeErroreGraveRed: Color = Color(0xFFD32F2F)

// Carte da Gioco (Cartoncino bianco puro e dorso verde bottiglia)
public val CardPaperWhite: Color = Color(0xFFFFFFFF)
public val CardBackGreen: Color = Color(0xFF1B4D2B)
public val CardBackGold: Color = Color(0xFFD4AF37)

// Colori legacy compatibili
public val GreenPrimary: Color = ButtonGreenBottom
public val GreenPrimaryDark: Color = TableFeltGreenDark
public val GoldAccent: Color = ButtonGoldBorder
public val SurfaceLight: Color = ModalBackgroundWarm
public val SurfaceDark: Color = WoodSurfaceDark

private val LightColorScheme = lightColorScheme(
    primary = ButtonGreenBottom,
    secondary = ButtonGoldBorder,
    background = ModalBackgroundWarm,
    surface = ModalBackgroundWarm,
)

private val DarkColorScheme = darkColorScheme(
    primary = TableFeltGreenDark,
    secondary = ButtonGoldBorder,
    background = WoodSurfaceDark,
    surface = WoodSurfaceDark,
)

/**
 * Tema Material3 per l'applicazione Maestro ispirato al circolo tradizionale italiano (Guida v1.0).
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
