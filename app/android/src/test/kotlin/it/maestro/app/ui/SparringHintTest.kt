package it.maestro.app.ui

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.ai.bot.PimcEvaluator
import it.maestro.ai.bot.SearchBudget
import it.maestro.app.feature.table.presentation.GameEffect
import it.maestro.app.feature.table.presentation.GameIntent
import it.maestro.app.feature.table.presentation.GameReducer
import it.maestro.app.feature.table.presentation.GameViewModel
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test per il suggerimento su richiesta in Sparring (task F1-11, `docs/vision.md` §4, decisione D9):
 * - Criterio di accettazione: Il suggerimento usa solo viewFor() del giocatore umano.
 * - La mossa suggerita è una mossa legale per il giocatore umano.
 * - Il suggerimento viene resettato dopo aver effettuato una giocata.
 * - Solo in modalità Sparring il pulsante/intento produce l'effetto di calcolo.
 */
class SparringHintTest {

    @Test
    fun hintUsesExclusivelyViewForHumanPlayerAndNeverExposesOpponentSecretHand() {
        var state = newMatch(config = ScopaConfig(targetScore = 11), seed = 12345L)
        // Se non è il turno di P1, avanziamo finché non tocca al giocatore umano
        while (state.toMove != Seat.P1) {
            val moves = legalMoves(state)
            val result = play(state, moves.first())
            state = (result as PlayResult.Applied).state
        }
        val humanSeat = Seat.P1

        // 1. Costruiamo la vista autorizzata per il giocatore umano
        val humanView = viewFor(state, humanSeat)
        val legal = legalMoves(state)

        // 2. Valutiamo le mosse con PimcEvaluator passando rigorosamente la vista del giocatore umano
        val evaluations = PimcEvaluator.evaluate(
            view = humanView,
            legal = legal,
            rng = SeededRng(42L),
            budget = SearchBudget(samples = 10, maxNodes = 40),
        )

        assertFalse(evaluations.isEmpty(), "Il valutatore deve restituire almeno una mossa valutata")
        val bestMove = evaluations.maxByOrNull { it.value }!!.move

        // La mossa consigliata deve essere una mossa legale
        assertTrue(bestMove in legal, "La mossa suggerita $bestMove deve essere legale")

        // La carta da giocare deve appartenere alla mano del giocatore umano
        assertTrue(bestMove.card in humanView.hand, "La carta suggerita deve essere nella mano del giocatore umano")

        // La carta non deve MAI appartenere alla mano avversaria segreta
        val opponentHand = state.hand(Seat.P2)
        assertFalse(
            bestMove.card in opponentHand,
            "La carta suggerita non può provenire dalla mano avversaria segreta",
        )

        // Tutte le carte della presa devono essere attualmente presenti sul tavolo
        assertTrue(
            humanView.table.containsAll(bestMove.capture),
            "Tutte le carte prese devono essere sul tavolo visibile",
        )
    }

    @Test
    fun requestingHintInSparringModeComputesAndAppliesSuggestedMove() = runBlocking {
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = GameViewModel(
            ioDispatcher = Dispatchers.Unconfined,
            coroutineScope = testScope,
        )

        viewModel.onIntent(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 777L,
                humanSeat = Seat.P1,
            ),
        )

        val initialState = viewModel.uiState.value
        assertEquals(Seat.P1, initialState.humanSeat)
        assertTrue(initialState.isHumanTurn)
        assertTrue(initialState.isSparring)
        assertNull(initialState.suggestedMove)
        assertFalse(initialState.isHintLoading)

        // Richiedi suggerimento al Maestro
        viewModel.onIntent(GameIntent.RequestHint)

        val hintState = viewModel.uiState.value
        assertNotNull(hintState.suggestedMove, "Lo stato UI deve contenere la mossa suggerita dal Maestro")
        assertFalse(hintState.isHintLoading)

        // La mossa suggerita deve essere nella mano del giocatore umano
        val suggestedCard = hintState.suggestedMove!!.card
        assertTrue(suggestedCard in hintState.humanHand)

        // Quando il giocatore gioca una carta, il suggerimento deve essere cancellato
        viewModel.onIntent(GameIntent.PlayCard(suggestedCard))

        val afterPlayState = viewModel.uiState.value
        assertNull(afterPlayState.suggestedMove, "Il suggerimento deve essere azzerato dopo aver giocato")
    }

    @Test
    fun reducerIgnoresHintWhenNotSparringOrNotHumanTurn() {
        val initialResult = GameReducer.initial(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 42L,
                humanSeat = Seat.P1,
            ),
        )

        // Se disabilitiamo isSparring, RequestHint non deve produrre effetti
        val nonSparringState = initialResult.state.copy(isSparring = false)
        val nonSparringResult = GameReducer.reduce(nonSparringState, GameIntent.RequestHint)
        assertNull(nonSparringResult.effect, "In modalità non sparring non deve essere emesso ComputeHint")
        assertNull(nonSparringResult.state.suggestedMove)

        // Se non è il turno del giocatore umano, RequestHint non produce effetti
        val notMyTurnState = initialResult.state.copy(
            scopaState = initialResult.state.scopaState.copy(toMove = Seat.P2),
        )
        val notMyTurnResult = GameReducer.reduce(notMyTurnState, GameIntent.RequestHint)
        assertNull(notMyTurnResult.effect, "Quando non è il proprio turno non deve essere emesso ComputeHint")
    }
}
