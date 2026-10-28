package it.maestro.app.feature.review.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.app.feature.review.presentation.RetryFeedback
import it.maestro.app.feature.review.presentation.isMistake
import it.maestro.app.feature.review.presentation.ReviewStage
import it.maestro.app.feature.table.ui.StageBoard
import it.maestro.app.feature.table.ui.StageModel
import it.maestro.app.feature.table.ui.TableGeometry
import it.maestro.game.model.Card as GameCard
import it.maestro.game.model.Seat

private val GoldAccent = Color(0xFFD4AF37)

/**
 * Il tavolo della revisione (`docs/app/features/review.md` §3): lo stesso del gioco, pilotato da [stage]. Mostra
 * solo ciò che il giocatore sapeva (la mano avversaria è sempre coperta) e mette in scena la mossa giocata e, a
 * richiesta, quella migliore. Le carte della mano si giocano solo quando si prova la mano ([canPlayCards]).
 */
@Composable
internal fun ReviewBoard(
    stage: ReviewStage,
    step: ReviewStep,
    humanSeat: Seat,
    isRetryMode: Boolean,
    isSolutionVisible: Boolean,
    retryFeedback: RetryFeedback?,
    onPlayCard: (GameCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scene by stage.scene.collectAsStateWithLifecycle()
    val model = remember(scene) { StageModel.from(scene) }
    val isHumanMistake = step.mover == humanSeat && step.analysis.isMistake()
    val canPlayCards = !scene.busy && (isHumanMistake && !isSolutionVisible || isRetryMode && retryFeedback !is RetryFeedback.Correct)
    val showBest = !scene.busy && isHumanMistake && (isSolutionVisible || retryFeedback is RetryFeedback.Correct)
    val best = step.analysis.bestMove

    Card(
        modifier = modifier.fillMaxWidth().testTag("review_board"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E4D2B)),
        border = BorderStroke(4.dp, Brush.verticalGradient(listOf(Color(0xFFFFE082), GoldAccent, Color(0xFF8C6D1F)))),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val geometry = remember(maxWidth, maxHeight) { TableGeometry.from(maxWidth - 16.dp, maxHeight - 16.dp) }
            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                StageBoard(
                    model = model,
                    geometry = geometry,
                    canPlay = canPlayCards,
                    atRest = !scene.busy,
                    stepKey = scene.step,
                    stepMillis = scene.stepMillis.toInt(),
                    opponentSweepCards = emptyList(),
                    humanSweepCards = emptyList(),
                    suggestedCapture = if (showBest) best.capture.toSet() else emptySet(),
                    suggestedCard = if (showBest) best.card else null,
                    onPlay = onPlayCard,
                )
                if (scene.busy) {
                    // Un tocco salta la scena, come al tavolo.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = stage::skip)
                            .testTag("review_skip_layer"),
                    )
                }
            }
        }
    }
}
