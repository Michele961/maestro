package it.maestro.app.game

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.legalMoves
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameReducerTest {

    @Test
    fun initialStateIsPureAndCorrectlyDerivedFromHumanSeat() {
        val result = GameReducer.initial(
            GameIntent.StartMatch(
                config = ScopaConfig(),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 42L,
                humanSeat = Seat.P1,
            ),
        )

        val state = result.state
        assertEquals(Seat.P1, state.humanSeat)
        assertEquals(DifficultyLevel.PRINCIPIANTE, state.botLevel)
        assertEquals(DealPhase.PLAYING, state.scopaState.phase)
        assertEquals(0, state.matchLog.moves.size)
        assertNull(state.pendingCapture)
        assertNull(state.lastError)
        assertNull(state.lastEvent)

        val uiState = state.toUiState()
        assertEquals(3, uiState.humanHand.size)
        assertEquals(4, uiState.tableCards.size)
        assertEquals(3, uiState.opponentCardCount)
        assertEquals(30, uiState.deckCardCount)
        assertEquals(0, uiState.humanCapturedCount)
        assertEquals(0, uiState.opponentCapturedCount)
        assertEquals(0, uiState.humanSweeps)
        assertEquals(0, uiState.opponentSweeps)
        assertEquals(0, uiState.humanScore)
        assertEquals(0, uiState.opponentScore)
    }

    @Test
    fun humanPlayingSingleLegalMoveAppliesMoveAndProducesBotEffectWhenOpponentTurn() {
        val initial = GameReducer.initial(
            GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1),
        )

        // Verifichiamo di chi è il turno iniziale
        val firstMover = initial.state.scopaState.toMove
        val internal = if (firstMover != Seat.P1) {
            // Se inizia il bot, applichiamo prima la mossa del bot
            val botLegal = legalMoves(initial.state.scopaState).first()
            val botRes = GameReducer.reduce(initial.state, GameIntent.ApplyBotMove(botLegal))
            botRes.state
        } else {
            initial.state
        }

        assertEquals(Seat.P1, internal.scopaState.toMove)
        val humanHand = internal.scopaState.hand(Seat.P1)
        val playableCard = humanHand.first { card ->
            legalMoves(internal.scopaState).count { it.card == card } == 1
        }
        val expectedMove = legalMoves(internal.scopaState).first { it.card == playableCard }

        val playResult = GameReducer.reduce(internal, GameIntent.PlayCard(playableCard))
        assertNull(playResult.state.lastError)
        assertEquals(internal.matchLog.moves.size + 1, playResult.state.matchLog.moves.size)
        assertEquals(expectedMove, playResult.state.matchLog.moves.last())

        // Dopo la mossa di P1, tocca al bot: deve essere emesso ScheduleBotMove
        if (playResult.state.scopaState.phase == DealPhase.PLAYING) {
            assertEquals(Seat.P2, playResult.state.scopaState.toMove)
            assertTrue(playResult.state.isBotThinking)
            assertNotNull(playResult.effect)
            assertTrue(playResult.effect is GameEffect.ScheduleBotMove)
            assertEquals(Seat.P2, (playResult.effect as GameEffect.ScheduleBotMove).botSeat)
        }
    }

    @Test
    fun playingCardWithMultipleCaptureOptionsTriggersPendingChoiceWithoutModifyingBoard() {
        val initial = GameReducer.initial(
            GameIntent.StartMatch(seed = 100L, humanSeat = Seat.P1),
        )

        // Troviamo uno stato in cui una carta ha più prese possibili
        // Creiamo uno stato specifico o cerchiamo una giocata con opzioni
        var current = initial.state
        for (seed in 1L..50L) {
            val candidate = GameReducer.initial(GameIntent.StartMatch(seed = seed, humanSeat = Seat.P1)).state
            if (candidate.scopaState.toMove == Seat.P1) {
                val legal = legalMoves(candidate.scopaState)
                val cardWithMultipleOptions = legal.groupBy { it.card }.entries.firstOrNull { it.value.size > 1 }?.key
                if (cardWithMultipleOptions != null) {
                    current = candidate
                    val res = GameReducer.reduce(current, GameIntent.PlayCard(cardWithMultipleOptions))
                    assertNotNull(res.state.pendingCapture)
                    assertEquals(cardWithMultipleOptions, res.state.pendingCapture?.card)
                    assertTrue(res.state.pendingCapture!!.options.size > 1)
                    assertNull(res.effect)
                    assertEquals(current.scopaState, res.state.scopaState)

                    // Verifichiamo annullamento scelta
                    val cancelRes = GameReducer.reduce(res.state, GameIntent.CancelCaptureChoice)
                    assertNull(cancelRes.state.pendingCapture)

                    // Verifichiamo conferma di una delle opzioni
                    val chosenOption = res.state.pendingCapture!!.options.first()
                    val selectRes = GameReducer.reduce(
                        res.state,
                        GameIntent.SelectCapture(cardWithMultipleOptions, chosenOption),
                    )
                    assertNull(selectRes.state.pendingCapture)
                    assertNull(selectRes.state.lastError)
                    assertEquals(1, selectRes.state.matchLog.moves.size)
                    return
                }
            }
        }
    }

    @Test
    fun invalidMoveProducesVisibleErrorAndNeverCrashes() {
        val initial = GameReducer.initial(
            GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1),
        )

        // Tentativo di giocare una carta non presente in mano
        val cardNotInHand = Card(Rank.RE, Suit.BASTONI)
        val invalidCard = if (cardNotInHand in initial.state.toUiState().humanHand) {
            Card(Rank.DUE, Suit.COPPE)
        } else {
            cardNotInHand
        }

        val result = GameReducer.reduce(initial.state, GameIntent.PlayCard(invalidCard))
        assertNotNull(result.state.lastError)
        assertNull(result.effect)
        // La tavola e lo stato restano identici
        assertEquals(initial.state.scopaState, result.state.scopaState)

        // Dismiss error
        val dismissed = GameReducer.reduce(result.state, GameIntent.DismissError)
        assertNull(dismissed.state.lastError)
    }

    @Test
    fun playWhenNotYourTurnProducesErrorAndNeverCrashes() {
        val initial = GameReducer.initial(
            GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1),
        )
        // Se tocca a P2, P1 prova comunque a giocare
        val p2State = if (initial.state.scopaState.toMove == Seat.P2) {
            initial.state
        } else {
            val legal = legalMoves(initial.state.scopaState).first()
            GameReducer.reduce(initial.state, GameIntent.PlayCard(legal.card)).state
        }

        assertEquals(Seat.P2, p2State.scopaState.toMove)
        val dummyCard = Card(Rank.ASSO, Suit.DENARI)
        val res = GameReducer.reduce(p2State, GameIntent.PlayCard(dummyCard))
        assertNotNull(res.state.lastError)
        assertTrue(res.state.lastError!!.contains("Non è il tuo turno"))
        assertEquals(p2State.scopaState, res.state.scopaState)
    }

    @Test
    fun botMoveApplicationUpdatesStateAndAdvancesTurn() {
        val initial = GameReducer.initial(
            GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1),
        )
        val p2State = if (initial.state.scopaState.toMove == Seat.P2) {
            initial.state
        } else {
            val legal = legalMoves(initial.state.scopaState).first()
            GameReducer.reduce(initial.state, GameIntent.PlayCard(legal.card)).state
        }

        assertEquals(Seat.P2, p2State.scopaState.toMove)
        val botLegal = legalMoves(p2State.scopaState).first()
        val afterBot = GameReducer.reduce(p2State, GameIntent.ApplyBotMove(botLegal))

        assertNull(afterBot.state.lastError)
        assertFalse(afterBot.state.isBotThinking)
        assertEquals(1, afterBot.state.matchLog.moves.size)
        assertEquals(botLegal, afterBot.state.matchLog.moves.first())
    }
}
