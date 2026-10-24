package it.maestro.app.ui

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.game.GameIntent
import it.maestro.app.game.GameViewModel
import it.maestro.app.game.UiGameEvent
import it.maestro.game.model.Seat
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScoreCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test per il flusso completo della partita (task F1-07):
 * - Una partita completa contro ciascun livello si gioca dall'inizio alla fine (test UI su seed fisso).
 * - Il riepilogo di smazzata mostra le stesse voci e gli stessi valori dell'evento DealEnded.
 * - Flusso di rivincita dopo il termine della partita.
 */
class FullMatchUiTest {

    @Test
    fun completeMatchAgainstEachDifficultyLevelPlaysFromStartToFinish() {
        for (level in DifficultyLevel.entries) {
            val testScope = CoroutineScope(Dispatchers.Unconfined)
            val viewModel = GameViewModel(
                ioDispatcher = Dispatchers.Unconfined,
                coroutineScope = testScope,
            )

            // Avvio partita con targetScore = 11 su seed fisso (42L)
            viewModel.onIntent(
                GameIntent.StartMatch(
                    config = ScopaConfig(targetScore = 11),
                    level = level,
                    seed = 42L,
                    humanSeat = Seat.P1,
                ),
            )

            var safetyCounter = 0
            val maxTurns = 500

            while (viewModel.uiState.value.phase != DealPhase.MATCH_OVER && safetyCounter < maxTurns) {
                safetyCounter++
                val state = viewModel.uiState.value

                // 1. Se c'è un riepilogo smazzata aperto, lo verifichiamo e proseguiamo
                if (state.pendingDealScore != null) {
                    val lastEvent = state.lastEvent
                    assertTrue(lastEvent is UiGameEvent.DealEnded, "L'evento deve essere DealEnded")
                    assertEquals(state.pendingDealScore, lastEvent.score)
                    viewModel.onIntent(GameIntent.DismissDealSummary)
                    continue
                }

                // 2. Se c'è una scelta multipla pendente, confermiamo la prima opzione
                if (state.pendingCaptureChoice != null) {
                    val choice = state.pendingCaptureChoice!!
                    viewModel.onIntent(
                        GameIntent.SelectCapture(
                            card = choice.card,
                            capture = choice.options.first(),
                        ),
                    )
                    continue
                }

                // 3. Se è il turno dell'umano, gioca la prima carta disponibile
                if (state.isHumanTurn) {
                    assertTrue(state.humanHand.isNotEmpty(), "La mano umana non deve essere vuota durante il proprio turno")
                    viewModel.onIntent(GameIntent.PlayCard(state.humanHand.first()))
                    continue
                }
            }

            assertTrue(
                safetyCounter < maxTurns,
                "La partita contro ${level.name} non è terminata entro $maxTurns turni!",
            )

            val finalState = viewModel.uiState.value
            assertEquals(DealPhase.MATCH_OVER, finalState.phase, "La partita contro ${level.name} deve essere MATCH_OVER")
            assertTrue(
                finalState.humanScore >= 11 || finalState.opponentScore >= 11,
                "Almeno un giocatore deve aver raggiunto il punteggio obiettivo 11 (P1: ${finalState.humanScore}, P2: ${finalState.opponentScore})",
            )
            assertTrue(
                finalState.humanScore != finalState.opponentScore,
                "A MATCH_OVER i punteggi non possono essere pari",
            )
        }
    }

    @Test
    fun dealSummaryDisplaysExactDealEndedCategoriesAndScores() {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )

        viewModel.onIntent(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 42L,
                humanSeat = Seat.P1,
            ),
        )

        var dealEndedReached = false
        var safetyCounter = 0
        while (!dealEndedReached && safetyCounter < 100) {
            safetyCounter++
            val state = viewModel.uiState.value

            if (state.pendingDealScore != null) {
                dealEndedReached = true
                val dealScore = state.pendingDealScore!!
                val lastEvent = state.lastEvent
                assertTrue(lastEvent is UiGameEvent.DealEnded, "L'ultimo evento deve essere DealEnded")
                val eventScore = lastEvent.score

                // Verifica che le voci e i valori coincidano rigorosamente con DealEnded (criterio F1-07)
                assertEquals(eventScore, dealScore)

                val humanSeat = state.humanSeat
                val oppSeat = humanSeat.other()

                // Verifica categorie standard
                assertEquals(eventScore.points(humanSeat, ScoreCategory.CARTE), dealScore.points(humanSeat, ScoreCategory.CARTE))
                assertEquals(eventScore.points(oppSeat, ScoreCategory.CARTE), dealScore.points(oppSeat, ScoreCategory.CARTE))

                assertEquals(eventScore.points(humanSeat, ScoreCategory.DENARI), dealScore.points(humanSeat, ScoreCategory.DENARI))
                assertEquals(eventScore.points(oppSeat, ScoreCategory.DENARI), dealScore.points(oppSeat, ScoreCategory.DENARI))

                assertEquals(eventScore.points(humanSeat, ScoreCategory.SETTEBELLO), dealScore.points(humanSeat, ScoreCategory.SETTEBELLO))
                assertEquals(eventScore.points(oppSeat, ScoreCategory.SETTEBELLO), dealScore.points(oppSeat, ScoreCategory.SETTEBELLO))

                assertEquals(eventScore.points(humanSeat, ScoreCategory.PRIMIERA), dealScore.points(humanSeat, ScoreCategory.PRIMIERA))
                assertEquals(eventScore.points(oppSeat, ScoreCategory.PRIMIERA), dealScore.points(oppSeat, ScoreCategory.PRIMIERA))

                assertEquals(eventScore.points(humanSeat, ScoreCategory.SCOPE), dealScore.points(humanSeat, ScoreCategory.SCOPE))
                assertEquals(eventScore.points(oppSeat, ScoreCategory.SCOPE), dealScore.points(oppSeat, ScoreCategory.SCOPE))

                assertEquals(eventScore.totalPoints(humanSeat), dealScore.totalPoints(humanSeat))
                assertEquals(eventScore.totalPoints(oppSeat), dealScore.totalPoints(oppSeat))
                break
            }

            if (state.pendingCaptureChoice != null) {
                viewModel.onIntent(
                    GameIntent.SelectCapture(
                        card = state.pendingCaptureChoice!!.card,
                        capture = state.pendingCaptureChoice!!.options.first(),
                    ),
                )
                continue
            }

            if (state.isHumanTurn) {
                viewModel.onIntent(GameIntent.PlayCard(state.humanHand.first()))
            }
        }

        assertTrue(dealEndedReached, "La prima smazzata deve concludersi entro 100 mosse")
    }

    @Test
    fun rematchResetsGameAndAllowsPlayingAgain() {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )

        // Simula termine partita e rivincita
        viewModel.onIntent(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 42L,
                humanSeat = Seat.P1,
            ),
        )

        // Giochiamo fino a fine partita
        var safetyCounter = 0
        while (viewModel.uiState.value.phase != DealPhase.MATCH_OVER && safetyCounter < 500) {
            safetyCounter++
            val state = viewModel.uiState.value
            if (state.pendingDealScore != null) {
                viewModel.onIntent(GameIntent.DismissDealSummary)
                continue
            }
            if (state.pendingCaptureChoice != null) {
                viewModel.onIntent(GameIntent.SelectCapture(state.pendingCaptureChoice!!.card, state.pendingCaptureChoice!!.options.first()))
                continue
            }
            if (state.isHumanTurn) {
                viewModel.onIntent(GameIntent.PlayCard(state.humanHand.first()))
            }
        }

        assertEquals(DealPhase.MATCH_OVER, viewModel.uiState.value.phase)

        // Utente preme "Rivincita"
        viewModel.onIntent(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 999L,
                humanSeat = Seat.P1,
            ),
        )

        val rematchedState = viewModel.uiState.value
        assertEquals(DealPhase.PLAYING, rematchedState.phase)
        assertEquals(0, rematchedState.humanScore)
        assertEquals(0, rematchedState.opponentScore)
        assertEquals(3, rematchedState.humanHand.size)
        assertEquals(30, rematchedState.deckCardCount)
    }
}
