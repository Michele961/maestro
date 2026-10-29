package it.maestro.app.feature.review.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.maestro.app.R
import it.maestro.app.designsystem.components.TactileSegmentedPill
import it.maestro.app.feature.review.presentation.ReviewStage

/**
 * I tasti per rivedere la scena (`docs/app/features/review.md` §3): la mossa giocata e, quando la soluzione è visibile,
 * la migliore. Sono gli stessi con due scelte e con più scelte. Il tavolo parte fermo sulla posizione, così la mossa si
 * può guardare quando si vuole.
 */
@Composable
internal fun ReviewReplayControls(stage: ReviewStage, step: ReviewStep, showBest: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TactileSegmentedPill(
            text = stringResource(id = R.string.review_replay_played),
            selected = false,
            onClick = { stage.showPlayed(step) },
            modifier = Modifier.weight(1f).heightIn(min = 56.dp).testTag("review_replay_played"),
        )
        if (showBest) {
            TactileSegmentedPill(
                text = stringResource(id = R.string.review_replay_best),
                selected = false,
                onClick = { stage.showBest(step) },
                modifier = Modifier.weight(1f).heightIn(min = 56.dp).testTag("review_replay_best"),
            )
        }
    }
}
