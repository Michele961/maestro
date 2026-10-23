package it.maestro.game.scopa

import it.maestro.game.model.Card
import it.maestro.game.model.CardOrder
import it.maestro.game.model.Seat
import it.maestro.game.model.card
import it.maestro.game.model.cards
import it.maestro.game.model.fullDeck
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScopaPlayTest {

    @Test
    fun spec4_8_spec4_9_exampleFromSpecificationWithExactMoveOrder() {
        // spec §4.8: tavolo AD, 3C, 4S, 5B, 7C; mano con FC e 5D.
        // spec §4.9: ordine delle mosse prima per carta in ordine canonico (5D prima di FC),
        // poi per presa ordinata internamente in ordine canonico e confrontata lessicograficamente.
        val state = customState(
            toMove = Seat.P1,
            p1Hand = cards("FC", "5D"),
            p2Hand = cards("2D", "2C"),
            table = cards("AD", "3C", "4S", "5B", "7C"),
        )

        val moves = legalMoves(state)

        val expectedMoves = listOf(
            ScopaMove(card = card("5D"), capture = setOf(card("5B"))),
            ScopaMove(card = card("FC"), capture = cards("AD", "3C", "4S").toSet()),
            ScopaMove(card = card("FC"), capture = cards("AD", "7C").toSet()),
            ScopaMove(card = card("FC"), capture = cards("3C", "5B").toSet()),
        )
        assertEquals(expectedMoves, moves)

        // Verifica anche l'ordine di iterazione interno di ciascun Set<Card> di presa (spec §4.9).
        assertEquals(cards("5B"), moves[0].capture.toList())
        assertEquals(cards("AD", "3C", "4S"), moves[1].capture.toList())
        assertEquals(cards("AD", "7C"), moves[2].capture.toList())
        assertEquals(cards("3C", "5B"), moves[3].capture.toList())
    }

    @Test
    fun spec4_2_equalRankCardTakesPrecedenceOverSumCapture() {
        // spec §4.2, §10: carta uguale presente insieme a una somma possibile -> si può prendere solo la carta uguale.
        val state = customState(
            toMove = Seat.P1,
            p1Hand = cards("7D"),
            p2Hand = cards("AC"),
            table = cards("7S", "3D", "4C", "2B", "5S"),
        )

        val moves = legalMoves(state)
        assertEquals(
            listOf(ScopaMove(card = card("7D"), capture = setOf(card("7S")))),
            moves,
        )

        // Tentare la presa per somma {3D, 4C} quando 7S è sul tavolo deve essere rifiutato.
        val illegalSumAttempt = play(state, ScopaMove(card = card("7D"), capture = cards("3D", "4C").toSet()))
        assertEquals(PlayResult.Rejected(RejectReason.NOT_IN_LEGAL_MOVES), illegalSumAttempt)
    }

    @Test
    fun spec4_2_multipleEqualRankCardsOnTableGenerateDistinctLegalMovesInCanonicalOrder() {
        // spec §4.2, §4.9, §10: due (o più) carte uguali sul tavolo -> mosse legali distinte in ordine canonico.
        val state = customState(
            toMove = Seat.P1,
            p1Hand = cards("6C"),
            p2Hand = cards("AD"),
            table = cards("6B", "2D", "4C", "6D", "6S"),
        )

        val moves = legalMoves(state)
        assertEquals(
            listOf(
                ScopaMove(card = card("6C"), capture = setOf(card("6D"))),
                ScopaMove(card = card("6C"), capture = setOf(card("6S"))),
                ScopaMove(card = card("6C"), capture = setOf(card("6B"))),
            ),
            moves,
        )
    }

    @Test
    fun spec4_3_multipleSumCombinationsWithSharedCardsGenerateDistinctMoves() {
        // spec §4.3, §10: più insiemi per somma, anche con carte in comune -> una mossa per insieme.
        // Carta giocata: 9D (Cavallo = 9). Tavolo: 2D, 3D, 4D, 4C, 7C (nessun Cavallo sul tavolo).
        // Combinazioni a somma 9 (ordinate internamente secondo CardOrder: 2D, 3D, 4D, 4C, 7C):
        // - {2D, 3D, 4D} (2+3+4=9)
        // - {2D, 3D, 4C} (2+3+4=9, condivide 2D e 3D)
        // - {2D, 7C}     (2+7=9, condivide 2D)
        val state = customState(
            toMove = Seat.P1,
            p1Hand = cards("CD"),
            p2Hand = cards("AB"),
            table = cards("7C", "4C", "2D", "4D", "3D"),
        )

        val moves = legalMoves(state)
        assertEquals(
            listOf(
                ScopaMove(card = card("CD"), capture = cards("2D", "3D", "4D").toSet()),
                ScopaMove(card = card("CD"), capture = cards("2D", "3D", "4C").toSet()),
                ScopaMove(card = card("CD"), capture = cards("2D", "7C").toSet()),
            ),
            moves,
        )
    }

    @Test
    fun spec4_4_mandatoryCaptureForbidsEmptyCaptureWhenCaptureIsPossible() {
        // spec §4.4, §10: nessuna mossa legale con capture vuoto per una carta che può prendere,
        // sia per carta uguale (4.2) sia per somma (4.3).
        val state = customState(
            toMove = Seat.P1,
            p1Hand = cards("4D", "6C", "RB"),
            p2Hand = cards("AD", "2D", "3D"),
            table = cards("4S", "AD", "5B"),
        )

        val moves = legalMoves(state)
        // 4D può prendere 4S (carta uguale); 6C può prendere {AD, 5B} (somma); RB non può prendere (1+4+5=10! Aspetta: 4S+AD+5B = 4+1+5 = 10, quindi anche RB prende!).
        // Mettiamo 7B invece di RB (7 non ottenibile da {4S, AD, 5B}: somme possibili 5, 6, 9, 10).
        val stateWithNonCapturing = customState(
            toMove = Seat.P1,
            p1Hand = cards("4D", "6C", "7B"),
            p2Hand = cards("2D", "3D", "RD"),
            table = cards("4S", "AD", "5B"),
        )

        val legal = legalMoves(stateWithNonCapturing)
        assertTrue(legal.none { it.card == card("4D") && it.capture.isEmpty() })
        assertTrue(legal.none { it.card == card("6C") && it.capture.isEmpty() })
        assertEquals(
            listOf(
                ScopaMove(card = card("4D"), capture = setOf(card("4S"))),
                ScopaMove(card = card("6C"), capture = cards("AD", "5B").toSet()),
                ScopaMove(card = card("7B"), capture = emptySet()),
            ),
            legal,
        )

        // Giocare 4D o 6C con capture vuoto è rifiutato.
        assertEquals(
            PlayResult.Rejected(RejectReason.NOT_IN_LEGAL_MOVES),
            play(stateWithNonCapturing, ScopaMove(card("4D"), emptySet())),
        )
        assertEquals(
            PlayResult.Rejected(RejectReason.NOT_IN_LEGAL_MOVES),
            play(stateWithNonCapturing, ScopaMove(card("6C"), emptySet())),
        )
        assertTrue(moves.all { it.capture.isNotEmpty() })
    }

    @Test
    fun spec4_5_spec4_6_nonCapturingCardStaysOnTableIncludingEmptyTable() {
        // spec §4.5: se la carta giocata non può prendere, resta scoperta sul tavolo.
        val stateNonEmptyTable = customState(
            toMove = Seat.P1,
            p1Hand = cards("2D", "7C"),
            p2Hand = cards("RS", "RB"),
            table = cards("5D", "FC"),
        )

        val res1 = play(stateNonEmptyTable, ScopaMove(card("2D"), emptySet()))
        assertTrue(res1 is PlayResult.Applied)
        assertEquals(cards("5D", "FC", "2D"), res1.state.table)
        assertEquals(cards("7C"), res1.state.hand(Seat.P1))
        assertEquals(Seat.P2, res1.state.toMove)
        assertEquals(listOf<GameEvent>(GameEvent.CardPlayed(Seat.P1, card("2D"))), res1.events)
        assertNull(res1.state.lastCapturer)

        // spec §4.6: con il tavolo vuoto la carta giocata resta sul tavolo.
        val stateEmptyTable = customState(
            toMove = Seat.P2,
            p1Hand = cards("7C"),
            p2Hand = cards("AD", "3S"),
            table = emptyList(),
        )
        assertEquals(
            listOf(
                ScopaMove(card("AD"), emptySet()),
                ScopaMove(card("3S"), emptySet()),
            ),
            legalMoves(stateEmptyTable),
        )
        val res2 = play(stateEmptyTable, ScopaMove(card("AD"), emptySet()))
        assertTrue(res2 is PlayResult.Applied)
        assertEquals(cards("AD"), res2.state.table)
        assertEquals(Seat.P1, res2.state.toMove)
        assertEquals(listOf<GameEvent>(GameEvent.CardPlayed(Seat.P2, card("AD"))), res2.events)
    }

    @Test
    fun spec4_7_capturedCardsAndPlayedCardGoToPlayerCapturedPileAndUpdateLastCapturer() {
        // spec §4.7: le carte prese e la carta giocata vanno nel mazzetto delle prese del giocatore.
        val state = customState(
            toMove = Seat.P1,
            p1Hand = cards("7D", "2C"),
            p2Hand = cards("RD", "RC"),
            table = cards("4S", "3D", "FB"),
        )

        val move = ScopaMove(card = card("7D"), capture = cards("3D", "4S").toSet())
        val result = play(state, move)
        assertTrue(result is PlayResult.Applied)

        assertEquals(cards("FB"), result.state.table)
        assertEquals(cards("7D", "3D", "4S"), result.state.captured(Seat.P1))
        assertEquals(emptyList(), result.state.captured(Seat.P2))
        assertEquals(Seat.P1, result.state.lastCapturer)
        assertEquals(0, result.state.sweeps.getValue(Seat.P1))
        assertEquals(
            listOf(
                GameEvent.CardPlayed(Seat.P1, card("7D")),
                GameEvent.Captured(Seat.P1, cards("3D", "4S").toSet()),
            ),
            result.events,
        )
    }

    @Test
    fun spec5_1_spec5_3_clearingTableMidDistributionAndOnLastPlayOfNonFinalDistributionScoresSweep() {
        // Caso A (spec §5.1): scopa a metà distribuzione (mani non vuote).
        val midDealState = customState(
            toMove = Seat.P1,
            p1Hand = cards("5D", "AC"),
            p2Hand = cards("2S", "3B"),
            table = cards("2D", "3C"),
            deck = cards("RD", "RC", "RS", "RB", "CD", "CC"),
        )

        val midResult = play(midDealState, ScopaMove(card("5D"), cards("2D", "3C").toSet()))
        assertTrue(midResult is PlayResult.Applied)
        assertTrue(midResult.state.table.isEmpty())
        assertEquals(1, midResult.state.sweeps.getValue(Seat.P1))
        assertEquals(0, midResult.state.sweeps.getValue(Seat.P2))
        assertEquals(
            listOf(
                GameEvent.CardPlayed(Seat.P1, card("5D")),
                GameEvent.Captured(Seat.P1, cards("2D", "3C").toSet()),
                GameEvent.Sweep(Seat.P1),
            ),
            midResult.events,
        )

        // spec §5.3: dopo una scopa la carta giocata dall'avversario resta sul tavolo, come da 4.6.
        val afterSweepResult = play(midResult.state, ScopaMove(card("2S"), emptySet()))
        assertTrue(afterSweepResult is PlayResult.Applied)
        assertEquals(cards("2S"), afterSweepResult.state.table)

        // Caso B (spec §5.1, §10): scopa all'ultima giocata di una distribuzione NON finale (mazzo non vuoto)
        // -> punto scopa valido + ridistribuzione automatica (CardsDealt).
        val endOfNonFinalDistState = customState(
            dealer = Seat.P1,
            toMove = Seat.P1,
            p1Hand = cards("7D"),
            p2Hand = emptyList(),
            table = cards("7C"),
            deck = cards("AD", "2D", "3D", "4D", "5D", "6D"),
            distribution = 2,
        )

        val endNonFinalResult = play(endOfNonFinalDistState, ScopaMove(card("7D"), setOf(card("7C"))))
        assertTrue(endNonFinalResult is PlayResult.Applied)
        assertEquals(1, endNonFinalResult.state.sweeps.getValue(Seat.P1))
        assertEquals(3, endNonFinalResult.state.distribution)
        assertEquals(DealPhase.PLAYING, endNonFinalResult.state.phase)
        assertEquals(
            listOf(
                GameEvent.CardPlayed(Seat.P1, card("7D")),
                GameEvent.Captured(Seat.P1, setOf(card("7C"))),
                GameEvent.Sweep(Seat.P1),
                GameEvent.CardsDealt(distribution = 3),
            ),
            endNonFinalResult.events,
        )
    }

    @Test
    fun spec5_2_clearingTableOnFinalPlayOfFinalDistributionIsNotASweep() {
        // spec §5.2, §10: l'ultima giocata dell'ultima distribuzione della smazzata
        // (entrambe le mani vuote dopo la giocata e mazzo vuoto) non vale mai scopa, anche se svuota il tavolo.
        val finalPlayState = customState(
            dealer = Seat.P1,
            toMove = Seat.P1,
            p1Hand = cards("RD"),
            p2Hand = emptyList(),
            table = cards("4C", "6S"),
            deck = emptyList(),
            distribution = 6,
            sweeps = mapOf(Seat.P1 to 2, Seat.P2 to 1),
        )

        val result = play(finalPlayState, ScopaMove(card("RD"), cards("4C", "6S").toSet()))
        assertTrue(result is PlayResult.Applied)

        // Non viene emesso GameEvent.Sweep e i punti SCOPE in DealEnded restano 2 per P1 e 1 per P2.
        assertTrue(result.events.none { it is GameEvent.Sweep })
        val dealEnded = result.events.filterIsInstance<GameEvent.DealEnded>().single()
        assertEquals(2, dealEnded.score.points(Seat.P1, ScoreCategory.SCOPE))
        assertEquals(1, dealEnded.score.points(Seat.P2, ScoreCategory.SCOPE))
        assertEquals(
            listOf(
                GameEvent.CardPlayed(Seat.P1, card("RD")),
                GameEvent.Captured(Seat.P1, cards("4C", "6S").toSet()),
                dealEnded,
            ),
            result.events,
        )
    }

    @Test
    fun spec3_4_spec3_5_automaticRedealAlternatesStartingFromDealerOtherWithoutNewTableCards() {
        // spec §3.4: quando entrambi i giocatori hanno finito le carte e il mazzo non è vuoto,
        // il mazziere dà altre 3 carte a testa una alla volta, iniziando dall'avversario, senza carte sul tavolo.
        val state = customState(
            dealer = Seat.P2,
            toMove = Seat.P2,
            p1Hand = emptyList(),
            p2Hand = cards("AD"),
            table = cards("RC", "RS"),
            deck = cards("2D", "3D", "4D", "5D", "6D", "7D", "FD", "CD", "RD", "AC", "2C", "3C"),
            distribution = 1,
        )

        val result = play(state, ScopaMove(card("AD"), emptySet()))
        assertTrue(result is PlayResult.Applied)

        // dealer = P2 -> firstReceiver = P1 riceve indici 0, 2, 4; dealer P2 riceve indici 1, 3, 5.
        assertEquals(cards("2D", "4D", "6D"), result.state.hand(Seat.P1))
        assertEquals(cards("3D", "5D", "7D"), result.state.hand(Seat.P2))
        assertEquals(cards("FD", "CD", "RD", "AC", "2C", "3C"), result.state.deck)
        // Nessuna carta del mazzo è finita sul tavolo (sul tavolo ci sono solo RC, RS e lo scarto AD).
        assertEquals(cards("RC", "RS", "AD"), result.state.table)
        assertEquals(2, result.state.distribution)
        assertEquals(Seat.P1, result.state.toMove)
        assertEquals(DealPhase.PLAYING, result.state.phase)
        assertEquals(
            listOf(
                GameEvent.CardPlayed(Seat.P2, card("AD")),
                GameEvent.CardsDealt(distribution = 2),
            ),
            result.events,
        )
    }

    @Test
    fun rejectedWhenMoveNotInLegalMovesOrWhenDealOverLeavesStateUnchanged() {
        val state = customState(
            toMove = Seat.P1,
            p1Hand = cards("5D", "7C"),
            p2Hand = cards("AD", "2C"),
            table = cards("5B", "2D", "3S"),
        )
        val snapshotBefore = state.copy()

        // 1. Carta non in mano al giocatore di turno.
        val cardNotInHand = play(state, ScopaMove(card("AD"), emptySet()))
        assertEquals(PlayResult.Rejected(RejectReason.NOT_IN_LEGAL_MOVES), cardNotInHand)

        // 2. Scarto con carta che può prendere (viola 4.4).
        val mandatoryCaptureViolated = play(state, ScopaMove(card("5D"), emptySet()))
        assertEquals(PlayResult.Rejected(RejectReason.NOT_IN_LEGAL_MOVES), mandatoryCaptureViolated)

        // 3. Presa per somma quando sul tavolo c'è la carta uguale (viola 4.2).
        val sumInsteadOfEqual = play(state, ScopaMove(card("5D"), cards("2D", "3S").toSet()))
        assertEquals(PlayResult.Rejected(RejectReason.NOT_IN_LEGAL_MOVES), sumInsteadOfEqual)

        // 4. Presa con carte che non sono sul tavolo.
        val captureNotOnTable = play(state, ScopaMove(card("7C"), cards("5B", "2C").toSet()))
        assertEquals(PlayResult.Rejected(RejectReason.NOT_IN_LEGAL_MOVES), captureNotOnTable)

        // 5. Presa con somma errata.
        val wrongSum = play(state, ScopaMove(card("7C"), cards("2D", "3S").toSet()))
        assertEquals(PlayResult.Rejected(RejectReason.NOT_IN_LEGAL_MOVES), wrongSum)

        // Lo stato originale è rimasto invariato.
        assertEquals(snapshotBefore, state)

        // 6. Comportamento di confine F0-05: a DEAL_OVER, legalMoves() è vuota e play() dà Rejected(DEAL_OVER).
        val dealOverState = state.copy(
            hands = mapOf(Seat.P1 to emptyList(), Seat.P2 to emptyList()),
            deck = emptyList(),
            phase = DealPhase.DEAL_OVER,
        )
        assertTrue(legalMoves(dealOverState).isEmpty())
        assertEquals(
            PlayResult.Rejected(RejectReason.DEAL_OVER),
            play(dealOverState, ScopaMove(card("5D"), emptySet())),
        )
    }

    @Test
    fun spec3_5_spec10_500SeedsPlayingFirstLegalMoveCompletesFirstDealWith36PlaysAnd40CardsInvariant() {
        val canonicalDeckSet = fullDeck().toSet()

        for (seed in 0L until 500L) {
            var state = newMatch(ScopaConfig(), seed)
            assertInitialInvariants(state, canonicalDeckSet, seed)

            val initialDealer = state.dealer
            val cardsDealtEvents = mutableListOf<GameEvent.CardsDealt>()
            var dealEndedEvent: GameEvent.DealEnded? = null
            var movesPlayed = 0

            while (dealEndedEvent == null) {
                val legal = legalMoves(state)
                assertTrue(legal.isNotEmpty(), "legalMoves vuota durante PLAYING per seed=$seed, mossa=$movesPlayed")
                assertLegalMovesCanonicalOrder(legal, seed, movesPlayed)

                val chosenMove = legal.first()
                val currentMover = state.toMove
                val result = play(state, chosenMove)
                assertTrue(result is PlayResult.Applied, "play ha rifiutato la prima mossa legale per seed=$seed")

                cardsDealtEvents.addAll(result.events.filterIsInstance<GameEvent.CardsDealt>())
                dealEndedEvent = result.events.filterIsInstance<GameEvent.DealEnded>().singleOrNull()
                state = result.state
                movesPlayed++

                if (dealEndedEvent == null) {
                    assertEquals(currentMover.other(), state.toMove, "Il turno non si è alternato per seed=$seed")
                }
                assert40CardsInvariant(state, canonicalDeckSet, seed, movesPlayed)
            }

            // spec §3.5: 6 distribuzioni totali = 36 carte giocate in una smazzata a 2 giocatori.
            assertEquals(36, movesPlayed, "Numero di giocate != 36 per seed=$seed")
            assertEquals(
                listOf(2, 3, 4, 5, 6),
                cardsDealtEvents.map { it.distribution },
                "Eventi CardsDealt non corrispondono alle distribuzioni 2..6 per seed=$seed",
            )
            // Dopo la 36ª giocata (spec §7.5): o è iniziata una nuova smazzata con mazziere alternato,
            // oppure la partita è terminata (MATCH_OVER).
            if (state.phase == DealPhase.PLAYING) {
                assertEquals(initialDealer.other(), state.dealer, "Il mazziere non è cambiato per seed=$seed")
                assertEquals(1, state.distribution, "Nuova smazzata non in distribuzione 1 per seed=$seed")
            } else {
                assertEquals(DealPhase.MATCH_OVER, state.phase, "Fase inattesa per seed=$seed")
            }
        }
    }

    private fun assertInitialInvariants(state: ScopaState, canonicalDeckSet: Set<Card>, seed: Long) {
        assertEquals(DealPhase.PLAYING, state.phase, "Fase iniziale != PLAYING per seed=$seed")
        assertEquals(1, state.distribution, "Distribuzione iniziale != 1 per seed=$seed")
        assertTrue(state.captured(Seat.P1).isEmpty())
        assertTrue(state.captured(Seat.P2).isEmpty())
        assertEquals(0, state.sweeps.getValue(Seat.P1))
        assertEquals(0, state.sweeps.getValue(Seat.P2))
        assertNull(state.lastCapturer)
        assert40CardsInvariant(state, canonicalDeckSet, seed, step = 0)
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

    private fun assertLegalMovesCanonicalOrder(moves: List<ScopaMove>, seed: Long, step: Int) {
        for (move in moves) {
            val captureList = move.capture.toList()
            assertEquals(
                captureList.sortedWith(CardOrder),
                captureList,
                "Presa non in ordine canonico per seed=$seed, step=$step: $captureList",
            )
        }
        for (i in 0 until moves.size - 1) {
            val a = moves[i]
            val b = moves[i + 1]
            val cardCmp = CardOrder.compare(a.card, b.card)
            assertTrue(
                cardCmp <= 0,
                "Mosse non ordinate per carta giocata per seed=$seed, step=$step: $a prima di $b",
            )
        }
    }

    private fun customState(
        config: ScopaConfig = ScopaConfig(),
        dealer: Seat = Seat.P1,
        toMove: Seat = Seat.P2,
        p1Hand: List<Card>,
        p2Hand: List<Card>,
        table: List<Card>,
        deck: List<Card> = emptyList(),
        phase: DealPhase = DealPhase.PLAYING,
        distribution: Int = 1,
        capturedBy: Map<Seat, List<Card>> = mapOf(Seat.P1 to emptyList(), Seat.P2 to emptyList()),
        sweeps: Map<Seat, Int> = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        lastCapturer: Seat? = null,
    ): ScopaState = ScopaState(
        config = config,
        dealer = dealer,
        toMove = toMove,
        hands = mapOf(Seat.P1 to p1Hand, Seat.P2 to p2Hand),
        table = table,
        deck = deck,
        rngState = 0L,
        phase = phase,
        distribution = distribution,
        capturedBy = capturedBy,
        sweeps = sweeps,
        lastCapturer = lastCapturer,
    )
}
