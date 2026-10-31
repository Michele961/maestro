package it.maestro.app.feature.firstlaunch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroDialogueBox
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.theme.ButtonAmberBottom
import it.maestro.app.feature.home.ui.PlayerNameDialog

/**
 * Il primo avvio (task F1G-01): il Maestro saluta, chiede il nome (con il dialogo del nome della Home) e offre due
 * strade: la prima mano guidata oppure «So già giocare». Una schermata sola, ferma, senza animazioni; si misura sullo
 * spazio che c'è e scorre se è basso. Testi dalle risorse (ADR-0007).
 *
 * [playerName] è il nome già dato; [nameAsked] dice se la domanda è già stata fatta (chi ha lasciato la mano a metà non
 * se la sente rifare).
 */
@Composable
public fun FirstLaunchScreen(
    playerName: String?,
    nameAsked: Boolean,
    playerNameMaxLength: Int,
    onNameConfirmed: (String) -> Unit,
    onNameSkipped: () -> Unit,
    onStartHand: () -> Unit,
    onAlreadyKnow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var asking by rememberSaveable { mutableStateOf(playerName.isNullOrBlank() && !nameAsked) }
    if (asking) {
        PlayerNameDialog(
            currentName = playerName,
            maxLength = playerNameMaxLength,
            onConfirm = { asking = false; onNameConfirmed(it) },
            onDismiss = { asking = false; onNameSkipped() },
        )
    }
    ClubBackground(modifier = modifier.testTag("first_launch_screen"), feltFraction = 0.5f) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Il ritratto si misura sullo spazio che c'è: più piccolo sui telefoni bassi.
            val portrait = (maxHeight * 0.26f).coerceIn(80.dp, 150.dp)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FramedPortrait(
                    portrait = R.drawable.maestro_neutral,
                    size = portrait,
                    background = ButtonAmberBottom,
                    contentDescription = stringResource(R.string.home_maestro_title),
                )
                val greeting = stringResource(R.string.first_launch_greeting)
                val message = when {
                    asking -> greeting + "\n\n" + stringResource(R.string.first_launch_ask_name)
                    !playerName.isNullOrBlank() -> stringResource(R.string.first_launch_name_given, playerName)
                    else -> stringResource(R.string.first_launch_name_skipped)
                }
                MaestroDialogueBox(
                    title = stringResource(R.string.home_maestro_title),
                    message = message,
                    modifier = Modifier.testTag("first_launch_message"),
                )
                if (!asking) {
                    MaestroPillButton(
                        text = stringResource(R.string.first_launch_start_button),
                        onClick = onStartHand,
                        variant = PillButtonVariant.HERO_RED,
                        height = 56.dp,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "first_launch_start_button",
                    )
                    MaestroPillButton(
                        text = stringResource(R.string.first_launch_skip_button),
                        onClick = onAlreadyKnow,
                        variant = PillButtonVariant.SECONDARY_AMBER,
                        height = 44.dp,
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        testTag = "first_launch_skip_button",
                    )
                }
            }
        }
    }
}
