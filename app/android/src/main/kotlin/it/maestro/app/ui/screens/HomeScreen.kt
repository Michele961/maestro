package it.maestro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.ui.components.MaestroAvatarView
import it.maestro.app.ui.components.MaestroDialogueBox
import it.maestro.app.ui.components.MaestroMood
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.theme.ButtonGoldBorder
import it.maestro.app.ui.theme.ModalBackgroundWarm
import it.maestro.app.ui.theme.WoodSurfaceDark
import it.maestro.app.ui.theme.WoodSurfaceLight

/**
 * Schermata iniziale di Maestro (Guida di Stile v1.0, stile "Circolo Tradizionale").
 * Tutti i testi visibili provengono rigorosamente dalle risorse stringhe (ADR-0007).
 */
@Composable
public fun HomeScreen(
    modifier: Modifier = Modifier,
    hasActiveMatch: Boolean = false,
    onResumeGameClicked: () -> Unit = {},
    onStartGameClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
) {
    val woodGradient = Brush.verticalGradient(
        listOf(WoodSurfaceLight, WoodSurfaceDark),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(woodGradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header / Titolo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.home_welcome_title),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = ButtonGoldBorder,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.home_subtitle),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ModalBackgroundWarm.copy(alpha = 0.9f),
                )
            }

            // Pulsanti Tattili Centrali
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (hasActiveMatch) {
                    MaestroPillButton(
                        text = stringResource(id = R.string.home_resume_game),
                        onClick = onResumeGameClicked,
                        variant = PillButtonVariant.PRIMARY_GREEN,
                        modifier = Modifier.fillMaxWidth(0.85f),
                        testTag = "resume_game_button",
                    )
                    MaestroPillButton(
                        text = stringResource(id = R.string.home_start_game),
                        onClick = onStartGameClicked,
                        variant = PillButtonVariant.SECONDARY_AMBER,
                        modifier = Modifier.fillMaxWidth(0.85f),
                        testTag = "new_game_button",
                    )
                } else {
                    MaestroPillButton(
                        text = stringResource(id = R.string.home_start_game),
                        onClick = onStartGameClicked,
                        variant = PillButtonVariant.PRIMARY_GREEN,
                        modifier = Modifier.fillMaxWidth(0.85f),
                        testTag = "new_game_button",
                    )
                }

                MaestroPillButton(
                    text = stringResource(id = R.string.home_settings_button),
                    onClick = onSettingsClicked,
                    variant = PillButtonVariant.WOOD,
                    modifier = Modifier.fillMaxWidth(0.85f),
                    testTag = "home_settings_button",
                )
            }

            // Box del Maestro in basso
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MaestroAvatarView(
                    mood = MaestroMood.NEUTRAL,
                    size = 56.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                MaestroDialogueBox(
                    title = stringResource(id = R.string.game_player_maestro),
                    message = stringResource(id = R.string.home_coach_welcome),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
