package it.maestro.app.feature.firstlaunch.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import it.maestro.app.domain.story.StoryDirector
import it.maestro.app.feature.season.ui.StoryScenePlayer

/**
 * Il primo avvio sopra la Home (task F1G-01, F1G-16b): finché [firstLaunch] è `true` mostra il saluto del Maestro; con
 * «So già giocare» mostra la scena di `guide_skip_scene*` e da lì [onGoToGigi], che segna la mano guidata come saltata
 * (il capitolo 1 è fatto) e porta da Gigi. La scena resta anche dopo che [firstLaunch] torna `false`.
 */
@Composable
public fun FirstLaunchHost(
    firstLaunch: Boolean,
    playerName: String?,
    nameAsked: Boolean,
    playerNameMaxLength: Int,
    onNameConfirmed: (String) -> Unit,
    onNameSkipped: () -> Unit,
    onStartHand: () -> Unit,
    onGoToGigi: () -> Unit,
) {
    var skipping by rememberSaveable { mutableStateOf(false) }
    when {
        skipping -> StoryScenePlayer(
            StoryDirector.GUIDE_SKIP,
            playerName = null,
            onDone = { skipping = false; onGoToGigi() },
        )
        firstLaunch -> FirstLaunchScreen(
            playerName = playerName,
            nameAsked = nameAsked,
            playerNameMaxLength = playerNameMaxLength,
            onNameConfirmed = onNameConfirmed,
            onNameSkipped = onNameSkipped,
            onStartHand = onStartHand,
            onAlreadyKnow = { skipping = true },
        )
    }
}
