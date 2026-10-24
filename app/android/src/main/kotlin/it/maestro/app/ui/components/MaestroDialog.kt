package it.maestro.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import it.maestro.app.ui.theme.ButtonGreenBottom

/** Inchiostro dei testi su pergamena. */
internal val ParchmentInk: Color = Color(0xFF2B1A10)

/**
 * Dialogo del circolo su pergamena con cornice d'ottone (F1-23): stessi slot di `AlertDialog`
 * (titolo, testo, conferma, annulla), così i dialoghi esistenti cambiano aspetto senza cambiare logica.
 * Testi in inchiostro scuro; i pulsanti Material dentro gli slot prendono il verde del tavolo.
 */
@Composable
public fun MaestroAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        ParchmentPanel(modifier = modifier.fillMaxWidth(0.92f).padding(vertical = 12.dp)) {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    primary = ButtonGreenBottom,
                    onSurface = ParchmentInk,
                    onSurfaceVariant = ParchmentInk,
                ),
                typography = MaterialTheme.typography,
            ) {
                CompositionLocalProvider(LocalContentColor provides ParchmentInk) {
                    if (title != null) {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp), contentAlignment = Alignment.Center) {
                            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineSmall) { title() }
                        }
                    }
                    if (text != null) {
                        Box(modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) { text() }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}

