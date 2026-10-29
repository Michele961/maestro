package it.maestro.app.feature.review.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.maestro.app.R
import it.maestro.app.designsystem.components.TactileSegmentedPill
import it.maestro.app.feature.review.presentation.ReviewStage

/**
 * I tasti per rivedere la scena (`docs/app/features/review.md` §3): la mossa giocata, la migliore (quando la soluzione
 * è svelata) e il rallentatore. Il tavolo parte fermo sulla posizione, così la mossa si può guardare quando si vuole.
 */
@Composable
internal fun ReviewReplayControls(stage: ReviewStage, step: ReviewStep, showBest: Boolean, modifier: Modifier = Modifier) {
    var slow by remember(stage) { mutableStateOf(stage.slow) }
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TactileSegmentedPill(
            text = stringResource(id = R.string.review_replay_played),
            selected = false,
            onClick = { stage.showPlayed(step) },
            modifier = Modifier.weight(1f).height(44.dp).testTag("review_replay_played"),
        )
        if (showBest) {
            TactileSegmentedPill(
                text = stringResource(id = R.string.review_replay_best),
                selected = false,
                onClick = { stage.showBest(step) },
                modifier = Modifier.weight(1f).height(44.dp).testTag("review_replay_best"),
            )
        }
        TactileSegmentedPill(
            text = stringResource(id = R.string.review_replay_slow),
            selected = slow,
            onClick = {
                slow = !slow
                stage.slow = slow
            },
            modifier = Modifier.weight(1f).height(44.dp).testTag("review_replay_slow"),
        )
    }
}
