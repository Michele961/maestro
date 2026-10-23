package it.maestro.game.scopa

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.model.card
import it.maestro.game.model.cards
import it.maestro.game.model.fullDeck
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScopaScoringAndMatchEndTest {

    @Test
    fun spec6_1_remainingTableCardsGoToLastCapturerWithoutSweep() {
        // spec §6.1, §10: a fine smazzata, le carte rimaste sul tavolo vanno a chi ha fatto l'ultima presa.
        // Non è una scopa.
        // Prepariamo l'ultima giocata della smazzata: P1 ha fatto l'ultima presa in precedenza (lastCapturer = P1),
        // ma l'ultima carta della smazzata la gioca P2 (che è il mazziere) calando una carta senza prendere.
        // Sul tavolo restano carte, tra cui il 7D (Settebello) e l'unica carta di Bastoni di P1:
        // grazie all'assegnazione delle carte residue a P1 (lastCapturer), P1 ottiene 7D e completa i 4 semi!
        val p1AlreadyCaptured = cards(
            "AD", "2D", "3D", "4D", "5D", "6D",
            "7C", "6C", "AC", "5C", "4C", "3C", "2C", "FC", "CC", "RC",
            "7S", "6S", "AS", "5S",
        ) // 20 carte (6 Denari, 10 Coppe, 4 Spade, 0 Bastoni)
        val p2AlreadyCaptured = cards(
            "FD", "CD", "RD",
            "FS", "CS", "RS", "4S", "3S", "2S",
            "FB", "CB", "RB", "5B", "4B", "3B", "2B",
        ) // 16 carte (3 Denari, 0 Coppe, 6 Spade, 7 Bastoni)
        // Rimangono 4 carte: 7D, 7B, 6B sul tavolo + AB in mano a P2 (che non può prendere 7D, 7B, 6B).
        val state = endOfDealState(
            config = ScopaConfig(targetScore = 11),
            dealer = Seat.P2,
            toMove = Seat.P2,
            p1Hand = emptyList(),
            p2Hand = cards("AB"),
            table = cards("7D", "7B", "6B"),
            capturedBy = mapOf(Seat.P1 to p1AlreadyCaptured, Seat.P2 to p2AlreadyCaptured),
            sweeps = mapOf(Seat.P1 to 1, Seat.P2 to 0),
            lastCapturer = Seat.P1,
            totals = mapOf(Seat.P1 to 9, Seat.P2 to 0),
        )

        val result = play(state, ScopaMove(card("AB"), emptySet()))
        assertTrue(result is PlayResult.Applied)

        // Non viene emessa alcuna scopa.
        assertTrue(result.events.none { it is GameEvent.Sweep })

        val dealEnded = result.events.filterIsInstance<GameEvent.DealEnded>().single()
        val score = dealEnded.score

        // P1 aveva 20 carte + 4 residue (7D, 7B, 6B, AB) = 24 carte (> 20) -> 1 punto CARTE.
        // P1 aveva 6 Denari + 7D = 7 Denari -> 1 punto DENARI, 1 punto SETTEBELLO.
        // P1 ora ha 7D (21) e anche Bastoni (7B = 21) -> primiera P1 = 21 (7D) + 21 (7C) + 21 (7S) + 21 (7B) = 84 -> 1 punto PRIMIERA.
        // P1 aveva 1 scopa -> 1 punto SCOPE (nessuna scopa dalle carte residue).
        assertEquals(1, score.points(Seat.P1, ScoreCategory.CARTE))
        assertEquals(1, score.points(Seat.P1, ScoreCategory.DENARI))
        assertEquals(1, score.points(Seat.P1, ScoreCategory.SETTEBELLO))
        assertEquals(1, score.points(Seat.P1, ScoreCategory.PRIMIERA))
        assertEquals(1, score.points(Seat.P1, ScoreCategory.SCOPE))
        assertEquals(0, score.points(Seat.P1, ScoreCategory.RE_BELLO))
        assertEquals(84, score.primieraSum(Seat.P1))
        assertNull(score.primieraSum(Seat.P2))
        assertEquals(5, score.totalPoints(Seat.P1))
        assertEquals(0, score.totalPoints(Seat.P2))

        // Poiché P1 partiva da 9 + 5 = 14 >= 11, la partita termina e possiamo ispezionare anche finalCapturedBy.
        assertEquals(DealPhase.MATCH_OVER, result.state.phase)
        assertTrue(result.state.table.isEmpty())
        assertEquals(24, result.state.captured(Seat.P1).size)
        assertEquals(16, result.state.captured(Seat.P2).size)
        assertTrue(result.state.captured(Seat.P1).containsAll(cards("7D", "7B", "6B", "AB")))
    }

    @Test
    fun spec6_1_remainingTableCardsStayUnassignedIfNobodyCapturedAnythingInDeal() {
        // spec §6.1: "Se nessuno ha preso nulla nell'intera smazzata, restano assegnate a nessuno."
        val allExceptLast = fullDeck().dropLast(1)
        val lastCard = fullDeck().last()

        val state = endOfDealState(
            config = ScopaConfig(targetScore = 11),
            dealer = Seat.P2,
            toMove = Seat.P2,
            p1Hand = emptyList(),
            p2Hand = listOf(lastCard),
            table = allExceptLast,
            capturedBy = mapOf(Seat.P1 to emptyList(), Seat.P2 to emptyList()),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
            lastCapturer = null,
            totals = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )

        // Usiamo direttamente calculateDealScore o una giocata senza presa (su tavolo con carte tutte > lastCard).
        val nonCapturingState = endOfDealState(
            config = ScopaConfig(targetScore = 11),
            dealer = Seat.P2,
            toMove = Seat.P2,
            p1Hand = emptyList(),
            p2Hand = cards("AD"),
            table = cards("7D", "RC", "RS", "RB"),
            capturedBy = mapOf(Seat.P1 to emptyList(), Seat.P2 to emptyList()),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
            lastCapturer = null,
        )

        val result = play(nonCapturingState, ScopaMove(card("AD"), emptySet()))
        assertTrue(result is PlayResult.Applied)
        val dealEnded = result.events.filterIsInstance<GameEvent.DealEnded>().single()
        assertEquals(0, dealEnded.score.totalPoints(Seat.P1))
        assertEquals(0, dealEnded.score.totalPoints(Seat.P2))
        assertNull(dealEnded.score.primieraSum(Seat.P1))
        assertNull(dealEnded.score.primieraSum(Seat.P2))
        // Verifica che anche lo stato con 40 carte costruito sopra sia valido.
        assertEquals(40, state.table.size + state.hand(Seat.P2).size)
    }

    @Test
    fun spec6_2_cardsAndDenariPointAwardedOnlyAboveHalfAndTies20To20Or5To5AwardNobody() {
        // spec §6.2, §10: parità 20–20 nelle carte e 5–5 nei denari -> 0 a entrambi.
        val deck = fullDeck()
        val p1FiveDenari = cards("AD", "2D", "3D", "4D", "5D")
        val p2FiveDenari = cards("6D", "7D", "FD", "CD", "RD")
        val remaining30 = deck.filterNot { it.suit == Suit.DENARI }
        val p1TwentyCards = p1FiveDenari + remaining30.take(15)
        val p2TwentyCards = p2FiveDenari + remaining30.drop(15)
        assertEquals(20, p1TwentyCards.size)
        assertEquals(20, p2TwentyCards.size)

        val tieScore = calculateDealScore(
            config = ScopaConfig(),
            capturedBy = mapOf(Seat.P1 to p1TwentyCards, Seat.P2 to p2TwentyCards),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )
        assertEquals(0, tieScore.points(Seat.P1, ScoreCategory.CARTE))
        assertEquals(0, tieScore.points(Seat.P2, ScoreCategory.CARTE))
        assertEquals(0, tieScore.points(Seat.P1, ScoreCategory.DENARI))
        assertEquals(0, tieScore.points(Seat.P2, ScoreCategory.DENARI))
        assertEquals(mapOf(Seat.P1 to 0, Seat.P2 to 0), tieScore.byCategory.getValue(ScoreCategory.CARTE))
        assertEquals(mapOf(Seat.P1 to 0, Seat.P2 to 0), tieScore.byCategory.getValue(ScoreCategory.DENARI))

        // Caso 21–19 carte per P1 e 6–4 denari per P2.
        val extraNonDenari = p2TwentyCards.first { it.suit != Suit.DENARI }
        val p1OneDenari = p1FiveDenari.first()
        val p1Cards21With4Denari = (p1TwentyCards - p1OneDenari) + listOf(extraNonDenari, remaining30[16])
            .distinct()
        // Costruiamo esplicitamente 21 carte per P1 (4 Denari + 17 non-Denari) e 19 per P2 (6 Denari + 13 non-Denari).
        val p1Explicit = cards("AD", "2D", "3D", "4D") + remaining30.take(17)
        val p2Explicit = cards("5D", "6D", "7D", "FD", "CD", "RD") + remaining30.drop(17)
        assertEquals(21, p1Explicit.size)
        assertEquals(19, p2Explicit.size)
        assertTrue(p1Cards21With4Denari.isNotEmpty())

        val splitScore = calculateDealScore(
            config = ScopaConfig(),
            capturedBy = mapOf(Seat.P1 to p1Explicit, Seat.P2 to p2Explicit),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )
        assertEquals(1, splitScore.points(Seat.P1, ScoreCategory.CARTE))
        assertEquals(0, splitScore.points(Seat.P2, ScoreCategory.CARTE))
        assertEquals(0, splitScore.points(Seat.P1, ScoreCategory.DENARI))
        assertEquals(1, splitScore.points(Seat.P2, ScoreCategory.DENARI))
    }

    @Test
    fun spec6_2_settebelloAndReBelloVariantScoring() {
        // spec §6.2: Settebello (7D) vale sempre 1 punto; Re bello (RD) vale 1 punto solo con reBello = true.
        val p1Cards = cards("7D", "AC", "AS", "AB")
        val p2Cards = cards("RD", "2C", "2S", "2B")

        val scoreWithoutReBello = calculateDealScore(
            config = ScopaConfig(reBello = false),
            capturedBy = mapOf(Seat.P1 to p1Cards, Seat.P2 to p2Cards),
            sweeps = mapOf(Seat.P1 to 2, Seat.P2 to 3),
        )
        assertEquals(1, scoreWithoutReBello.points(Seat.P1, ScoreCategory.SETTEBELLO))
        assertEquals(0, scoreWithoutReBello.points(Seat.P2, ScoreCategory.SETTEBELLO))
        assertEquals(0, scoreWithoutReBello.points(Seat.P1, ScoreCategory.RE_BELLO))
        assertEquals(0, scoreWithoutReBello.points(Seat.P2, ScoreCategory.RE_BELLO))
        assertEquals(2, scoreWithoutReBello.points(Seat.P1, ScoreCategory.SCOPE))
        assertEquals(3, scoreWithoutReBello.points(Seat.P2, ScoreCategory.SCOPE))

        val scoreWithReBello = calculateDealScore(
            config = ScopaConfig(reBello = true),
            capturedBy = mapOf(Seat.P1 to p1Cards, Seat.P2 to p2Cards),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )
        assertEquals(0, scoreWithReBello.points(Seat.P1, ScoreCategory.RE_BELLO))
        assertEquals(1, scoreWithReBello.points(Seat.P2, ScoreCategory.RE_BELLO))
    }

    @Test
    fun spec6_3_primieraTableValuesAndMissingSuitOrTieCases() {
        // Verifica l'intera tabella normativa di spec §6.3:
        // 7=21, 6=18, A=16, 5=15, 4=14, 3=13, 2=12, Fante/Cavallo/Re=10.
        assertEquals(21, Rank.SETTE.primieraPoints)
        assertEquals(18, Rank.SEI.primieraPoints)
        assertEquals(16, Rank.ASSO.primieraPoints)
        assertEquals(15, Rank.CINQUE.primieraPoints)
        assertEquals(14, Rank.QUATTRO.primieraPoints)
        assertEquals(13, Rank.TRE.primieraPoints)
        assertEquals(12, Rank.DUE.primieraPoints)
        assertEquals(10, Rank.FANTE.primieraPoints)
        assertEquals(10, Rank.CAVALLO.primieraPoints)
        assertEquals(10, Rank.RE.primieraPoints)

        // Caso 1 (spec §6.3, §10): P1 ha tre 7 (21+21+21=63) ma gli manca Bastoni;
        // P2 ha quattro figure (10+10+10+10=40) in tutti e 4 i semi -> vince P2!
        val oneMissingSuitScore = calculateDealScore(
            config = ScopaConfig(),
            capturedBy = mapOf(
                Seat.P1 to cards("7D", "7C", "7S"),
                Seat.P2 to cards("FD", "CC", "RS", "FB"),
            ),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )
        assertNull(oneMissingSuitScore.primieraSum(Seat.P1))
        assertEquals(40, oneMissingSuitScore.primieraSum(Seat.P2))
        assertEquals(0, oneMissingSuitScore.points(Seat.P1, ScoreCategory.PRIMIERA))
        assertEquals(1, oneMissingSuitScore.points(Seat.P2, ScoreCategory.PRIMIERA))

        // Caso 2 (spec §6.3, §10): entrambi senza almeno un seme -> il punto non va a nessuno.
        val bothMissingSuitScore = calculateDealScore(
            config = ScopaConfig(),
            capturedBy = mapOf(
                Seat.P1 to cards("7D", "7C", "7S"), // manca Bastoni
                Seat.P2 to cards("6D", "6C", "7B"), // manca Spade
            ),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )
        assertNull(bothMissingSuitScore.primieraSum(Seat.P1))
        assertNull(bothMissingSuitScore.primieraSum(Seat.P2))
        assertEquals(0, bothMissingSuitScore.points(Seat.P1, ScoreCategory.PRIMIERA))
        assertEquals(0, bothMissingSuitScore.points(Seat.P2, ScoreCategory.PRIMIERA))

        // Caso 3 (spec §6.3, §10): parità di somma di primiera (es. 7+6+A+5 = 21+18+16+15 = 70 ciascuno)
        // -> il punto non va a nessuno.
        val tiePrimieraScore = calculateDealScore(
            config = ScopaConfig(),
            capturedBy = mapOf(
                Seat.P1 to cards("7D", "6C", "AS", "5B", "2D", "FC"),
                Seat.P2 to cards("5D", "AC", "6S", "7B", "3S", "RB"),
            ),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )
        assertEquals(70, tiePrimieraScore.primieraSum(Seat.P1))
        assertEquals(70, tiePrimieraScore.primieraSum(Seat.P2))
        assertEquals(0, tiePrimieraScore.points(Seat.P1, ScoreCategory.PRIMIERA))
        assertEquals(0, tiePrimieraScore.points(Seat.P2, ScoreCategory.PRIMIERA))

        // Caso 4: entrambi hanno tutti i semi e P1 ha somma superiore (es. 7+7+6+A = 76 vs 6+6+7+7 = 78 -> vince P2).
        val normalPrimieraScore = calculateDealScore(
            config = ScopaConfig(),
            capturedBy = mapOf(
                Seat.P1 to cards("7D", "7C", "6S", "AB"), // 21 + 21 + 18 + 16 = 76
                Seat.P2 to cards("6D", "6C", "7S", "7B"), // 18 + 18 + 21 + 21 = 78
            ),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        )
        assertEquals(76, normalPrimieraScore.primieraSum(Seat.P1))
        assertEquals(78, normalPrimieraScore.primieraSum(Seat.P2))
        assertEquals(0, normalPrimieraScore.points(Seat.P1, ScoreCategory.PRIMIERA))
        assertEquals(1, normalPrimieraScore.points(Seat.P2, ScoreCategory.PRIMIERA))
    }

    @Test
    fun spec7_2_spec7_3_bothPlayersAboveTargetScoreHigherWinsAndTieAboveTargetContinuesMatch() {
        // Prepariamo una fine smazzata in cui P1 ottiene 2 punti (CARTE + PRIMIERA) e P2 ottiene 2 punti (DENARI + SETTEBELLO).
        val deck = fullDeck()
        val p2SixDenari = cards("5D", "6D", "7D", "FD", "CD", "RD")
        val p1FourDenari = cards("AD", "2D", "3D", "4D")
        val nonDenari = deck.filterNot { it.suit == Suit.DENARI }
        // Diamo a P1 i tre 7 non-denari (7C, 7S, 7B) + altre 14 carte non-denari = 4 + 17 = 21 carte.
        // P1 primiera: 4D(14) + 7C(21) + 7S(21) + 7B(21) = 77.
        // P2 primiera: 7D(21) + max<=18(54) <= 75 -> P1 vince CARTE (21 vs 19) e PRIMIERA; P2 vince DENARI (6 vs 4) e SETTEBELLO (7D).
        val threeSevens = cards("7C", "7S", "7B")
        val otherNonDenari = nonDenari.filterNot { it in threeSevens }
        val p1CapturedBeforeLast = p1FourDenari + threeSevens + otherNonDenari.take(13) // 20 carte
        val lastCardPlayedByP1 = otherNonDenari[13] // 21ª carta di P1, scartata sul tavolo e ripresa come residua da P1 (lastCapturer = P1)
        val p2Captured = p2SixDenari + otherNonDenari.drop(14) // 6 + 13 = 19 carte

        // Caso A (spec §7.3, §10): entrambi raggiungono/superano targetScore (11) ma sono PARI (10+2=12 a 10+2=12)
        // -> la partita NON finisce, inizia una nuova smazzata con il mazziere cambiato!
        val tieAboveTargetState = endOfDealState(
            config = ScopaConfig(targetScore = 11),
            dealer = Seat.P1,
            toMove = Seat.P1,
            p1Hand = listOf(lastCardPlayedByP1),
            p2Hand = emptyList(),
            table = emptyList(),
            capturedBy = mapOf(Seat.P1 to p1CapturedBeforeLast, Seat.P2 to p2Captured),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
            lastCapturer = Seat.P1,
            totals = mapOf(Seat.P1 to 10, Seat.P2 to 10),
        )

        val tieResult = play(tieAboveTargetState, ScopaMove(lastCardPlayedByP1, emptySet()))
        assertTrue(tieResult is PlayResult.Applied)
        val tieDealEnded = tieResult.events.filterIsInstance<GameEvent.DealEnded>().single()
        assertEquals(2, tieDealEnded.score.totalPoints(Seat.P1))
        assertEquals(2, tieDealEnded.score.totalPoints(Seat.P2))
        assertEquals(mapOf(Seat.P1 to 2, Seat.P2 to 2), tieDealEnded.score.totalsBySeat)
        assertTrue(tieResult.events.none { it is GameEvent.MatchEnded })

        // La partita continua in una nuova smazzata con mazziere alternato (P2) e totali 12–12.
        assertEquals(DealPhase.PLAYING, tieResult.state.phase)
        assertNull(tieResult.state.winner)
        assertEquals(Seat.P2, tieResult.state.dealer)
        assertEquals(Seat.P1, tieResult.state.toMove)
        assertEquals(1, tieResult.state.distribution)
        assertEquals(mapOf(Seat.P1 to 12, Seat.P2 to 12), tieResult.state.totals)
        assertEquals(12, tieResult.state.totalScore(Seat.P1))
        assertEquals(12, tieResult.state.totalScore(Seat.P2))
        assertEquals(3, tieResult.state.hand(Seat.P1).size)
        assertEquals(3, tieResult.state.hand(Seat.P2).size)
        assertEquals(4, tieResult.state.table.size)
        assertEquals(30, tieResult.state.deck.size)
        assertTrue(tieResult.state.captured(Seat.P1).isEmpty())
        assertTrue(tieResult.state.captured(Seat.P2).isEmpty())
        assertEquals(0, tieResult.state.sweeps.getValue(Seat.P1))
        assertEquals(0, tieResult.state.sweeps.getValue(Seat.P2))
        assertNull(tieResult.state.lastCapturer)

        // Caso B (spec §7.2, §10): entrambi superano targetScore (11) nella stessa smazzata (P1: 10+2=12, P2: 10+3=13)
        // -> vince chi ha il punteggio totale più alto (P2 con 13 > 12).
        val bothAboveTargetState = tieAboveTargetState.copy(
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 1), // P2 fa 3 punti invece di 2 -> 13 a 12
        )

        val winResult = play(bothAboveTargetState, ScopaMove(lastCardPlayedByP1, emptySet()))
        assertTrue(winResult is PlayResult.Applied)
        val matchEnded = winResult.events.filterIsInstance<GameEvent.MatchEnded>().single()
        assertEquals(Seat.P2, matchEnded.winner)
        assertEquals(mapOf(Seat.P1 to 12, Seat.P2 to 13), matchEnded.totals)
        assertEquals(DealPhase.MATCH_OVER, winResult.state.phase)
        assertEquals(Seat.P2, winResult.state.winner)
        assertEquals(mapOf(Seat.P1 to 12, Seat.P2 to 13), winResult.state.totals)
    }

    @Test
    fun spec7_6_whenMatchIsOverLegalMovesIsEmptyAndPlayReturnsRejectedMatchOver() {
        // spec §7.6: a partita finita legalMoves() restituisce una lista vuota e play() restituisce Rejected(MATCH_OVER).
        val deck = fullDeck()
        val lastCard = deck.last()
        val state = endOfDealState(
            config = ScopaConfig(targetScore = 11),
            dealer = Seat.P1,
            toMove = Seat.P1,
            p1Hand = listOf(lastCard),
            p2Hand = emptyList(),
            table = emptyList(),
            capturedBy = mapOf(Seat.P1 to deck.dropLast(1), Seat.P2 to emptyList()),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
            lastCapturer = Seat.P1,
            totals = mapOf(Seat.P1 to 10, Seat.P2 to 0),
        )

        val applied = play(state, ScopaMove(lastCard, emptySet()))
        assertTrue(applied is PlayResult.Applied)
        val finishedState = applied.state
        assertEquals(DealPhase.MATCH_OVER, finishedState.phase)
        assertEquals(Seat.P1, finishedState.winner)

        assertTrue(legalMoves(finishedState).isEmpty())
        assertEquals(
            PlayResult.Rejected(RejectReason.MATCH_OVER),
            play(finishedState, ScopaMove(lastCard, emptySet())),
        )
    }

    @Test
    fun spec7_2_spec7_3_spec10_500SeedsMatchAlwaysTerminatesWithValidWinnerAndInvariants() {
        val canonicalDeckSet = fullDeck().toSet()

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
            var state = newMatch(config, seed)
            var totalMovesInMatch = 0
            var dealsCompleted = 0
            var expectedDealer = state.dealer

            while (state.phase != DealPhase.MATCH_OVER) {
                assertEquals(DealPhase.PLAYING, state.phase, "Fase non PLAYING durante la partita per seed=$seed")
                assertEquals(expectedDealer, state.dealer, "Mazziere inatteso nella smazzata $dealsCompleted per seed=$seed")

                val legal = legalMoves(state)
                assertTrue(legal.isNotEmpty(), "legalMoves vuota per seed=$seed")

                val result = play(state, legal.first())
                assertTrue(result is PlayResult.Applied, "play ha rifiutato la prima mossa legale per seed=$seed")
                totalMovesInMatch++

                val dealEnded = result.events.filterIsInstance<GameEvent.DealEnded>().singleOrNull()
                if (dealEnded != null) {
                    dealsCompleted++
                    expectedDealer = expectedDealer.other()
                    assertScoreCoherence(dealEnded.score, config, seed, dealsCompleted)

                    if (result.state.phase == DealPhase.PLAYING) {
                        // La partita non era ancora finita (nessuno >= targetScore, oppure parità >= targetScore).
                        val p1Tot = result.state.totalScore(Seat.P1)
                        val p2Tot = result.state.totalScore(Seat.P2)
                        assertTrue(
                            (p1Tot < config.targetScore && p2Tot < config.targetScore) || p1Tot == p2Tot,
                            "La partita doveva terminare con totali $p1Tot-$p2Tot (target=${config.targetScore}) per seed=$seed",
                        )
                    }
                }

                state = result.state
                assert40CardsInvariant(state, canonicalDeckSet, seed, totalMovesInMatch)
            }

            assertTrue(dealsCompleted >= 1, "Nessuna smazzata completata per seed=$seed")
            assertEquals(36 * dealsCompleted, totalMovesInMatch, "Mosse totali non multiple di 36 per seed=$seed")

            val winner = state.winner
            assertNotNull(winner, "Vincitore nullo a MATCH_OVER per seed=$seed")
            val loser = winner.other()
            assertTrue(
                state.totalScore(winner) >= config.targetScore,
                "Vincitore sotto targetScore per seed=$seed: ${state.totals}",
            )
            assertTrue(
                state.totalScore(winner) > state.totalScore(loser),
                "Vincitore non ha punteggio strettamente maggiore per seed=$seed: ${state.totals}",
            )
            assertTrue(legalMoves(state).isEmpty(), "legalMoves non vuota a MATCH_OVER per seed=$seed")
            assertEquals(
                PlayResult.Rejected(RejectReason.MATCH_OVER),
                play(state, ScopaMove(card("AD"), emptySet())),
            )
        }
    }

    private fun assertScoreCoherence(
        score: DealScore,
        config: ScopaConfig,
        seed: Long,
        dealIndex: Int,
    ) {
        val cartePointsSum = score.points(Seat.P1, ScoreCategory.CARTE) + score.points(Seat.P2, ScoreCategory.CARTE)
        val denariPointsSum = score.points(Seat.P1, ScoreCategory.DENARI) + score.points(Seat.P2, ScoreCategory.DENARI)
        val settebelloSum =
            score.points(Seat.P1, ScoreCategory.SETTEBELLO) + score.points(Seat.P2, ScoreCategory.SETTEBELLO)
        val primieraSum = score.points(Seat.P1, ScoreCategory.PRIMIERA) + score.points(Seat.P2, ScoreCategory.PRIMIERA)
        val reBelloSum = score.points(Seat.P1, ScoreCategory.RE_BELLO) + score.points(Seat.P2, ScoreCategory.RE_BELLO)

        assertTrue(cartePointsSum in 0..1, "Somma punti CARTE fuori [0, 1] per seed=$seed, deal=$dealIndex")
        assertTrue(denariPointsSum in 0..1, "Somma punti DENARI fuori [0, 1] per seed=$seed, deal=$dealIndex")
        assertTrue(settebelloSum in 0..1, "Somma punti SETTEBELLO fuori [0, 1] per seed=$seed, deal=$dealIndex")
        assertTrue(primieraSum in 0..1, "Somma punti PRIMIERA fuori [0, 1] per seed=$seed, deal=$dealIndex")
        if (config.reBello) {
            assertTrue(reBelloSum in 0..1, "Somma punti RE_BELLO fuori [0, 1] per seed=$seed, deal=$dealIndex")
        } else {
            assertEquals(0, reBelloSum, "RE_BELLO assegnato con reBello=false per seed=$seed, deal=$dealIndex")
        }
        assertEquals(score.pointsFor(Seat.P1).values.sum(), score.totalPoints(Seat.P1))
        assertEquals(score.pointsFor(Seat.P2).values.sum(), score.totalPoints(Seat.P2))
    }

    private fun assert40CardsInvariant(
        state: ScopaState,
        canonicalDeckSet: Set<Card>,
        seed: Long,
        step: Int,
    ) {
        val allCards = buildList(40) {
            addAll(state.hand(Seat.P1))
            addAll(state.hand(Seat.P2))
            addAll(state.table)
            addAll(state.deck)
            addAll(state.captured(Seat.P1))
            addAll(state.captured(Seat.P2))
        }
        assertEquals(40, allCards.size, "Totale carte != 40 per seed=$seed, step=$step")
        assertEquals(
            canonicalDeckSet,
            allCards.toSet(),
            "Insieme delle 40 carte non coincide col mazzo canonico per seed=$seed, step=$step",
        )
    }

    private fun endOfDealState(
        config: ScopaConfig = ScopaConfig(),
        dealer: Seat = Seat.P1,
        toMove: Seat = Seat.P1,
        p1Hand: List<Card>,
        p2Hand: List<Card>,
        table: List<Card>,
        capturedBy: Map<Seat, List<Card>>,
        sweeps: Map<Seat, Int> = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        lastCapturer: Seat? = Seat.P1,
        totals: Map<Seat, Int> = mapOf(Seat.P1 to 0, Seat.P2 to 0),
    ): ScopaState = ScopaState(
        config = config,
        dealer = dealer,
        toMove = toMove,
        hands = mapOf(Seat.P1 to p1Hand, Seat.P2 to p2Hand),
        table = table,
        deck = emptyList(),
        rngState = 42L,
        phase = DealPhase.PLAYING,
        distribution = 6,
        capturedBy = capturedBy,
        sweeps = sweeps,
        lastCapturer = lastCapturer,
        totals = totals,
    )
}
