package it.maestro.ai.bot

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import kotlin.reflect.KFunction4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BotTest {

    @Test
    fun botChooseMoveSignatureAcceptsOnlyScopaViewLegalMovesAndSeededRng() {
        // Verifica statica del tipo della funzione Bot::chooseMove:
        // riceve (Bot, ScopaView, List<ScopaMove>, SeededRng) e restituisce ScopaMove.
        val chooseMoveRef: KFunction4<Bot, ScopaView, List<ScopaMove>, SeededRng, ScopaMove> =
            Bot::chooseMove

        val state = newMatch(config = ScopaConfig(), seed = 1L)
        val view = viewFor(state, state.toMove)
        val legal = legalMoves(state)

        val randomFromInstance = chooseMoveRef(RandomBot(), view, legal, SeededRng(10L))
        val randomFromObject = chooseMoveRef(RandomBot, view, legal, SeededRng(10L))
        assertEquals(randomFromInstance, randomFromObject)
        assertTrue(randomFromInstance in legal)

        val greedyFromInstance = chooseMoveRef(GreedyBot(), view, legal, SeededRng(10L))
        val greedyFromObject = chooseMoveRef(GreedyBot, view, legal, SeededRng(10L))
        assertEquals(greedyFromInstance, greedyFromObject)
        assertTrue(greedyFromInstance in legal)
    }

    @Test
    fun randomBotAndGreedyBotRejectEmptyLegalMovesList() {
        val view = customView(
            hand = cards("7D"),
            table = cards("7C"),
        )
        assertFailsWith<IllegalArgumentException> {
            RandomBot().chooseMove(view, emptyList(), SeededRng(1L))
        }
        assertFailsWith<IllegalArgumentException> {
            GreedyBot().chooseMove(view, emptyList(), SeededRng(1L))
        }
    }

    @Test
    fun randomBotAndGreedyBotAreDeterministicForSameViewAndSeedAcross200Matches() {
        val randomBot = RandomBot()
        val greedyBot = GreedyBot()

        for (seed in 0L until 200L) {
            var state = newMatch(config = ScopaConfig(), seed = seed)
            var turn = 0

            // Verifica su tutta la prima smazzata (36 giocate) di 200 partite distinte.
            while (turn < 36) {
                val view = viewFor(state, state.toMove)
                val legal = legalMoves(state)
                val botSeed = seed * 1_000L + turn

                val r1 = randomBot.chooseMove(view, legal, SeededRng(botSeed))
                val r2 = randomBot.chooseMove(view, legal, SeededRng(botSeed))
                val r3 = RandomBot.chooseMove(view, legal, SeededRng(botSeed))
                assertEquals(r1, r2, "RandomBot non deterministico per seed=$seed, turn=$turn")
                assertEquals(r1, r3, "RandomBot companion non coerente per seed=$seed, turn=$turn")
                assertTrue(r1 in legal, "RandomBot ha scelto una mossa non legale per seed=$seed, turn=$turn")

                val g1 = greedyBot.chooseMove(view, legal, SeededRng(botSeed))
                val g2 = greedyBot.chooseMove(view, legal, SeededRng(0L))
                val g3 = GreedyBot.chooseMove(view, legal, SeededRng(999_999L))
                assertEquals(g1, g2, "GreedyBot non deterministico per seed=$seed, turn=$turn")
                assertEquals(g1, g3, "GreedyBot companion non coerente per seed=$seed, turn=$turn")
                assertTrue(g1 in legal, "GreedyBot ha scelto una mossa non legale per seed=$seed, turn=$turn")

                // Avanza la partita alternando le scelte dei due bot.
                val chosen = if (turn % 2 == 0) g1 else r1
                state = (play(state, chosen) as PlayResult.Applied).state
                turn++
            }
        }
    }

    @Test
    fun randomBotExploresAllLegalMovesWithSameRngSequence() {
        val view = customView(
            hand = cards("AD", "2C", "3S"),
            table = cards("RB", "CC"),
        )
        val legal = listOf(
            ScopaMove(card = card("AD"), capture = emptySet()),
            ScopaMove(card = card("2C"), capture = emptySet()),
            ScopaMove(card = card("3S"), capture = emptySet()),
        )
        val rng = SeededRng(42L)
        val chosenCounts = mutableMapOf<ScopaMove, Int>()

        repeat(300) {
            val move = RandomBot.chooseMove(view, legal, rng)
            assertTrue(move in legal)
            chosenCounts[move] = (chosenCounts[move] ?: 0) + 1
        }

        assertEquals(legal.toSet(), chosenCounts.keys)
        for (move in legal) {
            val count = chosenCounts.getValue(move)
            assertTrue(count in 60..140, "Distribuzione imprevista per $move: $count su 300")
        }
    }

    @Test
    fun spec5_1_greedyBotPrefersSweepOverSettebelloMoreDenariAndMoreCards() {
        // Tavolo: 4C, 3S (somma 7).
        // Mano: 7S (prende {4C, 3S} e svuota il tavolo -> SCOPA!)
        // In una lista di mosse costruita a mano confrontiamo una mossa che fa scopa (senza denari né 7D)
        // contro una mossa ipotetica che cattura il settebello e molti denari ma non svuota il tavolo.
        val view = customView(
            hand = cards("7S", "RD"),
            table = cards("4C", "3S"),
            deckSize = 12,
            opponentHandSize = 2,
        )
        val nonSweepWithSettebelloAndDenari = ScopaMove(
            card = card("RD"),
            capture = cards("7D", "2D", "AD").toSet(), // 3 carte, 4 denari totali, include 7D, ma non svuota table
        )
        val sweepWithoutDenari = ScopaMove(
            card = card("7S"),
            capture = cards("4C", "3S").toSet(), // svuota il tavolo {4C, 3S} -> scopa!
        )

        val chosen = GreedyBot.chooseMove(
            view = view,
            legal = listOf(nonSweepWithSettebelloAndDenari, sweepWithoutDenari),
            rng = SeededRng(1L),
        )
        assertEquals(sweepWithoutDenari, chosen)
    }

    @Test
    fun spec5_2_greedyBotDoesNotTreatTableClearOnLastPlayOfDealAsSweep() {
        // Ultima giocata dell'ultima distribuzione della smazzata:
        // deckSize == 0, hand.size == 1, opponentHandSize == 0.
        // Svuotare il tavolo NON vale scopa (spec §5.2), quindi vince il criterio successivo (settebello).
        val lastPlayView = customView(
            hand = cards("RS"),
            table = cards("3C", "7S"),
            deckSize = 0,
            opponentHandSize = 0,
        )
        val clearsTableWithoutSweepPoint = ScopaMove(
            card = card("RS"),
            capture = cards("3C", "7S").toSet(), // svuota table ma all'ultima giocata non fa scopa
        )
        val capturesSettebelloPartial = ScopaMove(
            card = card("RS"),
            capture = cards("7D", "3C").toSet(), // non svuota table, ma prende il settebello
        )

        val chosenAtLastPlay = GreedyBot.chooseMove(
            view = lastPlayView,
            legal = listOf(clearsTableWithoutSweepPoint, capturesSettebelloPartial),
            rng = SeededRng(1L),
        )
        assertEquals(capturesSettebelloPartial, chosenAtLastPlay)

        // Verifica di controllo: nella stessa identica posizione ma NON all'ultima giocata
        // (es. opponentHandSize == 1), svuotare il tavolo vale scopa e quindi ha priorità sul settebello.
        val nonFinalPlayView = lastPlayView.copy(opponentHandSize = 1)
        val chosenBeforeLastPlay = GreedyBot.chooseMove(
            view = nonFinalPlayView,
            legal = listOf(capturesSettebelloPartial, clearsTableWithoutSweepPoint),
            rng = SeededRng(1L),
        )
        assertEquals(clearsTableWithoutSweepPoint, chosenBeforeLastPlay)
    }

    @Test
    fun greedyBotPrefersCapturingSettebelloFromTableOverMoreDenariAndMoreCards() {
        // Tavolo con 5 carte: nessuna mossa fa scopa.
        // Mano: FC (valore 8) può prendere:
        // - {AD, 2D, 5D}: 3 carte di denari dal tavolo (3 denari, 4 carte totali), senza 7D;
        // - {7D, AC}: 2 carte dal tavolo (1 denaro, 3 carte totali), ma prende il 7D!
        val view = customView(
            hand = cards("FC"),
            table = cards("AD", "2D", "5D", "7D", "AC", "RB"),
        )
        val moreDenariAndCards = ScopaMove(
            card = card("FC"),
            capture = cards("AD", "2D", "5D").toSet(),
        )
        val capturesSettebello = ScopaMove(
            card = card("FC"),
            capture = cards("7D", "AC").toSet(),
        )

        val chosen = GreedyBot.chooseMove(
            view = view,
            legal = listOf(moreDenariAndCards, capturesSettebello),
            rng = SeededRng(1L),
        )
        assertEquals(capturesSettebello, chosen)
    }

    @Test
    fun greedyBotPrefersPlayingSettebelloFromHandInACaptureOverMoreDenariAndMoreCards() {
        // Spec §4.7: la carta giocata in una presa finisce nel mazzetto delle prese del giocatore.
        // Giocare il 7D dalla mano per prendere 7B assicura il Settebello nel proprio mazzetto,
        // quindi batte una presa con FC che cattura due denari {3D, 5D}.
        val view = customView(
            hand = cards("7D", "FC"),
            table = cards("3D", "5D", "7B", "RB"),
        )
        val twoDenariCapture = ScopaMove(
            card = card("FC"),
            capture = cards("3D", "5D").toSet(), // 2 denari presi (3D, 5D), ma niente 7D
        )
        val playSettebelloCapture = ScopaMove(
            card = card("7D"),
            capture = cards("7B").toSet(), // porta 7D nel mazzetto delle prese
        )

        val chosen = GreedyBot.chooseMove(
            view = view,
            legal = listOf(twoDenariCapture, playSettebelloCapture),
            rng = SeededRng(1L),
        )
        assertEquals(playSettebelloCapture, chosen)
    }

    @Test
    fun greedyBotPrefersMoreDenariOverMoreCardsWhenNoSweepAndNoSettebello() {
        // Nessuna scopa, nessun 7D.
        // FC (8) può prendere:
        // - {AC, 3S, 4B}: 3 carte dal tavolo, 0 denari;
        // - {2D, 6D}: 2 carte dal tavolo, 2 denari.
        val view = customView(
            hand = cards("FC"),
            table = cards("2D", "6D", "AC", "3S", "4B", "RB"),
        )
        val moreCardsZeroDenari = ScopaMove(
            card = card("FC"),
            capture = cards("AC", "3S", "4B").toSet(),
        )
        val fewerCardsTwoDenari = ScopaMove(
            card = card("FC"),
            capture = cards("2D", "6D").toSet(),
        )

        val chosen = GreedyBot.chooseMove(
            view = view,
            legal = listOf(moreCardsZeroDenari, fewerCardsTwoDenari),
            rng = SeededRng(1L),
        )
        assertEquals(fewerCardsTwoDenari, chosen)
    }

    @Test
    fun greedyBotCountsPlayedDenariCardWhenComparingDenariCaptured() {
        // A parità di denari sul tavolo (es. 0 denari sul tavolo), giocare una carta di Denari (6D -> prende 6C)
        // porta 1 denaro nel mazzetto delle prese, mentre giocare 6S -> prende {2C, 4B} porta 0 denari.
        val view = customView(
            hand = cards("6D", "6S"),
            table = cards("6C", "2C", "4B", "RB"),
        )
        val zeroDenariThreeCards = ScopaMove(
            card = card("6S"),
            capture = cards("2C", "4B").toSet(),
        )
        val oneDenaroTwoCards = ScopaMove(
            card = card("6D"),
            capture = cards("6C").toSet(),
        )

        val chosen = GreedyBot.chooseMove(
            view = view,
            legal = listOf(zeroDenariThreeCards, oneDenaroTwoCards),
            rng = SeededRng(1L),
        )
        assertEquals(oneDenaroTwoCards, chosen)
    }

    @Test
    fun greedyBotPrefersMoreCardsWhenSweepSettebelloAndDenariAreTied() {
        // Nessuna scopa, nessun 7D, 1 denaro in entrambe le prese.
        // FC (8) può prendere:
        // - {AD, 7C}: 1 denaro, 2 carte dal tavolo;
        // - {AD, 3C, 4S}: 1 denaro, 3 carte dal tavolo.
        val view = customView(
            hand = cards("FC"),
            table = cards("AD", "3C", "7C", "4S", "RB"),
        )
        val twoCardsCapture = ScopaMove(
            card = card("FC"),
            capture = cards("AD", "7C").toSet(),
        )
        val threeCardsCapture = ScopaMove(
            card = card("FC"),
            capture = cards("AD", "3C", "4S").toSet(),
        )

        val chosen = GreedyBot.chooseMove(
            view = view,
            legal = listOf(twoCardsCapture, threeCardsCapture),
            rng = SeededRng(1L),
        )
        assertEquals(threeCardsCapture, chosen)
    }

    @Test
    fun greedyBotBreaksCaptureTiesByChoosingFirstInLegalMoves() {
        // Due prese con identico punteggio lessicografico (no scopa, no 7D, 0 denari, 2 carte dal tavolo).
        // Deve restituire la prima in `legal`.
        val view = customView(
            hand = cards("FC"),
            table = cards("2C", "6C", "3S", "5B", "RB"),
        )
        val firstCapture = ScopaMove(
            card = card("FC"),
            capture = cards("2C", "6C").toSet(),
        )
        val secondCapture = ScopaMove(
            card = card("FC"),
            capture = cards("3S", "5B").toSet(),
        )

        assertEquals(
            firstCapture,
            GreedyBot.chooseMove(view, listOf(firstCapture, secondCapture), SeededRng(1L)),
        )
        assertEquals(
            secondCapture,
            GreedyBot.chooseMove(view, listOf(secondCapture, firstCapture), SeededRng(1L)),
        )
    }

    @Test
    fun spec1_2_greedyBotDiscardsLowestCaptureValueCardAndBreaksTiesByFirstInLegalMoves() {
        // Nessuna mossa effettua una presa: deve scartare la carta con valore di presa (Rank.value) più basso.
        val view = customView(
            hand = cards("FD", "2C", "RS"),
            table = cards("7B"),
        )
        val discardFante = ScopaMove(card = card("FD"), capture = emptySet()) // valore 8
        val discardDue = ScopaMove(card = card("2C"), capture = emptySet()) // valore 2 (minimo)
        val discardRe = ScopaMove(card = card("RS"), capture = emptySet()) // valore 10

        val chosenLowest = GreedyBot.chooseMove(
            view = view,
            legal = listOf(discardFante, discardDue, discardRe),
            rng = SeededRng(1L),
        )
        assertEquals(discardDue, chosenLowest)

        // Parità sul valore minimo (tre carte di valore 2): sceglie la prima in `legalMoves`.
        val tieView = customView(
            hand = cards("2D", "2C", "2S"),
            table = cards("7B"),
        )
        val discard2D = ScopaMove(card = card("2D"), capture = emptySet())
        val discard2C = ScopaMove(card = card("2C"), capture = emptySet())
        val discard2S = ScopaMove(card = card("2S"), capture = emptySet())

        assertEquals(
            discard2D,
            GreedyBot.chooseMove(tieView, listOf(discard2D, discard2C, discard2S), SeededRng(1L)),
        )
        assertEquals(
            discard2C,
            GreedyBot.chooseMove(tieView, listOf(discard2C, discard2D, discard2S), SeededRng(1L)),
        )
    }

    private fun customView(
        hand: List<Card>,
        table: List<Card>,
        deckSize: Int = 20,
        opponentHandSize: Int = 3,
    ): ScopaView = ScopaView(
        seat = Seat.P1,
        config = ScopaConfig(),
        dealer = Seat.P2,
        toMove = Seat.P1,
        phase = DealPhase.PLAYING,
        hand = hand,
        table = table,
        capturedBy = mapOf(Seat.P1 to emptyList(), Seat.P2 to emptyList()),
        playedCards = emptyList(),
        opponentHandSize = opponentHandSize,
        deckSize = deckSize,
        sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
        totals = mapOf(Seat.P1 to 0, Seat.P2 to 0),
    )

    private fun card(notation: String): Card {
        require(notation.length == 2) { "Notazione non valida: $notation" }
        val rank = when (notation[0]) {
            'A' -> Rank.ASSO
            '2' -> Rank.DUE
            '3' -> Rank.TRE
            '4' -> Rank.QUATTRO
            '5' -> Rank.CINQUE
            '6' -> Rank.SEI
            '7' -> Rank.SETTE
            'F' -> Rank.FANTE
            'C' -> Rank.CAVALLO
            'R' -> Rank.RE
            else -> error("Valore non valido in $notation")
        }
        val suit = when (notation[1]) {
            'D' -> Suit.DENARI
            'C' -> Suit.COPPE
            'S' -> Suit.SPADE
            'B' -> Suit.BASTONI
            else -> error("Seme non valido in $notation")
        }
        return Card(rank = rank, suit = suit)
    }

    private fun cards(vararg notations: String): List<Card> = notations.map(::card)
}
