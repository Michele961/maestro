package it.maestro.app.feature.table.ui

import androidx.compose.runtime.Composable
import it.maestro.app.R
import it.maestro.app.domain.guide.GuidedHandStatus
import it.maestro.app.domain.story.StoryDirector
import it.maestro.app.feature.season.ui.StoryScenePlayer
import it.maestro.app.feature.table.presentation.GuideUi

/**
 * Le scene della mano d'esordio guidata (F1G-02) sopra il tavolo: l'introduzione prima della mano, il saluto quando la
 * mano finisce, la scena di «Salta la guida». Le battute durante la mano stanno nel fumetto del Maestro, non qui.
 *
 * [onFinished] riceve come è andata la mano: la shell la ricorda e porta da Gigi.
 */
@Composable
internal fun GuideOverlay(
    guide: GuideUi,
    skipping: Boolean,
    onSkipRequested: () -> Unit,
    onIntroDone: () -> Unit,
    onFinished: (GuidedHandStatus) -> Unit,
) {
    when {
        skipping -> StoryScenePlayer(StoryDirector.GUIDE_SKIP, playerName = null, onDone = { onFinished(GuidedHandStatus.SKIPPED) })
        guide.finished -> StoryScenePlayer(StoryDirector.GUIDE_END, playerName = null, onDone = { onFinished(GuidedHandStatus.DONE) })
        guide.intro -> StoryScenePlayer(
            StoryDirector.GUIDE_INTRO,
            playerName = null,
            onDone = onIntroDone,
            onSkip = onSkipRequested,
            skipLabel = R.string.guide_skip_button,
        )
    }
}
