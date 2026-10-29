package it.maestro.app.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.maestro.app.R

/**
 * Cursore di un volume 0–100: [onChange] a ogni movimento, [onCommitted] quando il dito si stacca.
 * Lo usano Impostazioni e la pausa del tavolo.
 */
@Composable
public fun VolumeSlider(
    title: String,
    volume: Int,
    enabled: Boolean,
    onChange: (Int) -> Unit,
    onCommitted: (Int) -> Unit,
    tag: String,
) {
    var dragging by remember(volume) { mutableFloatStateOf(volume.toFloat()) }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = stringResource(R.string.settings_volume_percent, dragging.toInt()), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B5A4A))
        }
        Slider(
            value = dragging,
            onValueChange = {
                dragging = it
                onChange(it.toInt())
            },
            onValueChangeFinished = { onCommitted(dragging.toInt()) },
            valueRange = 0f..100f,
            enabled = enabled,
            modifier = Modifier.testTag(tag),
        )
    }
}
