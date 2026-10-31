package it.maestro.app.designsystem.components

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import it.maestro.app.designsystem.theme.ButtonGoldBorder
import it.maestro.app.designsystem.theme.ButtonGreenBottom
import it.maestro.app.designsystem.theme.WoodSurfaceLight

private val SwitchOnThumb = Color(0xFFFFF4D6)
private val SwitchOffTrack = Color(0xFFE3D3B0)
private val SwitchOffBorder = Color(0xFF8B6B3E)

/**
 * Interruttore del design system (task F1G-17): acceso verde del gioco con pallino crema e bordo oro, spento beige con
 * pallino e bordo di legno. Contrasto leggibile su pergamena e su fondo chiaro; stesso comportamento di [Switch].
 */
@Composable
public fun MaestroSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = SwitchOnThumb,
            checkedTrackColor = ButtonGreenBottom,
            checkedBorderColor = ButtonGoldBorder,
            uncheckedThumbColor = WoodSurfaceLight,
            uncheckedTrackColor = SwitchOffTrack,
            uncheckedBorderColor = SwitchOffBorder,
            disabledCheckedThumbColor = SwitchOnThumb.copy(alpha = 0.7f),
            disabledCheckedTrackColor = ButtonGreenBottom.copy(alpha = 0.4f),
            disabledCheckedBorderColor = ButtonGoldBorder.copy(alpha = 0.4f),
            disabledUncheckedThumbColor = WoodSurfaceLight.copy(alpha = 0.4f),
            disabledUncheckedTrackColor = SwitchOffTrack.copy(alpha = 0.5f),
            disabledUncheckedBorderColor = SwitchOffBorder.copy(alpha = 0.4f),
        ),
    )
}
