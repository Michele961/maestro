package it.maestro.game.scopa

import it.maestro.game.CoreGame
import it.maestro.game.model.Card
import it.maestro.game.model.Seat
import it.maestro.game.model.card
import it.maestro.game.model.cards
import it.maestro.game.model.fullDeck
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ScopaViewLogReplayTest {

    @Test
    fun viewForExposesOnlyPlayerHandAndPublicInfoNeverOpponentHandOrDeckAcross500Seeds() {
        for (seed in 0L until 500L) {
            val config = ScopaConfig(
                targetScore = 11,
                redealOnThreeKings = seed % 2L == 0L,
                reBello = seed % 3L == 0L,
            )
            var state = newMatch(config = config, seed = seed)
            var movesInDeal = 0

            // Verifica a ogni turno di una smazzata completa (36 giocate) su 500 seed.
            while (movesInDeal < 36) {
                for (seat in Seat.entries) {
                    assertViewHidesHiddenCardsAndMatchesPublicState(state, seat, seed, movesInDeal)
                }

                val legal = legalMoves(state)
                val applied = play(state, legal.first()) as PlayResult.Applied
                state = applied.state
                movesInDeal++
            }

            // Verifica anche lo stato post-smazzata (inizio nuova smazzata o MATCH_OVER).
            for (seat in Seat.entries) {
                assertViewHidesHiddenCardsAndMatchesPublicState(state, seat, seed, movesInDeal)
            }
        }
    }

    @Test
    fun replayProducesIdenticalFinalStateAndToTextParseIsIdentityAcross500Seeds() {
        for (seed in 0L until 500L) {
            val config = ScopaConfig(
                targetScore = when ((seed % 3L).toInt()) {
                    0 -> 11
                    1 -> 16
                    else -> 21
                },
                redealOnThreeKings = seed % 2L == 0L,
                reBello = seed % 5L == 0L,
            )
            var state = newMatch(config = config, seed = seed)
            val recordedMoves = mutableListOf<ScopaMove>()

            while (state.phase != DealPhase.MATCH_OVER) {
                val move = legalMoves(state).first()
                recordedMoves.add(move)
                val applied = play(state, move) as PlayResult.Applied
                state = applied.state
            }

            val log = MatchLog(
                rulesVersion = CoreGame.RULES_VERSION,
                config = config,
                seed = seed,
                moves = recordedMoves,
            )

            // 1. toText -> parse è un'identità.
            val text = log.toText()
            val parsedLog = MatchLog.parse(text)
            assertEquals(log, parsedLog, "toText -> parse non è un'identità per seed=$seed")
            assertEquals(text, parsedLog.toText(), "Riserializzazione diversa per seed=$seed")

            // 2. replay(log) dà esattamente lo stesso stato finale della partita originale.
            val replayResult = replay(parsedLog)
            assertEquals(ReplayResult.Ok(state = state), replayResult, "replay(log) diverso per seed=$seed")
        }
    }

    @Test
    fun replayReturnsWrongRulesVersionOrIllegalMoveIndexOnInvalidLogs() {
        val config = ScopaConfig()
        val initial = newMatch(config = config, seed = 42L)
        val firstMove = legalMoves(initial).first()
        val afterFirst = (play(initial, firstMove) as PlayResult.Applied).state
        val secondMove = legalMoves(afterFirst).first()

        // 1. rulesVersion diversa -> WrongRulesVersion(found, expected).
        val wrongVersion0 = MatchLog(
            rulesVersion = 0,
            config = config,
            seed = 42L,
            moves = listOf(firstMove),
        )
        assertEquals(
            ReplayResult.WrongRulesVersion(found = 0, expected = CoreGame.RULES_VERSION),
            replay(wrongVersion0),
        )

        val wrongVersion99 = MatchLog(
            rulesVersion = 99,
            config = config,
            seed = 42L,
            moves = listOf(firstMove),
        )
        assertEquals(
            ReplayResult.WrongRulesVersion(found = 99, expected = CoreGame.RULES_VERSION),
            replay(wrongVersion99),
        )

        // 2. Mossa illegale all'indice 0 (carta in mano all'avversario, non a chi muove).
        val opponentCard = initial.hand(initial.toMove.other()).first()
        val illegalAt0 = MatchLog(
            rulesVersion = CoreGame.RULES_VERSION,
            config = config,
            seed = 42L,
            moves = listOf(ScopaMove(card = opponentCard, capture = emptySet()), secondMove),
        )
        assertEquals(ReplayResult.IllegalMove(index = 0), replay(illegalAt0))

        // 3. Mossa illegale all'indice 2 (dopo due mosse valide, rigioca la stessa prima mossa).
        val illegalAt2 = MatchLog(
            rulesVersion = CoreGame.RULES_VERSION,
            config = config,
            seed = 42L,
            moves = listOf(firstMove, secondMove, firstMove),
        )
        assertEquals(ReplayResult.IllegalMove(index = 2), replay(illegalAt2))
    }

    @Test
    fun matchLogParseRejectsUnknownLinesAndMalformedInputs() {
        // Esempio valido dalla specifica del task F0-07.
        val validSample = """
            maestro-log 1
            rules 1
            config targetScore=11 redealOnThreeKings=true reBello=false
            seed 42
            move 7D AD,6C
            move 3S -
        """.trimIndent()

        val parsed = MatchLog.parse(validSample)
        assertEquals(1, parsed.rulesVersion)
        assertEquals(ScopaConfig(targetScore = 11, redealOnThreeKings = true, reBello = false), parsed.config)
        assertEquals(42L, parsed.seed)
        assertEquals(
            listOf(
                ScopaMove(card = card("7D"), capture = cards("AD", "6C").toSet()),
                ScopaMove(card = card("3S"), capture = emptySet()),
            ),
            parsed.moves,
        )
        assertEquals(validSample, parsed.toText())

        // Accetta anche un newline finale.
        assertEquals(parsed, MatchLog.parse("$validSample\n"))

        // Rifiuta testo vuoto.
        assertFailsWith<IllegalArgumentException> { MatchLog.parse("") }

        // Rifiuta header sbagliato o versione formato diversa.
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse(validSample.replace("maestro-log 1", "maestro-log 2"))
        }

        // Rifiuta riga sconosciuta in coda o nel mezzo.
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse("$validSample\ncomment hello")
        }
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse("$validSample\n\nmove 2D -")
        }

        // Rifiuta chiave di config sconosciuta o targetScore non ammesso.
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse(validSample.replace("reBello=false", "assoPigliatutto=true"))
        }
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse(validSample.replace("targetScore=11", "targetScore=15"))
        }
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse(validSample.replace("reBello=false", "reBello=maybe"))
        }

        // Rifiuta seed o rules non numerici.
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse(validSample.replace("rules 1", "rules abc"))
        }
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse(validSample.replace("seed 42", "seed notanumber"))
        }

        // Rifiuta notazione carta invalida o presa con duplicati / token vuoti.
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse(validSample.replace("move 3S -", "move 8D -"))
        }
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse(validSample.replace("move 7D AD,6C", "move 7D AD,AD"))
        }
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse(validSample.replace("move 7D AD,6C", "move 7D AD,"))
        }
        assertFailsWith<IllegalArgumentException> {
            MatchLog.parse(validSample.replace("move 3S -", "move 3S"))
        }
    }

    @Test
    fun determinizeWithTrueHiddenCardsReconstructsEquivalentStateAcross500Seeds() {
        for (seed in 0L until 500L) {
            var state = newMatch(config = ScopaConfig(), seed = seed)
            var turn = 0

            // Verifica durante l'intera prima smazzata (tutti i 36 turni) per i primi 50 seed,
            // e nei turni chiave (inizio, metà, ultima distribuzione) per tutti i 500 seed.
            val checkEveryTurn = seed < 50L
            while (turn < 36) {
                if (checkEveryTurn || turn == 0 || turn == 15 || turn == 30 || turn == 35) {
                    for (seat in Seat.entries) {
                        val view = viewFor(state, seat)
                        val trueOpponentHand = state.hand(seat.other())
                        val trueDeck = state.deck

                        val reconstructed = determinize(
                            view = view,
                            opponentHand = trueOpponentHand,
                            deckOrder = trueDeck,
                        )

                        // Stato equivalente all'originale (eccetto rngState, che è segreto e vale 0L).
                        assertEquals(
                            state.copy(rngState = 0L),
                            reconstructed,
                            "Stato ricostruito non equivalente per seed=$seed, turn=$turn, seat=$seat",
                        )
                        assertEquals(
                            legalMoves(state),
                            legalMoves(reconstructed),
                            "Mosse legali diverse nello stato determinizzato per seed=$seed, turn=$turn",
                        )
                    }
                }

                val move = legalMoves(state).first()
                state = (play(state, move) as PlayResult.Applied).state
                turn++
            }
        }
    }

    @Test
    fun determinizeRejectsIncompatibleHypotheses() {
        // Giochiamo 2 mosse sul seed 2 per avere carte in mano, sul tavolo, nelle prese e in playedCards.
        val s0 = newMatch(ScopaConfig(), seed = 2L)
        val s1 = (play(s0, legalMoves(s0).first()) as PlayResult.Applied).state
        val s2 = (play(s1, legalMoves(s1).first()) as PlayResult.Applied).state

        val seat = s2.toMove
        val view = viewFor(s2, seat)
        val trueOpponentHand = s2.hand(seat.other())
        val trueDeck = s2.deck

        // Verifica di base: con le carte vere funziona.
        assertEquals(s2.copy(rngState = 0L), determinize(view, trueOpponentHand, trueDeck))

        // 1. Dimensione sbagliata della mano avversaria.
        assertFailsWith<IllegalArgumentException> {
            determinize(
                view = view,
                opponentHand = trueOpponentHand.drop(1),
                deckOrder = listOf(trueOpponentHand.first()) + trueDeck,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            determinize(
                view = view,
                opponentHand = trueOpponentHand + trueDeck.first(),
                deckOrder = trueDeck.drop(1),
            )
        }

        // 2. Dimensione sbagliata del mazzo.
        assertFailsWith<IllegalArgumentException> {
            determinize(
                view = view,
                opponentHand = trueOpponentHand,
                deckOrder = trueDeck.drop(1),
            )
        }

        // 3. Carta già nella propria mano inserita nella mano avversaria.
        val ownCard = view.hand.first()
        assertFailsWith<IllegalArgumentException> {
            determinize(
                view = view,
                opponentHand = listOf(ownCard) + trueOpponentHand.drop(1),
                deckOrder = trueDeck,
            )
        }

        // 4. Carta già sul tavolo inserita nel mazzo.
        val tableCard = view.table.first()
        assertFailsWith<IllegalArgumentException> {
            determinize(
                view = view,
                opponentHand = trueOpponentHand,
                deckOrder = listOf(tableCard) + trueDeck.drop(1),
            )
        }

        // 5. Carta già catturata (e uscita in playedCards) inserita nella mano avversaria.
        val capturedCard = view.capturedBy.values.flatten().first()
        assertFailsWith<IllegalArgumentException> {
            determinize(
                view = view,
                opponentHand = listOf(capturedCard) + trueOpponentHand.drop(1),
                deckOrder = trueDeck,
            )
        }

        // 6. Carta duplicata dentro opponentHand.
        assertFailsWith<IllegalArgumentException> {
            determinize(
                view = view,
                opponentHand = listOf(trueOpponentHand[0], trueOpponentHand[0]) + trueOpponentHand.drop(2),
                deckOrder = trueDeck,
            )
        }

        // 7. Carta duplicata tra opponentHand e deckOrder.
        assertFailsWith<IllegalArgumentException> {
            determinize(
                view = view,
                opponentHand = trueOpponentHand,
                deckOrder = listOf(trueOpponentHand.first()) + trueDeck.drop(1),
            )
        }
    }

    @Test
    fun regressionFixturesReplayToExpectedFinalStates() {
        // Fixture 1: seed 2, config standard (targetScore=11, redealOnThreeKings=true, reBello=false).
        val log1 = MatchLog.parse(FIXTURE_LOG_SEED_2)
        assertEquals(2L, log1.seed)
        assertEquals(108, log1.moves.size)
        assertEquals(FIXTURE_LOG_SEED_2, log1.toText())

        val res1 = replay(log1)
        assertTrue(res1 is ReplayResult.Ok)
        val state1 = res1.state
        assertEquals(DealPhase.MATCH_OVER, state1.phase)
        assertEquals(Seat.P1, state1.winner)
        assertEquals(mapOf(Seat.P1 to 11, Seat.P2 to 4), state1.totals)
        assertEquals(Seat.P2, state1.dealer)
        assertEquals(Seat.P1, state1.toMove)
        assertEquals(Seat.P1, state1.lastCapturer)
        assertEquals(mapOf(Seat.P1 to 0, Seat.P2 to 1), state1.sweeps)
        assertEquals(28, state1.captured(Seat.P1).size)
        assertEquals(12, state1.captured(Seat.P2).size)
        assertEquals(36, state1.playedCards.size)

        // Fixture 2: seed 4, config standard (targetScore=11, redealOnThreeKings=true, reBello=false).
        val log2 = MatchLog.parse(FIXTURE_LOG_SEED_4)
        assertEquals(4L, log2.seed)
        assertEquals(108, log2.moves.size)
        assertEquals(FIXTURE_LOG_SEED_4, log2.toText())

        val res2 = replay(log2)
        assertTrue(res2 is ReplayResult.Ok)
        val state2 = res2.state
        assertEquals(DealPhase.MATCH_OVER, state2.phase)
        assertEquals(Seat.P2, state2.winner)
        assertEquals(mapOf(Seat.P1 to 1, Seat.P2 to 11), state2.totals)
        assertEquals(Seat.P2, state2.dealer)
        assertEquals(Seat.P1, state2.toMove)
        assertEquals(Seat.P2, state2.lastCapturer)
        assertEquals(mapOf(Seat.P1 to 0, Seat.P2 to 1), state2.sweeps)
        assertEquals(20, state2.captured(Seat.P1).size)
        assertEquals(20, state2.captured(Seat.P2).size)
        assertEquals(36, state2.playedCards.size)

        // Fixture 3: seed 68, variante reBello=true (targetScore=11, redealOnThreeKings=true, reBello=true).
        val log3 = MatchLog.parse(FIXTURE_LOG_SEED_68_RE_BELLO)
        assertEquals(68L, log3.seed)
        assertEquals(ScopaConfig(targetScore = 11, redealOnThreeKings = true, reBello = true), log3.config)
        assertEquals(72, log3.moves.size)
        assertEquals(FIXTURE_LOG_SEED_68_RE_BELLO, log3.toText())

        val res3 = replay(log3)
        assertTrue(res3 is ReplayResult.Ok)
        val state3 = res3.state
        assertEquals(DealPhase.MATCH_OVER, state3.phase)
        assertEquals(Seat.P2, state3.winner)
        assertEquals(mapOf(Seat.P1 to 1, Seat.P2 to 12), state3.totals)
        assertEquals(Seat.P1, state3.dealer)
        assertEquals(Seat.P2, state3.toMove)
        assertEquals(Seat.P2, state3.lastCapturer)
        assertEquals(mapOf(Seat.P1 to 1, Seat.P2 to 0), state3.sweeps)
        assertEquals(18, state3.captured(Seat.P1).size)
        assertEquals(22, state3.captured(Seat.P2).size)
        assertEquals(36, state3.playedCards.size)
    }

    private fun assertViewHidesHiddenCardsAndMatchesPublicState(
        state: ScopaState,
        seat: Seat,
        seed: Long,
        turn: Int,
    ) {
        val view = viewFor(state, seat)
        val opponent = seat.other()
        val opponentHand = state.hand(opponent)
        val deck = state.deck

        assertEquals(seat, view.seat)
        assertEquals(state.config, view.config)
        assertEquals(state.dealer, view.dealer)
        assertEquals(state.toMove, view.toMove)
        assertEquals(state.phase, view.phase)
        assertEquals(state.hand(seat), view.hand)
        assertEquals(state.table, view.table)
        assertEquals(state.capturedBy, view.capturedBy)
        assertEquals(state.playedCards, view.playedCards)
        assertEquals(opponentHand.size, view.opponentHandSize)
        assertEquals(deck.size, view.deckSize)
        assertEquals(state.sweeps, view.sweeps)
        assertEquals(state.totals, view.totals)

        // Test sul contenuto: nessuna carta visibile in ScopaView appartiene mai alla mano avversaria
        // né al mazzo coperto (le 40 carte del mazzo sono partizionate senza perdite).
        val visibleCardsInView: Set<Card> = buildSet {
            addAll(view.hand)
            addAll(view.table)
            addAll(view.capturedBy.getValue(Seat.P1))
            addAll(view.capturedBy.getValue(Seat.P2))
            addAll(view.playedCards)
        }
        val overlapWithOpponentHand = visibleCardsInView intersect opponentHand.toSet()
        assertTrue(
            overlapWithOpponentHand.isEmpty(),
            "La vista di $seat espone carte della mano avversaria per seed=$seed, turn=$turn: $overlapWithOpponentHand",
        )
        val overlapWithDeck = visibleCardsInView intersect deck.toSet()
        assertTrue(
            overlapWithDeck.isEmpty(),
            "La vista di $seat espone carte del mazzo coperto per seed=$seed, turn=$turn: $overlapWithDeck",
        )
        assertEquals(
            fullDeck().size - opponentHand.size - deck.size,
            visibleCardsInView.size,
            "Conteggio carte visibili incoerente per seed=$seed, turn=$turn",
        )
    }

    companion object {
        private val FIXTURE_LOG_SEED_2: String = """
            maestro-log 1
            rules 1
            config targetScore=11 redealOnThreeKings=true reBello=false
            seed 2
            move 4D -
            move 5D 5C
            move AC -
            move 3C -
            move 6C -
            move RB RC
            move 5S 4D,AC
            move RD 3C,7S
            move CS -
            move 4S -
            move 2B -
            move FB FC
            move 7D -
            move 3D -
            move 2C 2B
            move CD CS
            move 7B 7D
            move 4C 4S
            move AD -
            move CC 3D,6C
            move AB AD
            move RS -
            move 3B -
            move 6B -
            move FD -
            move 6D 6B
            move AS -
            move FS FD
            move 5B -
            move CB AS,3B,5B
            move 2D -
            move 7C -
            move 2S 2D
            move 6S -
            move 3S -
            move 4B -
            move AD -
            move 5D 3S,2B
            move 5S -
            move FC AD,7B
            move CS CC
            move 5B 5S
            move 4D -
            move 2D -
            move FD -
            move 7D -
            move 6S 2D,4D
            move 7C 7D
            move 2C -
            move 6D -
            move 4C -
            move 4B 4C
            move AB -
            move FB FD
            move RD -
            move 4S -
            move RC RD
            move RS 6D,4S
            move 3B 2C,AB
            move CB -
            move 3C -
            move 3D 3C
            move 7S -
            move 5C -
            move 6B -
            move 6C 6B
            move AC -
            move CD CB
            move AS AC
            move 2S -
            move FS -
            move RB 2S,FS
            move 4D 3C,AS
            move 5D -
            move FD -
            move CC 2C,7B
            move 7C -
            move 2B -
            move 5C 5D
            move 2D 2B
            move 5S -
            move 2S -
            move 5B 5S
            move 6B -
            move AD -
            move CD AD,FD
            move 6C 6B
            move FC -
            move 4S -
            move 3B -
            move RC 7C,3B
            move 3D -
            move FS FC
            move CS 3D,2S,4S
            move FB -
            move RS -
            move RD RS
            move 7D -
            move AC -
            move 6S -
            move 7S 7D
            move RB -
            move 6D 6S
            move 3S -
            move 4C AC,3S
            move AB -
            move CB AB,FB
            move 4B -
        """.trimIndent()

        private val FIXTURE_LOG_SEED_4: String = """
            maestro-log 1
            rules 1
            config targetScore=11 redealOnThreeKings=true reBello=false
            seed 4
            move 2S -
            move AD -
            move RS AD,4D,5B
            move 4S -
            move 6B 2S,4S
            move FB -
            move 4C -
            move 2C -
            move 6C 2C,4C
            move RC -
            move FC FB
            move 6S -
            move 5C -
            move 5D 5C
            move 2B -
            move RD RC
            move CB CC
            move 7S 7C
            move 6D 6S
            move 7D -
            move AS -
            move CD 7D,2B
            move CS -
            move AC AS
            move 3S -
            move 3D 3S
            move 7B -
            move AB -
            move RB CS,AB
            move 4B -
            move 2D -
            move FD -
            move 3C -
            move 5S 2D,3C
            move FS FD
            move 3B -
            move 5D AD,4D
            move RC -
            move 7S 7C
            move FS -
            move FB FS
            move RS RC
            move 7D -
            move CD -
            move FD -
            move RD -
            move 7B 7D
            move 6B -
            move CC CD
            move 3D -
            move 2S -
            move 4C 4S
            move RB RD
            move 5S 3D,2S
            move 2B -
            move 2D 2B
            move 3B -
            move FC FD
            move 5B -
            move 3S 3B
            move 6D 6B
            move AS -
            move 6S AS,5B
            move CS -
            move AB -
            move 4B -
            move 2C -
            move AC AB
            move 5C -
            move 3C -
            move 6C 2C,4B
            move CB CS
            move 2C 2B
            move 7C -
            move 6S 6B
            move FC 5D,3S
            move 3B -
            move 4S -
            move 2D -
            move FD -
            move 4B 4S
            move RD 2D,FD
            move CB -
            move RB 7C,3B
            move AD -
            move 3D -
            move CD CB
            move 6D -
            move CC 3D,6D
            move AS AD
            move 5C -
            move 4D -
            move 5S 5C
            move RC -
            move 7S -
            move FB -
            move CS -
            move AC -
            move AB AC
            move 3C -
            move 5B -
            move 4C 4D
            move 2S -
            move 7D 7S
            move RS RC
            move 6C -
            move 7B 2S,5B
            move FS FB
        """.trimIndent()

        private val FIXTURE_LOG_SEED_68_RE_BELLO: String = """
            maestro-log 1
            rules 1
            config targetScore=11 redealOnThreeKings=true reBello=true
            seed 68
            move 6D -
            move AC -
            move CC 6D,3B
            move 4C -
            move AS AC
            move 2S -
            move CD 4C,5S
            move 2C 2S
            move RC -
            move FS FB
            move 5B -
            move 6B -
            move 2D -
            move RD RC
            move 7C 7B
            move 7S 2D,5B
            move 4B -
            move RB 4B,6B
            move 3C -
            move 4D -
            move CS -
            move 5C -
            move CB CS
            move 2B -
            move 3D 3C
            move AD -
            move 4S 4D
            move FD AD,5C,2B
            move RS -
            move 3S -
            move 5D -
            move 7D -
            move 6C -
            move FC 5D,3S
            move AB -
            move 6S 6C
            move RD FD,2C
            move CS CB
            move 5C -
            move 2B -
            move 5S 5C
            move FB 2B,6B
            move 6C -
            move CD -
            move 4S -
            move 2S -
            move 6S 6C
            move RS -
            move 5D -
            move AC -
            move AS AC
            move 7B 5D,2S
            move 3B -
            move RB RS
            move AD -
            move 3D 3B
            move 2D -
            move 4C 4S
            move 5B -
            move FS AD,2D,5B
            move 4D -
            move 6D -
            move 7C -
            move FC -
            move RC 4D,6D
            move AB -
            move 7D 7C
            move 3C -
            move CC CD
            move 7S -
            move 3S 3C
            move 4B -
        """.trimIndent()
    }
}
