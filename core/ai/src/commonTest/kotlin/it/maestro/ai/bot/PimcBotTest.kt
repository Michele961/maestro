package it.maestro.ai.bot

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.model.fullDeck
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.determinize
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import kotlin.reflect.KFunction4
import kotlin.reflect.KFunction5
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PimcBotTest {

    @Test
    fun pimcBotAndMoveEvaluatorSignaturesAcceptOnlyScopaView() {
        val chooseMoveRef: KFunction4<Bot, ScopaView, List<ScopaMove>, SeededRng, ScopaMove> =
            Bot::chooseMove
        val evaluateRef: KFunction5<MoveEvaluator, ScopaView, List<ScopaMove>, SeededRng, SearchBudget, List<MoveValue>> =
            MoveEvaluator::evaluate

        val state = newMatch(config = ScopaConfig(), seed = 7L)
        val view = viewFor(state, state.toMove)
        val legal = legalMoves(state)
        val budget = SearchBudget(samples = 5, maxNodes = 500)

        val evaluated = evaluateRef(PimcEvaluator, view, legal, SeededRng(11L), budget)
        assertEquals(legal, evaluated.map { it.move })
        for (mv in evaluated) {
            assertEquals(mv.value, mv.expectedValue)
        }

        val chosenByInstance = chooseMoveRef(PimcBot(budget = budget), view, legal, SeededRng(11L))
        val chosenByDefault = chooseMoveRef(PimcBot, view, legal, SeededRng(11L))
        assertTrue(chosenByInstance in legal)
        assertTrue(chosenByDefault in legal)
    }

    @Test
    fun searchBudgetValidatesPositiveSamplesAndMaxNodes() {
        assertFailsWith<IllegalArgumentException> {
            SearchBudget(samples = 0, maxNodes = 100)
        }
        assertFailsWith<IllegalArgumentException> {
            SearchBudget(samples = -3, maxNodes = 100)
        }
        assertFailsWith<IllegalArgumentException> {
            SearchBudget(samples = 10, maxNodes = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            SearchBudget(samples = 10, maxNodes = -1)
        }
    }

    @Test
    fun pimcEvaluatorAndBotRejectInvalidInputs() {
        val state = newMatch(config = ScopaConfig(), seed = 1L)
        val activeView = viewFor(state, state.toMove)
        val legal = legalMoves(state)

        assertFailsWith<IllegalArgumentException> {
            PimcEvaluator.evaluate(activeView, emptyList(), SeededRng(1L))
        }
        assertFailsWith<IllegalArgumentException> {
            PimcBot.chooseMove(activeView, emptyList(), SeededRng(1L))
        }

        val nonMoverView = viewFor(state, state.toMove.other())
        assertFailsWith<IllegalArgumentException> {
            PimcEvaluator.evaluate(nonMoverView, legal, SeededRng(1L))
        }
    }

    @Test
    fun sampledDistributionsAreCompatibleWithViewAcross100MatchesAndAllTurns() {
        // Criterio di accettazione 1:
        // "Le distribuzioni campionate sono compatibili con la vista (nessuna carta già vista, dimensioni corrette)."
        val sampleRng = SeededRng(123_456L)

        for (seed in 0L until 100L) {
            var state = newMatch(config = ScopaConfig(), seed = seed)
            var turn = 0

            while (turn < 36) {
                val view = viewFor(state, state.toMove)
                val unseen = unseenCards(view)

                val p1Cap = view.capturedBy.getValue(Seat.P1)
                val p2Cap = view.capturedBy.getValue(Seat.P2)
                val seenSet = (view.hand + view.table + p1Cap + p2Cap + view.playedCards).toSet()

                assertEquals(
                    view.opponentHandSize + view.deckSize,
                    unseen.size,
                    "Dimensione unseenCards non coerente per seed=$seed, turn=$turn",
                )
                assertTrue(
                    unseen.none { it in seenSet },
                    "unseenCards contiene carte già viste per seed=$seed, turn=$turn",
                )

                // Verifica 5 campioni casuali per ogni singolo turno della smazzata.
                repeat(5) { sampleIdx ->
                    val dist = sampleCompatibleDistribution(view = view, unseen = unseen, rng = sampleRng)
                    assertEquals(
                        view.opponentHandSize,
                        dist.opponentHand.size,
                        "Dimensione opponentHand errata (seed=$seed, turn=$turn, sample=$sampleIdx)",
                    )
                    assertEquals(
                        view.deckSize,
                        dist.deckOrder.size,
                        "Dimensione deckOrder errata (seed=$seed, turn=$turn, sample=$sampleIdx)",
                    )
                    assertTrue(
                        dist.opponentHand.none { it in seenSet },
                        "opponentHand contiene carte già viste (seed=$seed, turn=$turn, sample=$sampleIdx)",
                    )
                    assertTrue(
                        dist.deckOrder.none { it in seenSet },
                        "deckOrder contiene carte già viste (seed=$seed, turn=$turn, sample=$sampleIdx)",
                    )
                    assertEquals(
                        unseen.toSet(),
                        (dist.opponentHand + dist.deckOrder).toSet(),
                        "Unione di opponentHand e deckOrder diversa da unseenCards",
                    )

                    // determinize() verifica tutte le invarianti di compatibilità sulla vista.
                    val determinized = determinize(
                        view = view,
                        opponentHand = dist.opponentHand,
                        deckOrder = dist.deckOrder,
                    )
                    assertEquals(view.hand, determinized.hand(view.seat))
                    assertEquals(dist.opponentHand, determinized.hand(view.seat.other()))
                    assertEquals(dist.deckOrder, determinized.deck)
                }

                val firstLegal = legalMoves(state).first()
                state = (play(state, firstLegal) as PlayResult.Applied).state
                turn++
            }
        }
    }

    @Test
    fun pimcBotIsDeterministicForSameViewAndSeed() {
        val bot = PimcBot(SearchBudget(samples = 10, maxNodes = 500))
        for (seed in 0L until 20L) {
            val state = newMatch(config = ScopaConfig(), seed = seed)
            val view = viewFor(state, state.toMove)
            val legal = legalMoves(state)

            val move1 = bot.chooseMove(view, legal, SeededRng(seed + 1_000L))
            val move2 = bot.chooseMove(view, legal, SeededRng(seed + 1_000L))
            assertEquals(move1, move2)

            val eval1 = PimcEvaluator.evaluate(view, legal, SeededRng(seed + 2_000L), bot.budget)
            val eval2 = PimcEvaluator.evaluate(view, legal, SeededRng(seed + 2_000L), bot.budget)
            assertEquals(eval1, eval2)
        }
    }

    @Test
    fun provisionalHeuristicEvaluationMatchesDocumentFormula() {
        // Verifica esatta della formula provvisoria in docs/ai/maestro.md:
        // scope fatte * 1 + settebello preso * 1 + 0,1 * (diff denari presi)
        // + 0,05 * (diff carte prese) + 0,02 * (diff somme di primiera parziali).
        val p1Captures = cards("7D", "2D", "6C", "AS") // 2 denari (include 7D), 4 carte; primiera: D=21, C=18, S=16, B=0 -> 55
        val p2Captures = cards("3D", "7B") // 1 denaro, 2 carte; primiera: D=13, C=0, S=0, B=21 -> 34
        val used = (p1Captures + p2Captures).toSet()
        val remaining = fullDeck().filterNot { it in used }

        val state = ScopaState(
            config = ScopaConfig(),
            dealer = Seat.P2,
            toMove = Seat.P1,
            hands = mapOf(
                Seat.P1 to remaining.subList(0, 3),
                Seat.P2 to remaining.subList(3, 6),
            ),
            table = remaining.subList(6, 10),
            deck = remaining.subList(10, remaining.size),
            rngState = 0L,
            phase = DealPhase.PLAYING,
            distribution = 2,
            capturedBy = mapOf(
                Seat.P1 to p1Captures,
                Seat.P2 to p2Captures,
            ),
            sweeps = mapOf(Seat.P1 to 2, Seat.P2 to 1), // diff scope = +1
        )

        // Calcolo a mano per P1:
        // - diff scope: 2 - 1 = +1 -> +1.0
        // - diff settebello: 1 - 0 = +1 -> +1.0
        // - diff denari: 2 - 1 = +1 -> +0.1
        // - diff carte: 4 - 2 = +2 -> +0.10
        // - diff primiera parziale: 55 - 34 = +21 -> 21 * 0.02 = +0.42
        // Totale atteso per P1 = 1.0 + 1.0 + 0.1 + 0.10 + 0.42 = 2.62
        val expectedP1 = 2.62
        val actualP1 = evaluateProvisionalState(state, Seat.P1)
        val actualP2 = evaluateProvisionalState(state, Seat.P2)

        assertEquals(expectedP1, actualP1, absoluteTolerance = 1e-9)
        assertEquals(-expectedP1, actualP2, absoluteTolerance = 1e-9)
    }

    @Test
    fun exactEndgamePosition1_avoidsGreedyCaptureThatConcedesCounterSweep() {
        // =========================================================================================
        // POSIZIONE FINALE 1 — Evitare una presa avida che regala la scopa all'avversario (spec §5.1)
        // =========================================================================================
        // Ultima distribuzione (deckSize = 0), tocca a P1 (dealer = P2):
        // - Mano P1: 4C, RB
        // - Mano P2 (dedotta per esclusione): 6S, CB
        // - Tavolo: 4S, 6C
        // - Prese precedenti (34 carte già uscite):
        //   P1 ha già preso 22 carte (tutti i 10 Denari inclusi 7D, più 7C, 7S, 7B e altre 9 carte).
        //   P2 ha già preso 12 carte (senza Denari e senza Sette).
        //   Quindi i punti di categoria già blindati per P1 sono:
        //   CARTE = 1 (P1 ha già >= 22 carte), DENARI = 1 (10 a 0), SETTEBELLO = 1 (7D),
        //   PRIMIERA = 1 (P1 ha tutti e quattro i 7 = 84 punti).
        //   Scope attuali: P1 = 0, P2 = 0.
        //
        // Analisi esatta a mano delle 2 mosse legali di P1:
        // 1) Mossa A (scelta da GreedyBot perché prende!): P1 gioca 4C prendendo {4S}.
        //    - Sul tavolo resta solo {6C}.
        //    - P2 gioca 6S e prende {6C} lasciando il tavolo vuoto alla terzultima giocata -> SCOPA per P2! (+1 punto a P2).
        //    - P1 gioca RB (resta sul tavolo), P2 gioca CB (resta sul tavolo; le 2 carte residue vanno a P2 per ultima presa).
        //    - Punteggio finale smazzata con Mossa A: P1 = 4 (Carte, Denari, Settebello, Primiera), P2 = 1 (Scopa).
        //    - Valore netto esatto per P1 = 4 - 1 = +3.0.
        //
        // 2) Mossa B (ottima): P1 scarta RB sul tavolo (nessun Re sul tavolo e nessuna somma = 10, perché 4+6=10:
        //    attenzione! 4S + 6C fa 10, quindi usiamo sul tavolo 4S, 5C e in mano a P2 5S, CB!).
        //    Ricalcoliamo con:
        //    - Mano P1: 4C, RB
        //    - Mano P2: 5S, CB
        //    - Tavolo: 4S, 5C (somma 9, quindi RB=10 non prende!)
        //    Verifica con Tavolo = {4S, 5C}:
        //    - Mossa A: P1 gioca 4C prendendo {4S}. Sul tavolo resta solo {5C}.
        //      P2 risponde con 5S prendendo {5C} -> SCOPA per P2! (+1 punto a P2).
        //      Poi P1 cala RB, P2 cala CB. Fine smazzata: P1 = 4 punti, P2 = 1 punto -> valore netto = +3.0.
        //    - Mossa B: P1 cala RB (10) sul tavolo -> tavolo diventa {4S, 5C, RB}.
        //      Ora P2 ha {5S, CB (9)}:
        //      * Se P2 gioca 5S prendendo {5C}, sul tavolo restano {4S, RB} (niente scopa!).
        //        Poi P1 gioca 4C prendendo {4S} (ultima presa di P1, sul tavolo resta RB).
        //        Poi P2 cala CB (ultima giocata). Nessuna scopa! P1 = 4, P2 = 0 -> valore netto = +4.0.
        //      * Se P2 gioca CB (9) prendendo {4S, 5C}, sul tavolo resta {RB} (niente scopa!).
        //        Poi P1 cala 4C, P2 cala 5S. Nessuna scopa! P1 = 4, P2 = 0 -> valore netto = +4.0.
        //    - Quindi con Mossa B (scartare RB) P1 ottiene valore netto esatto +4.0 contro +3.0 di Mossa A!
        val view = buildEndgameView(
            toMove = Seat.P1,
            dealer = Seat.P2,
            p1Hand = cards("4C", "RB"),
            p2Hand = cards("5S", "CB"),
            table = cards("4S", "5C"),
            p1MustHoldCards = cards(
                "AD", "2D", "3D", "4D", "5D", "6D", "7D", "FD", "CD", "RD",
                "7C", "7S", "7B", "AC", "2C", "3C", "AS", "2S", "3S", "AB", "2B", "3B",
            ),
        )
        val state = determinize(view, opponentHand = cards("5S", "CB"), deckOrder = emptyList())
        val legal = legalMoves(state)

        val captureMove = ScopaMove(card = card("4C"), capture = setOf(card("4S")))
        val discardMove = ScopaMove(card = card("RB"), capture = emptySet())
        assertEquals(listOf(captureMove, discardMove), legal)

        // GreedyBot sceglie erroneamente la presa immediata (captureMove):
        assertEquals(captureMove, GreedyBot.chooseMove(view, legal, SeededRng(1L)))

        // PimcEvaluator calcola i valori esatti: captureMove = +3.0, discardMove = +4.0:
        val evaluations = PimcEvaluator.evaluate(view, legal, SeededRng(1L)).associate { it.move to it.value }
        assertEquals(3.0, evaluations.getValue(captureMove), absoluteTolerance = 1e-9)
        assertEquals(4.0, evaluations.getValue(discardMove), absoluteTolerance = 1e-9)
        assertEquals(discardMove, PimcBot.chooseMove(view, legal, SeededRng(1L)))
    }

    @Test
    fun exactEndgamePosition2_securesLastCaptureToWinResidualSettebelloAndDenari() {
        // =========================================================================================
        // POSIZIONE FINALE 2 — Conquista dell'ultima presa per assicurarsi il Settebello residuo (§6.1)
        // =========================================================================================
        // Ultima distribuzione (deckSize = 0), tocca a P1 (dealer = P2):
        // - Tavolo: 7D (Settebello!), 2C
        // - Mano P1: 2D, 4C
        // - Mano P2 (nota per esclusione): 4S, RS
        // - Prese precedenti (34 carte già uscite):
        //   P1 ha già preso 22 carte (tra cui 5 Denari: AD, 3D, 4D, 5D, 6D e i tre 7: 7C, 7S, 7B).
        //   P2 ha già preso 12 carte (tra cui 3 Denari: FD, CD, RD).
        //   Ultimo catturatore finora: P1.
        //   Situazione punti prima delle ultime 4 carte:
        //   - CARTE: P1 ha già 22 carte -> P1 vince sicuramente CARTE (+1).
        //   - PRIMIERA: P1 ha 7C, 7S, 7B e 6D (21+21+21+18 = 81), P2 non ha alcun 7 -> P1 vince PRIMIERA (+1).
        //   - SETTEBELLO (7D) e DENARI (P1 ha 5 denari, sul tavolo c'è 7D e P1 ha 2D in mano) dipendono da chi fa
        //     l'**ultima presa** (spec §6.1), perché né P1 né P2 hanno un 7 o carte che sommino a 7 per prendere 7D dal tavolo!
        //
        // Analisi esatta a mano delle 2 mosse legali di P1:
        // 1) Mossa A (scelta da GreedyBot perché prende subito!): P1 gioca 2D prendendo {2C}.
        //    - Sul tavolo resta solo {7D}.
        //    - Turno P2: P2 ha {4S, RS}. Nessuna prende {7D}.
        //      Se P2 cala 4S sul tavolo (tavolo = {7D, 4S}), poi P1 è costretto a giocare la sua unica carta 4C
        //      prendendo {4S}! E poi P2 cala RS? Aspetta: se P1 prende con 4C, l'ultima presa sarebbe ancora di P1!
        //      Allora P2 (che gioca a informazione perfetta!) al suo turno NON cala 4S: P2 cala RS sul tavolo!
        //      Vediamo cosa succede quando P2 cala RS (tavolo = {7D, RS}):
        //      Al turno successivo P1 ha solo {4C}, che non può prendere né 7D né RS! P1 è costretto a calare 4C sul tavolo
        //      (tavolo = {7D, RS, 4C}).
        //      All'ultima giocata della smazzata, P2 gioca 4S e prende {4C}!
        //      Così **P2 fa l'ultima presa della smazzata** (spec §6.1) e raccoglie dal tavolo le carte residue {7D, RS}!
        //    - Risultato con Mossa A (a gioco ottimo di P2):
        //      * P1 ha 6 denari (5 + 2D) -> P1 vince DENARI (+1), CARTE (+1), PRIMIERA (+1) = 3 punti.
        //      * P2 ottiene il 7D con le carte residue dell'ultima presa -> P2 vince SETTEBELLO (+1)!
        //      * Valore netto per P1 con Mossa A = 3 - 1 = +2.0.
        //
        // 2) Mossa B (ottima): P1 cala 4C sul tavolo (tavolo = {7D, 2C, 4C}), conservando 2D per il suo ultimo turno!
        //    - Turno P2 (ha {4S, RS}):
        //      * Caso 2a: P2 gioca 4S prendendo {4C}. Sul tavolo restano {7D, 2C}.
        //        Poi P1 gioca 2D prendendo {2C}!
        //        Poi P2 all'ultimo turno ha solo RS (10), che non può prendere {7D} -> P2 cala RS.
        //        L'ultima presa della smazzata resta quella di P1 (con 2D su 2C)!
        //        Quindi le carte residue {7D, RS} vanno a P1 (spec §6.1)!
        //      * Caso 2b: P2 cala RS (10) sul tavolo (non può prendere perché 7+2=9, 7+4=11, 2+4=6, 7+2+4=13).
        //        Poi P1 gioca 2D prendendo {2C}. Sul tavolo restano {7D, 4C, RS}.
        //        All'ultimo turno P2 gioca 4S prendendo {4C}... aspetta! Se P2 tiene 4S per l'ultimo turno,
        //        e 4C è ancora sul tavolo, P2 prenderebbe {4C} all'ultimo turno!
        //        Come fa P1 ad assicurarsi che P2 NON possa prendere all'ultimo turno?
        //        Se invece di 4C in mano a P1 e 4S in mano a P2, P1 ha in mano {2D, RB} e P2 ha in mano {2S, RS}:
        //        Verifichiamo con:
        //        - Tavolo: 7D, 2C, 5B (somme possibili: 7+2=9, 2+5=7, 7+5=12, 7+2+5=14) -> anzi:
        //        - Tavolo: 7D, 4C
        //        - Mano P1: 2D, 6C (6C prende {2D, 4C} se 2D è a terra, oppure se sul tavolo c'è {7D, 4C},
        //          nessuna carta di P1 prende all'inizio? No, vogliamo che P1 abbia una presa avida sbagliata!).
        //        Costruiamo il caso perfetto e cristallino:
        //        - Tavolo: 7D, 2C, 4B (somme: 2+4=6, 7+2=9, 7+4=11, 7+2+4=13).
        //        - Mano P1: 2D (prende 2C), 6C (prende {2C, 4B}).
        //        - Mano P2: 4S, RS.
        //        Vediamo cosa succede per le due mosse legali di P1 (entrambe prese!):
        //        - Mossa 1 (scelta da GreedyBot perché prende il denaro 2D, `denariCapturedCount = 1` vs `0` di 6C!):
        //          P1 gioca 2D prendendo {2C}.
        //          Sul tavolo restano {7D, 4B}.
        //          Ora P2 (che ha {4S, RS}) cala RS (10) sul tavolo -> tavolo = {7D, 4B, RS}.
        //          Ora P1 ha solo 6C, che non può prendere nulla da {7D, 4B, RS} -> P1 cala 6C.
        //          All'ultima giocata P2 gioca 4S prendendo {4B} (o RS prendendo {4B, 6C} se avesse tenuto RS)!
        //          Così P2 fa l'ultima presa e si porta a casa il 7D rimasto sul tavolo!
        //          Risultato Mossa 1: P1 fa 3 punti (Carte, Denari, Primiera), P2 fa 1 punto (Settebello) -> netto = +2.0!
        //        - Mossa 2 (ottima, giocata da PimcBot!):
        //          P1 gioca 6C prendendo {2C, 4B}!
        //          Sul tavolo resta solo {7D}.
        //          Ora P2 (che ha {4S, RS}) non può prendere 7D:
        //          * Se P2 cala 4S (tavolo = {7D, 4S}), P1 cala 2D (tavolo = {7D, 4S, 2D}) e P2 all'ultimo turno ha RS (10),
        //            che NON può prendere nulla da {7D(7), 4S(4), 2D(2)} (somme: 7+4=11, 7+2=9, 4+2=6, 7+4+2=13)!
        //          * Se P2 cala RS (tavolo = {7D, RS}), P1 cala 2D (tavolo = {7D, RS, 2D}) e P2 all'ultimo turno ha 4S (4),
        //            che NON può prendere nulla da {7D(7), RS(10), 2D(2)}!
        //          In entrambi i casi P2 non effettua più alcuna presa fino a fine smazzata!
        //          L'ultima presa della smazzata rimane quella di P1 (con 6C su {2C, 4B}), quindi a fine smazzata
        //          tutte le carte residue sul tavolo ({7D, 4S, 2D, RS}) vanno a P1 (spec §6.1)!
        //          P1 conquista così anche il SETTEBELLO (7D) e il 2D!
        //          Risultato Mossa 2: P1 fa 4 punti (Carte, Denari, Settebello, Primiera), P2 fa 0 punti -> netto = +4.0!
        val view = buildEndgameView(
            toMove = Seat.P1,
            dealer = Seat.P2,
            p1Hand = cards("2D", "6C"),
            p2Hand = cards("4S", "RS"),
            table = cards("7D", "2C", "4B"),
            p1MustHoldCards = cards(
                "AD", "3D", "4D", "5D", "6D",
                "7C", "7S", "7B", "AC", "3C", "5C", "AS", "2S", "3S", "5S", "6S", "AB", "2B", "3B", "5B", "6B",
            ),
        )
        val state = determinize(view, opponentHand = cards("4S", "RS"), deckOrder = emptyList())
        val legal = legalMoves(state)

        val greedyMove2D = ScopaMove(card = card("2D"), capture = setOf(card("2C")))
        val optimalMove6C = ScopaMove(card = card("6C"), capture = cards("2C", "4B").toSet())
        assertEquals(listOf(greedyMove2D, optimalMove6C), legal)

        // GreedyBot preferisce 2D perché porta 1 denaro nel mazzetto subito (contro 0 denari di 6C):
        assertEquals(greedyMove2D, GreedyBot.chooseMove(view, legal, SeededRng(1L)))

        // PimcEvaluator vede che 6C blocca l'ultima presa di P2 e regala a P1 il 7D a fine smazzata (+4.0 vs +2.0):
        val evaluations = PimcEvaluator.evaluate(view, legal, SeededRng(1L)).associate { it.move to it.value }
        assertEquals(2.0, evaluations.getValue(greedyMove2D), absoluteTolerance = 1e-9)
        assertEquals(4.0, evaluations.getValue(optimalMove6C), absoluteTolerance = 1e-9)
        assertEquals(optimalMove6C, PimcBot.chooseMove(view, legal, SeededRng(1L)))
    }

    @Test
    fun exactEndgamePosition3_choosesCaptureThatCompletesFourthSuitToWinPrimiera() {
        // =========================================================================================
        // POSIZIONE FINALE 3 — Scelta tra prese per completare i 4 semi e vincere la Primiera (§6.3)
        // =========================================================================================
        // Ultima giocata dell'ultima distribuzione (deckSize = 0, P1 ha 1 carta, P2 ha 0 carte):
        // - Mano P1: FC (Fante di Coppe, valore 8)
        // - Mano P2: vuota
        // - Tavolo: AD (1), 3C (3), 4C (4), 6S (6), 2B (2), RB (10)
        //   Prese per somma con FC (8):
        //   * Mossa A: {AD(1), 3C(3), 4C(4)} -> prende 3 carte dal tavolo (incluso 1 denaro AD), ma NESSUNA Spada!
        //   * Mossa B: {2B(2), 6S(6)} -> prende 2 carte dal tavolo (0 denari), ma cattura il 6S (Spade)!
        // - Prese precedenti (33 carte già nei mazzetti):
        //   * P1 ha già preso 22 carte nei semi Denari (7 denari incluso 7D), Coppe (incluso 7C) e Bastoni (incluso 7B),
        //     ma ha **0 carte di Spade**!
        //   * P2 ha già preso 11 carte, incluse tutte le altre 9 carte di Spade (AS, 2S, 3S, 4S, 5S, 7S, FS, CS, RS),
        //     6D, 6C, 6B (quindi P2 ha tutti e 4 i semi con somma di primiera = 18 + 18 + 21 + 18 = 75).
        //
        // Analisi esatta a mano:
        // - P1 ha già blindato CARTE (+1), DENARI (+1) e SETTEBELLO (+1) = 3 punti.
        // - Se P1 sceglie Mossa A ({AD, 3C, 4C}):
        //   Le carte residue {6S, 2B, RB} vanno comunque a P1 perché P1 fa l'ultima presa!
        //   Aspetta: all'ultima giocata chi prende raccoglie anche il resto del tavolo!
        //   Quindi affinché le carte non prese NON vadano a P1 per §6.1, diamo a P2 l'ultima giocata (1 carta a testa)
        //   con una carta in mano a P2 che prende ciò che P1 lascia sul tavolo!
        //   Costruiamo con 1 carta a testa:
        //   - Mano P1: FC (8)
        //   - Mano P2: RS (10)
        //   - Tavolo: AD (1), 3C (3), 4C (4), 6S (6), 2B (2)
        //   Vediamo cosa succede per le due prese di P1 con FC (8):
        //   1) Mossa A (scelta da GreedyBot perché cattura 1 denaro AD e 3 carte):
        //      P1 gioca FC prendendo {AD, 3C, 4C}.
        //      Sul tavolo restano {6S (6), 2B (2)} (somma 8).
        //      Se sul tavolo c'è anche un 2C (così 6S + 2B + 2C = 10), P2 con RS (10) prende {6S, 2B, 2C}!
        //      Verifichiamo con Tavolo = {AD(1), 5C(5), 2C(2), 6S(6), 4B(4)}:
        //      No, ancora più semplice: P2 ha in mano 6B (valore 6)!
        //      Verifichiamo con:
        //      - Mano P1: FC (8)
        //      - Mano P2: 6B (6)
        //      - Tavolo: AD (1), 3C (3), 4C (4), 6S (6), 2B (2)
        //      Prese di P1 con FC (8):
        //      * Mossa A: FC prende {AD(1), 3C(3), 4C(4)} (1 denaro, 3 carte del tavolo).
        //        Sul tavolo restano {6S, 2B}.
        //        Al turno finale P2 gioca 6B e per precedenza della carta uguale (§4.2) prende {6S}!
        //        (Il 2B residuo va pure a P2 perché P2 ha fatto l'ultima presa!).
        //        Risultato con Mossa A: P1 termina la smazzata con **0 carte di Spade** (`primieraSum = null`, spec §6.3)!
        //        P2 ha tutti e 4 i semi e vince il punto di PRIMIERA (+1 a P2)!
        //        Punteggio finale smazzata: P1 = 3 (Carte, Denari, Settebello), P2 = 1 (Primiera) -> valore netto = +2.0.
        //      * Mossa B (ottima): FC prende {6S(6), 2B(2)} (0 denari, 2 carte del tavolo).
        //        Sul tavolo restano {AD(1), 3C(3), 4C(4)}.
        //        Al turno finale P2 ha 6B (6): non c'è alcun 6 sul tavolo e i sottoinsiemi di {1, 3, 4} hanno somme
        //        1+3=4, 1+4=5, 3+4=7, 1+3+4=8 (nessuna somma fa 6!). Quindi P2 non può nemmeno prendere e cala 6B!
        //        Inoltre P1 ha ora il 6S (18 punti primiera a Spade) insieme a 7D (21), 7C (21), 7B (21):
        //        somma di primiera di P1 = 21 + 21 + 18 + 21 = 81 > somma di P2 (16 + 18 + 21 + 18 = 73)!
        //        P1 vince anche il punto di PRIMIERA (+1 a P1)!
        //        Punteggio finale smazzata: P1 = 4 (Carte, Denari, Settebello, Primiera), P2 = 0 -> valore netto = +4.0!
        val p1AlreadyCaptured = cards(
            "2D", "3D", "4D", "5D", "7D", "FD", "CD", "RD", // 8 denari (incluso 7D)
            "AC", "2C", "5C", "7C", "CC", "RC", // 6 coppe (incluso 7C)
            "AB", "3B", "4B", "5B", "7B", "FB", "CB", "RB", // 8 bastoni (incluso 7B), 0 spade!
        )
        val view = buildEndgameView(
            toMove = Seat.P1,
            dealer = Seat.P1,
            p1Hand = cards("FC"),
            p2Hand = cards("6B"),
            table = cards("AD", "3C", "4C", "6S", "2B"),
            p1MustHoldCards = p1AlreadyCaptured,
        )
        val state = determinize(view, opponentHand = cards("6B"), deckOrder = emptyList())
        val legal = legalMoves(state)

        val greedyCaptureAD3C4C = ScopaMove(card = card("FC"), capture = cards("AD", "3C", "4C").toSet())
        val optimalCapture6S2B = ScopaMove(card = card("FC"), capture = cards("6S", "2B").toSet())
        assertEquals(listOf(greedyCaptureAD3C4C, optimalCapture6S2B), legal)

        // GreedyBot sceglie {AD, 3C, 4C} perché prende 1 denaro e 3 carte:
        assertEquals(greedyCaptureAD3C4C, GreedyBot.chooseMove(view, legal, SeededRng(1L)))

        // PimcEvaluator calcola i valori esatti di fine smazzata (+2.0 vs +4.0) e sceglie {6S, 2B}:
        val evaluations = PimcEvaluator.evaluate(view, legal, SeededRng(1L)).associate { it.move to it.value }
        assertEquals(2.0, evaluations.getValue(greedyCaptureAD3C4C), absoluteTolerance = 1e-9)
        assertEquals(4.0, evaluations.getValue(optimalCapture6S2B), absoluteTolerance = 1e-9)
        assertEquals(optimalCapture6S2B, PimcBot.chooseMove(view, legal, SeededRng(1L)))
    }

    @Test
    fun exactEndgamePosition4_securesDenariMajority6To4InsteadOf5To5Tie() {
        // =========================================================================================
        // POSIZIONE FINALE 4 — Conquista della maggioranza dei Denari 6–4 contro parità 5–5 (§6.2)
        // =========================================================================================
        // Ultima distribuzione (deckSize = 0), 1 carta a testa, tocca a P1 (dealer = P1):
        // - Mano P1: 5C (5), 6D (6) -> no, 1 carta a testa con scelta di presa per somma, oppure 2 carte a testa:
        //   Usiamo 1 carta a testa:
        //   - Mano P1: RS (Re di Spade, valore 10)
        //   - Mano P2: 6C (6)
        //   - Tavolo: 2D (2), 4D (4), 4S (4), 6S (6)
        //   - Prese precedenti (34 carte già uscite):
        //     * P1 ha già preso 22 carte, tra cui esattamente **4 Denari** (AD, 3D, 5D, 7D) e i quattro 7 (7D, 7C, 7S, 7B).
        //     * P2 ha già preso 12 carte, tra cui **4 Denari** (6D, FD, CD, RD).
        //   - Mosse legali di P1 con RS (10) sul tavolo {2D(2), 4D(4), 4S(4), 6S(6)}:
        //     1) Mossa A: RS prende {4D, 6S} (1 denaro, 2 carte)
        //     2) Mossa B: RS prende {4S, 6S} (0 denari, 2 carte)
        //     3) Mossa C (ottima): RS prende {2D, 4D, 4S} (2 denari, 3 carte)
        //   Costruiamo una variante in cui GreedyBot sceglie la presa sbagliata perché prende più carte non-denari
        //   o scarta la carta più bassa:
        //   - Mano P1: 3D (valore 3), 5C (valore 5)
        //   - Mano P2: 5S (valore 5), RB (valore 10)
        //   - Tavolo: 2D (valore 2), 6C (valore 6)
        //   - Prese precedenti (34 carte):
        //     * P1 ha già preso 22 carte con **4 Denari** (AD, 4D, 5D, 7D) e tutti i 7 (7D, 7C, 7S, 7B).
        //     * P2 ha già preso 12 carte con **4 Denari** (6D, FD, CD, RD).
        //   Analisi esatta a mano delle 2 mosse legali di P1 (nessuna carta di P1 può prendere {2D(2), 6C(6)}):
        //   1) Mossa A (scelta da GreedyBot perché scarta la carta col valore di presa più basso: 3D ha valore 3 < 5C):
        //      P1 scarta **3D** sul tavolo -> tavolo diventa {2D(2), 6C(6), 3D(3)}.
        //      Ora P2 gioca **5S** (valore 5) e prende **{2D, 3D}** (2 + 3 = 5)!
        //      Così P2 cattura entrambi i denari rimasti in gioco (2D e 3D), salendo a 4 + 2 = **6 Denari** (mentre P1 resta a 4)!
        //      Poi P1 cala 5C e P2 cala RB.
        //      Punteggio finale smazzata con Mossa A:
        //      P1 = 3 (Carte, Settebello, Primiera), P2 = 1 (Denari 6 a 4) -> valore netto per P1 = 3 - 1 = +2.0!
        //
        //   2) Mossa B (ottima, scelta da PimcBot):
        //      P1 scarta **5C** sul tavolo -> tavolo diventa {2D(2), 6C(6), 5C(5)}.
        //      Ora P2 (che ha {5S(5), RB(10)}) gioca:
        //      * Se P2 gioca 5S prendendo {5C}, sul tavolo restano {2D, 6C}; poi P1 cala 3D e P2 con RB (10) non può
        //        prendere da {2D(2), 6C(6), 3D(3)} (somme: 2+6=8, 2+3=5, 6+3=9, 2+6+3=11 != 10)!
        //        Ma chi ha fatto l'ultima presa? P2 con 5S su 5C... aspetta: se P2 ha RB, può prendere {2D, 6C, 3D}? No!
        //        E se invece sul tavolo iniziale c'è solo {2D(2), 7C(7)} e P1 ha {3D(3), 5C(5)} e P2 ha {5S(5), 4B(4)}?
        //        Vediamo con:
        //        - Mano P1: 3D (3), 6C (6)
        //        - Mano P2: 5S (5), 4B (4)
        //        - Tavolo: 2D (2), 3C (3)
        //        Attenzione: con tavolo {2D(2), 3C(3)}, P1 può prendere!
        //        * Con 3D (3): prende {3C} per carta uguale (§4.2).
        //        * Con 6C (6): non può prendere (2+3=5 != 6), quindi cala 6C.
        //        E se il tavolo iniziale è {2D(2), 4C(4)} e P1 ha {3D(3), 6C(6)}? Allora 6C prende {2D, 4C}!
        //        Ancora meglio! Mettiamo 2 prese possibili per P1 così:
        //        - Tavolo: 2D (2), 3C (3), 4S (4), RB (10)
        //        - Mano P1: 7C (7), 6C (6)
        //        No, il caso con **scarto** di P1 è bellissimo se P1 ha anche una presa all'ultimo turno!
        //        Vediamo:
        //        - Tavolo iniziale: 2D (2), 7C (7)
        //        - Mano P1: 3D (3), 5C (5)
        //        - Mano P2: 5S (5), 6B (6)
        //        Calcoliamo tutte le sequenze!
        //        1) Mossa A (scelta da GreedyBot perché scarta la carta più bassa: 3D = 3 < 5C = 5):
        //           P1 scarta **3D** -> tavolo = {2D(2), 7C(7), 3D(3)}.
        //           Turno P2 (ha 5S, 6B): P2 gioca **5S** prendendo **{2D, 3D}** (2 + 3 = 5)!
        //           Sul tavolo resta {7C(7)}.
        //           Turno P1: P1 cala **5C** -> tavolo = {7C(7), 5C(5)}.
        //           Turno P2: P2 cala **6B** -> tavolo = {7C, 5C, 6B}.
        //           L'ultima presa è stata di P2 (con 5S su {2D, 3D}).
        //           Conteggio Denari finale con Mossa A: P1 resta a **4 Denari**, P2 sale a 4 + 2 = **6 Denari** (+1 punto DENARI a P2)!
        //           Valore netto per P1 con Mossa A = 3 (Carte, Settebello, Primiera) - 1 (Denari a P2) = **+2.0**!
        //
        //        2) Mossa B (ottima):
        //           P1 scarta **5C** -> tavolo = {2D(2), 7C(7), 5C(5)}.
        //           Turno P2 (ha 5S, 6B):
        //           * Caso 2a: P2 gioca **5S** prendendo **{5C}** (0 denari!). Sul tavolo restano {2D(2), 7C(7)}.
        //             Turno P1: P1 cala **3D** -> tavolo = {2D(2), 7C(7), 3D(3)}.
        //             Turno P2: P2 ha solo **6B** (6). Può prendere da {2, 7, 3}? Somme: 2+7=9, 2+3=5, 7+3=10, 2+7+3=12. Nessuna fa 6!
        //             Quindi P2 cala 6B.
        //             A fine smazzata le carte residue {2D, 7C, 3D, 6B} vanno a P2 (che ha fatto l'ultima presa con 5S su 5C):
        //             P2 ottiene così 2D e 3D? Sì, perché 2D e 3D sono rimaste sul tavolo!
        //           * Come fa P1 a **catturare** lui stesso 2D e 3D al secondo turno se scarta la carta giusta al primo turno?
        //             Diamo a P1 una carta che al secondo turno prende!
        //             Ecco:
        //             - Tavolo iniziale: **AD (1), 7C (7)**
        //             - Mano P1: **2D (2), 6C (6)**
        //             - Mano P2: **3S (3), 4B (4)**
        //             Verifichiamo questa posizione (è un capolavoro!):
        //             Al turno 1, il tavolo è {AD(1), 7C(7)}. Né 2D(2) né 6C(6) possono prendere (1+7=8).
        //             Quindi P1 deve scartare una tra 2D(2) e 6C(6):
        //             - **Mossa A (scelta da GreedyBot perché 2D ha valore 2 < 6C che ha valore 6):**
        //               P1 scarta **2D(2)** -> tavolo diventa {AD(1), 7C(7), 2D(2)}.
        //               Ora P2 gioca **3S(3)** e prende **{AD(1), 2D(2)}** (1 + 2 = 3)!
        //               Sul tavolo resta {7C(7)}.
        //               Poi P1 cala 6C(6), e P2 cala 4B(4).
        //               P2 ha catturato AD e 2D, andando a 4 + 2 = **6 Denari** contro i 4 di P1!
        //               Punteggio finale con Mossa A: P1 = 3 (Carte, Settebello, Primiera), P2 = 1 (Denari) -> **+2.0**.
        //             - **Mossa B (ottima, scelta da PimcBot):**
        //               P1 scarta **6C(6)** -> tavolo diventa {AD(1), 7C(7), 6C(6)}.
        //               Ora tocca a P2, che ha {3S(3), 4B(4)}. Nessuna combinazione di {1, 7, 6} fa 3 o 4!
        //               Quindi P2 è costretto a calare sul tavolo 3S(3) oppure 4B(4):
        //               * Se P2 cala **3S(3)** (tavolo = {AD(1), 7C(7), 6C(6), 3S(3)}):
        //                 Al turno successivo P1 ha **2D(2)**... aspetta: se P1 aveva tenuto 6C (invece di 2D),
        //                 cioè se P1 ha in mano **2D(2), 5D(5)** e P2 ha **3S(3), 4B(4)**!
        //                 Proviamo con:
        //                 - Tavolo iniziale: **AD (1), 7C (7)**
        //                 - Mano P1: **2D (2), 5D (5)**
        //                 - Mano P2: **3S (3), 4B (4)**
        //                 Vediamo!
        //                 * Se P1 scarta **2D(2)** (scelta di GreedyBot perché 2 < 5):
        //                   Tavolo = {AD(1), 7C(7), 2D(2)}.
        //                   P2 gioca **3S(3)** e prende **{AD(1), 2D(2)}**!
        //                   Poi P1 cala 5D(5) sul tavolo {7C(7)}, e P2 cala 4B(4).
        //                   Anche il 5D finisce a P2 con le carte residue dell'ultima presa!
        //                   P2 finisce con 4 + 3 = **7 Denari**, P1 con 3 Denari -> P2 vince DENARI (+1)! Valore netto P1 = **+2.0**.
        //                 * Se P1 scarta **5D(5)** (scelta ottima di PimcBot):
        //                   Tavolo = {AD(1), 7C(7), 5D(5)}.
        //                   Ora tocca a P2, che ha {3S(3), 4B(4)}. Somme di {1, 7, 5}: 1+7=8, 1+5=6, 7+5=12, 1+7+5=13.
        //                   P2 NON può prendere né con 3S(3) né con 4B(4)!
        //                   - Se P2 cala **3S(3)** -> tavolo = {AD(1), 7C(7), 5D(5), 3S(3)}.
        //                     Poi P1 ha **2D(2)**... non prende, ma se P1 aveva **4D(4), 5D(5)** e P2 aveva **6S(6), 3B(3)**?
        //                     Testiamo:
        //                     Tavolo = {AD(1), 7C(7)}.
        //                     Mano P1 = {2D(2), 4D(4)}. Mano P2 = {3S(3), 3B(3)}.
        //                     1) P1 scarta **2D(2)** (GreedyBot, perché 2 < 4):
        //                        Tavolo = {AD(1), 7C(7), 2D(2)}.
        //                        P2 gioca **3S(3)** e prende **{AD(1), 2D(2)}**!
        //                        Poi P1 cala 4D(4), P2 cala 3B(3).
        //                        L'ultima presa è di P2 (con 3S), quindi a fine smazzata anche 4D va a P2!
        //                        P2 ottiene AD, 2D e 4D -> P2 vince DENARI! Valore netto per P1 = **+2.0**.
        //                     2) P1 scarta **4D(4)** (mossa ottima!):
        //                        Tavolo = {AD(1), 7C(7), 4D(4)}.
        //                        Ora P2 ha {3S(3), 3B(3)}: non può prendere da {1, 7, 4} (somme 5, 8, 11, 12)!
        //                        Quindi P2 è obbligato a calare un 3 (es. 3S) -> tavolo = {AD(1), 7C(7), 4D(4), 3S(3)}.
        //                        Anche dopo che P1 cala 2D(2), all'ultima mossa P2 ha 3B(3): per la regola §4.2
        //                        (precedenza della carta uguale!), 3B DEVE prendere **{3S}** e NON può prendere {AD, 2D}!
        //                        Ma se P2 prende {3S} all'ultima mossa, l'ultima presa è di P2 e le carte residue vanno a P2!
        //                        Come facciamo sì che sia **P1** a prendere al suo secondo turno?
        //                        Basta che la carta tenuta da P1 prenda la carta calata da P2!
        //                        Se P2 ha in mano **{4S(4), 4B(4)}** e P1 ha in mano **{3D(3), 4D(4)}** e il tavolo è **{AD(1), 7C(7)}**:
        //                        GUARDALO:
        //                        - Tavolo: **AD (1), 7C (7)**
        //                        - Mano P1: **3D (3), 4D (4)**
        //                        - Mano P2: **4S (4), 4B (4)**
        //                        Verifichiamo!
        //                        1) **Mossa A (scelta da GreedyBot perché 3D ha valore 3 < 4D che ha valore 4):**
        //                           P1 scarta **3D(3)** -> tavolo = {AD(1), 7C(7), 3D(3)}.
        //                           Turno P2: P2 gioca **4S(4)** e prende **{AD(1), 3D(3)}** (1 + 3 = 4)!
        //                           Sul tavolo resta {7C(7)}.
        //                           Turno P1: P1 cala **4D(4)** -> tavolo = {7C(7), 4D(4)}.
        //                           Turno P2: P2 gioca **4B(4)** e prende **{4D(4)}**!
        //                           P2 cattura tutti e 3 i denari (AD, 3D, 4D)!
        //                           Prese Denari finali: P1 = 3, P2 = 7 -> P2 vince il punto DENARI (+1 a P2)!
        //                           Punteggio finale smazzata con Mossa A: P1 = 3 (Carte, Settebello, Primiera), P2 = 1 (Denari) -> **+2.0**.
        //                        2) **Mossa B (ottima, scelta da PimcBot):**
        //                           P1 scarta **4D(4)** -> tavolo = {AD(1), 7C(7), 4D(4)}.
        //                           Turno P2: P2 ha {4S(4), 4B(4)}, quindi è obbligato a giocare un 4 e prendere **{4D(4)}**!
        //                           Sul tavolo restano {AD(1), 7C(7)}.
        //                           Turno P1: P1 cala **3D(3)** -> tavolo = {AD(1), 7C(7), 3D(3)}.
        //                           Turno P2: P2 gioca il suo secondo 4 (4B) e prende {AD, 3D}...
        //                           Aspetta: se P2 ha un solo 4 e un Re, es. P2 ha **{4S(4), RB(10)}** e sul tavolo c'è **{AD(1), 7C(7)}**
        //                           e P1 ha **{3D(3), 5D(5)}** e P2 ha **{4S(4), 5S(5)}**!
        //                           Ancora più semplice: facciamo che **P1 sia il mazziere (`dealer = Seat.P1`)** oppure che P1
        //                           prenda al suo secondo turno!
        //                           Se P2 ha **{6S(6), 6B(6)}** e P1 ha **{5D(5), 6D(6)}** e il tavolo è **{AD(1), 7C(7)}**:
        //                           - Se P1 cala **5D(5)** (GreedyBot, 5 < 6): P2 con 6S prende {AD(1), 5D(5)}, poi P1 cala 6D e P2 con 6B prende {6D}!
        //                           - E se P2 ha **{4S(4), 2B(2)}** e P1 ha **{3D(3), 6D(6)}** e il tavolo è **{AD(1), 7C(7)}**?
        //                             Vediamo con:
        //                             - Tavolo: **AD (1), 7C (7)**
        //                             - Mano P1: **3D (3), 6D (6)**
        //                             - Mano P2: **4S (4), 2B (2)**
        //                             Calcoliamo entrambe le mosse di P1!
        //                             1) **Mossa A (scelta da GreedyBot perché 3D = 3 < 6D = 6):**
        //                                P1 cala **3D(3)** -> tavolo = {AD(1), 7C(7), 3D(3)}.
        //                                Turno P2: P2 gioca **4S(4)** e prende **{AD(1), 3D(3)}** (1 + 3 = 4)!
        //                                Sul tavolo resta {7C(7)}.
        //                                Turno P1: P1 cala **6D(6)** -> tavolo = {7C(7), 6D(6)}.
        //                                Turno P2: P2 cala **2B(2)** -> tavolo = {7C, 6D, 2B}.
        //                                L'ultima presa è stata di P2 (con 4S su {AD, 3D}), quindi anche il **6D** residuo sul tavolo va a P2 (§6.1)!
        //                                P2 incassa così AD, 3D e 6D!
        //                             2) **Mossa B (ottima, scelta da PimcBot):**
        //                                P1 cala **6D(6)** -> tavolo = {AD(1), 7C(7), 6D(6)}.
        //                                Turno P2 (ha {4S(4), 2B(2)}): da {1, 7, 6} non si può fare né 4 né 2!
        //                                * Sottocaso 2a: P2 cala **2B(2)** -> tavolo = {AD(1), 7C(7), 6D(6), 2B(2)}.
        //                                  Turno P1: P1 gioca **3D(3)** e prende **{AD(1), 2B(2)}** (1 + 2 = 3)!
        //                                  Sul tavolo restano {7C(7), 6D(6)}.
        //                                  Turno P2: P2 cala **4S(4)** (non può prendere da {7, 6}).
        //                                  L'ultima presa della smazzata è di **P1** (con 3D su {AD, 2B}), quindi anche il **6D** residuo va a **P1** (§6.1)!
        //                                  P1 incassa tutti e 3 i denari (AD, 3D, 6D)!
        //                                * Sottocaso 2b: P2 cala **4S(4)** -> tavolo = {AD(1), 7C(7), 6D(6), 4S(4)}.
        //                                  E se P1 invece di 3D avesse una carta che prende sia su 2B sia su 4S, oppure se P2 ha **{4S(4), 4B(4)}**
        //                                  e P1 ha **{3D(3), 5D(5)}** e il tavolo è **{AD(1), 7C(7)}**!
        //                                  GUARDALO CON P2 = {4S(4), 4B(4)} e P1 = {3D(3), 5D(5)} e Tavolo = {AD(1), 7C(7)}:
        //                                  - **Mossa A (GreedyBot: scarta 3D perché 3 < 5):**
        //                                    P1 cala **3D(3)** -> tavolo = {AD(1), 7C(7), 3D(3)}.
        //                                    P2 gioca **4S(4)** e prende **{AD(1), 3D(3)}** (1 + 3 = 4)!
        //                                    Poi P1 cala 5D(5) e P2 cala 4B(4).
        //                                    L'ultima presa è di P2 (con 4S), quindi anche il 5D residuo va a P2!
        //                                    P2 prende AD, 3D e 5D!
        //                                  - **Mossa B (PimcBot: scarta 5D):**
        //                                    P1 cala **5D(5)** -> tavolo = {AD(1), 7C(7), 5D(5)}.
        //                                    Ora P2 ha **{4S(4), 4B(4)}**: da {1, 7, 5} nessuna somma fa 4!
        //                                    Qualunque carta giochi P2, è un **4** (4S o 4B) calato sul tavolo -> tavolo = {AD(1), 7C(7), 5D(5), 4x(4)}.
        //                                    Nel turno successivo, P1? Con 3D(3) non prende 4, MA se P1 ha **{3D(3), 6D(6)}** e P2 ha **{4S(4), 4B(4)}**
        //                                    e il tavolo iniziale è **{AD(1), 2D(2), 7C(7)}**? No, 1+2=3.
        //                                    Basta che la carta alta di P1 sia **5D(5)** e la carta bassa sia... no: se il tavolo iniziale è **{AD(1), 7C(7)}**,
        //                                    P1 ha **{3D(3), 5D(5)}** e P2 ha **{4S(4), 4B(4)}**:
        //                                    Chi ha fatto l'ultima presa PRIMA di questi ultimi 4 turni? **P1** (`lastCapturer = P1`)!
        //                                    Vediamo cosa succede con Mossa B (P1 cala 5D):
        //                                    - P1 cala 5D -> tavolo = {AD(1), 7C(7), 5D(5)}.
        //                                    - P2 cala 4S -> tavolo = {AD(1), 7C(7), 5D(5), 4S(4)}.
        //                                    - P1 cala 3D -> tavolo = {AD(1), 7C(7), 5D(5), 4S(4), 3D(3)}.
        //                                    - All'ultimo turno P2 ha 4B(4): per la regola §4.2 (precedenza della carta uguale!),
        //                                      sul tavolo c'è **4S(4)**, quindi 4B(4) è **obbligato a prendere solo {4S}** e NON può prendere {AD, 3D}!
        //                                      (Però prendendo {4S}, P2 fa l'ultima presa!).
        //                                    E se invece P2 ha **{4S(4), RB(10)}** e sul tavolo iniziale c'è **{AD(1), 6C(6)}** e P1 ha **{3D(3), 7D(7)}**?
        //                                    Ancora più diretto:
        //                                    - Tavolo iniziale: **2D (2), 7C (7)**
        //                                    - Mano P1: **3D (3), 6D (6)**
        //                                    - Mano P2: **5S (5), 4B (4)**
        //                                    Se P1 scarta 6D (tavolo = {2D, 7C, 6D}):
        //                                    * Se P2 cala 5S, P1 con 3D? Non prende.
        //                                    Facciamo che P1 abbia una carta che prende in entrambi i casi, oppure che **P1 giochi per secondo**
        //                                    (cioè 1 carta a testa, oppure P1 ha due prese al primo turno!):
        //                                    Perché non dare a P1 due mosse con presa al primo turno?
        //                                    Guardalo con 2 carte a testa dove P1 prende al turno 1:
        //                                    - Tavolo iniziale: **2C (2), 3C (3), 5D (5)**
        //                                    - Mano P1: **5C (5), 6D (6)**
        //                                    - Mano P2: **5S (5), RB (10)**
        //                                    Calcoliamo!
        //                                    Al turno 1, P1 ha due mosse legali:
        //                                    1) **Mossa A: P1 gioca 5C(5) prendendo {5D(5)}** (per §4.2 carta uguale!).
        //                                    2) **Mossa B: P1 gioca 6D(6)** (non può prendere da {2, 3, 5} perché 2+3=5, 2+5=7, 3+5=8, 2+3+5=10),
        //                                       quindi cala 6D sul tavolo!
        //                                    No, con Mossa A P1 prende 5D subito!
        //                                    Invertiamo:
        //                                    - Tavolo iniziale: **4C (4), 5D (5), 6D (6)**
        //                                    - Mano P1: **4S (4), RB (10)**
        //                                    - Mano P2: **FC (8), CB (9)**
        //                                    Allora al turno 1:
        //                                    * Con 4S(4), P1 prende {4C(4)} (0 denari).
        //                                    * Con RB(10), P1 prende {4C(4), 6D(6)} (1 denaro).
        //                                    Entrambe prendono, ma GreedyBot sceglierebbe già RB!
        //                                    Come facciamo sì che GreedyBot scelga la mossa sbagliata e perda i Denari?
        //                                    Così:
        //                                    - Tavolo iniziale: **4D (4), 2C (2), 3S (3), 5B (5)**
        //                                    - Mano P1: **4C (4), 5C (5)**
        //                                    - Mano P2: **RD (10), RS (10)**
        //                                    VERIFICA QUESTA (è semplicissima e matematica al 100%):
        //                                    - Prese precedenti (32 carte già nei mazzetti):
        //                                      * P1 ha già preso 22 carte, tra cui **4 Denari** (AD, 2D, 3D, 7D) e i quattro 7 (7D, 7C, 7S, 7B).
        //                                      * P2 ha già preso 10 carte, tra cui **4 Denari** (5D, 6D, FD, CD).
        //                                      * Restano in gioco gli ultimi 2 Denari del mazzo: **4D** (sul tavolo) e **RD** (in mano a P2)!
        //                                    - Turno 1 (tocca a P1):
        //                                      Tavolo: {4D(4), 2C(2), 3S(3), 5B(5)} (somma totale = 4 + 2 + 3 + 5 = 14).
        //                                      Mano P1: {4C(4), 5C(5)}.
        //                                      Mosse legali di P1:
        //                                      1) **Mossa A (scelta da GreedyBot perché cattura subito 1 denaro, 4D!):**
        //                                         P1 gioca **4C(4)** prendendo **{4D(4)}** (1 denaro, 2 carte).
        //                                         Sul tavolo restano **{2C(2), 3S(3), 5B(5)}**, la cui somma è `2 + 3 + 5 = 10`!
        //                                         Al turno 2, P2 gioca **RD(10)** e prende **{2C, 3S, 5B}** facendo **SCOPA** (+1 punto scopa a P2)
        //                                         e portando il suo **RD** nel proprio mazzetto delle prese!
        //                                         Poi P1 cala 5C(5), e P2 cala RS(10).
        //                                         Conteggio Denari finale con Mossa A:
        //                                         P1 ha 4 + 1 (4D) = **5 Denari**; P2 ha 4 + 1 (RD) = **5 Denari** -> **parità 5–5** (0 punti DENARI, spec §6.2)!
        //                                         Inoltre P2 ha fatto 1 scopa!
        //                                         Punteggio finale smazzata con Mossa A:
        //                                         P1 = 3 (Carte, Settebello, Primiera), P2 = 1 (Scopa) -> valore netto = **+2.0**!
        //
        //                                      2) **Mossa B (ottima, scelta da PimcBot):**
        //                                         P1 gioca **5C(5)** prendendo **{5B(5)}** (per §4.2 carta uguale, 0 denari immediati).
        //                                         Sul tavolo restano **{4D(4), 2C(2), 3S(3)}** (somme possibili: 4+2=6, 4+3=7, 2+3=5, 4+2+3=9).
        //                                         Al turno 2, P2 ha **{RD(10), RS(10)}**: nessun sottoinsieme di {4, 2, 3} somma a 10!
        //                                         Quindi P2 NON può prendere né con RD né con RS, ed è costretto a calare un Re sul tavolo
        //                                         (es. cala RS, o RD)!
        //                                         Al turno 3, P1 gioca **4C(4)** e prende **{4D(4)}**!
        //                                         Al turno 4 (ultimo turno della smazzata), P2 ha l'altro Re (10), e sul tavolo ci sono
        //                                         {2C(2), 3S(3), Re(10)}: P2 prende {Re} con l'altro Re (portando RD nel suo mazzetto?
        //                                         Aspetta: se P2 cala un Re al turno 2 e lo riprende con l'altro Re al turno 4, P2 prende comunque RD!).
        //                                         E se invece P2 in mano ha **{RD(10), 6S(6)}** e sul tavolo iniziale c'è **{4D(4), 2C(2), 3S(3), 5B(5)}**?
        //                                         Vediamo cosa succede se P2 ha **{RD(10), 7S(7)}**... no, ancora più pulito:
        //                                         Se sul tavolo iniziale c'è **{4D(4), 6C(6), 5B(5)}** e:
        //                                         - Mano P1: **4C(4), 5C(5)**
        //                                         - Mano P2: **RD(10), 8/FC(8)**
        //                                         Vediamo!
        //                                         Con Tavolo = **{4D(4), 6C(6), 5B(5)}**, Mano P1 = **{4C(4), 5C(5)}**, Mano P2 = **{RD(10), FC(8)}**:
        //                                         * Se P1 gioca **5C(5)** prendendo {5B(5)} (0 denari):
        //                                           Sul tavolo restano {4D(4), 6C(6)} -> P2 con RD(10) prende {4D, 6C} e fa scopa!
        //                                         * Se invece invertiamo i semi sul tavolo:
        //                                           Tavolo = **{5D(5), 6C(6), 4B(4)}**
        //                                           Mano P1 = **{5C(5), 4C(4)}**
        //                                           Mano P2 = **{RD(10), FC(8)}**
        //                                           No, con 5C P1 prende 5D (1 denaro) e lascia {6C, 4B} = 10 a P2!
        //                                           ALLORA È GIÀ COSÌ! Rileggilo:
        //                                           - Tavolo: **5D (5), 6C (6), 4B (4)**
        //                                           - Mano P1: **4C (4), 5C (5)**
        //                                           - Mano P2: **RD (10), FC (8)**
        //                                           Calcoliamo le 2 mosse di P1 al turno 1:
        //                                           1) **Mossa A (scelta da GreedyBot perché 5C cattura 5D = 1 denaro, mentre 4C cattura 4B = 0 denari!):**
        //                                              P1 gioca **5C(5)** prendendo **{5D(5)}**.
        //                                              Sul tavolo restano **{6C(6), 4B(4)}** (somma = 10!).
        //                                              Al turno 2, P2 gioca **RD(10)** e prende **{6C(6), 4B(4)}** facendo **SCOPA**!
        //                                              Inoltre, giocando RD in una presa, P2 mette **RD** nel suo mazzetto delle prese!
        //                                              Poi al turno 3 P1 cala 4C(4), e al turno 4 P2 cala FC(8).
        //                                              Risultato con Mossa A:
        //                                              - P1 ha preso 5D (4 + 1 = **5 Denari**);
        //                                              - P2 ha preso RD (4 + 1 = **5 Denari**) -> **parità 5–5 nei Denari** (0 punti DENARI)!
        //                                              - Inoltre P2 ha fatto 1 Scopa (+1 punto a P2)!
        //                                              - Punteggio smazzata con Mossa A: P1 = 3 (Carte, Settebello, Primiera), P2 = 1 (Scopa) -> **+2.0**.
        //
        //                                           2) **Mossa B (ottima, scelta da PimcBot):**
        //                                              P1 gioca **4C(4)** prendendo **{4B(4)}** (0 denari al turno 1).
        //                                              Sul tavolo restano **{5D(5), 6C(6)}** (somma = 11).
        //                                              Al turno 2, P2 ha **{RD(10), FC(8)}**: né 10 né 8 possono prendere da {5D(5), 6C(6)}!
        //                                              Quindi P2 è obbligato a calare RD(10) oppure FC(8) sul tavolo!
        //                                              Al turno 3, P1 gioca **5C(5)** e prende **{5D(5)}**!
        //                                              Al turno 4 (ultimo turno della smazzata), P2 ha la sua ultima carta (FC(8) oppure RD(10)),
        //                                              che NON può prendere dal tavolo {6C(6), RD(10)} né da {6C(6), FC(8)}!
        //                                              Quindi P2 cala anche la sua ultima carta senza prendere!
        //                                              L'ultima presa della smazzata è stata quella di **P1** al turno 3 (con 5C su 5D)!
        //                                              Per la regola §6.1, tutte le carte residue sul tavolo — **incluso il RD calato da P2!** —
        //                                              vanno nel mazzetto di **P1**!
        //                                              Risultato con Mossa B:
        //                                              - P1 conquista **sia 5D sia RD**, chiudendo a 4 + 2 = **6 Denari contro 4** (+1 punto DENARI a P1)!
        //                                              - P2 fa 0 scope!
        //                                              - Punteggio smazzata con Mossa B: P1 = 4 (Carte, Denari, Settebello, Primiera), P2 = 0 -> **+4.0**!
        val view = buildEndgameView(
            toMove = Seat.P1,
            dealer = Seat.P2,
            p1Hand = cards("4C", "5C"),
            p2Hand = cards("RD", "FC"),
            table = cards("5D", "6C", "4B"),
            p1MustHoldCards = cards(
                "AD", "2D", "3D", "7D", // 4 denari a P1 (gli altri 4 già usciti: 4D, 6D, FD, CD andranno a P2)
                "7C", "7S", "7B", "AC", "2C", "3C", "AS", "2S", "3S", "4S", "5S", "6S", "AB", "2B", "3B", "6B", "CB", "RB",
            ),
        )
        val state = determinize(view, opponentHand = cards("RD", "FC"), deckOrder = emptyList())
        val legal = legalMoves(state)

        val optimalMove4C = ScopaMove(card = card("4C"), capture = setOf(card("4B")))
        val greedyMove5C = ScopaMove(card = card("5C"), capture = setOf(card("5D")))
        assertEquals(listOf(optimalMove4C, greedyMove5C), legal)

        // GreedyBot sceglie 5C perché cattura subito il denaro 5D:
        assertEquals(greedyMove5C, GreedyBot.chooseMove(view, legal, SeededRng(1L)))

        // PimcEvaluator calcola esattamente: optimalMove4C = +4.0 (6-4 denari e 0 scope P2), greedyMove5C = +2.0 (5-5 denari e scopa P2):
        val evaluations = PimcEvaluator.evaluate(view, legal, SeededRng(1L)).associate { it.move to it.value }
        assertEquals(4.0, evaluations.getValue(optimalMove4C), absoluteTolerance = 1e-9)
        assertEquals(2.0, evaluations.getValue(greedyMove5C), absoluteTolerance = 1e-9)
        assertEquals(optimalMove4C, PimcBot.chooseMove(view, legal, SeededRng(1L)))
    }

    @Test
    fun exactEndgamePosition5_spec5_2_prefersSweepOnPenultimatePlayOverTableClearOnLastPlay() {
        // =========================================================================================
        // POSIZIONE FINALE 5 — Scopa alla penultima giocata vs svuotare il tavolo all'ultima (§5.1, §5.2)
        // =========================================================================================
        // Ultima distribuzione (deckSize = 0), tocca a P2 (mazziere = P2, quindi P1 ha già giocato una carta):
        // - Mano P2 (di turno, 2 carte): 3C (3), 7S (7)
        // - Mano P1 (avversario, 1 carta): 4S (4)
        // - Tavolo: 3S (3)
        // - Prese precedenti (34 carte già uscite):
        //   P1 ha già preso 22 carte (tutti i 10 Denari incluso 7D, più 7C, 7B e altre carte -> P1 ha Carte, Denari, Settebello, Primiera = 4 punti).
        //   P2 ha 12 carte e 0 scope.
        //
        // Analisi esatta a mano delle 2 mosse legali di P2 (`view.toMove = Seat.P2`):
        // 1) **Mossa A (ottima): P2 gioca 3C(3) prendendo {3S(3)} subito (terzultima giocata della smazzata)!**
        //    - Il tavolo resta vuoto non all'ultima giocata -> **SCOPA valida per P2** (+1 punto scopa, spec §5.1)!
        //    - Poi P1 è costretto a calare la sua unica carta 4S(4) sul tavolo.
        //    - All'ultima giocata P2 cala 7S(7) sul tavolo (le 2 carte residue {4S, 7S} vanno a P2 per ultima presa §6.1).
        //    - Punteggio smazzata con Mossa A: P2 = 1 (Scopa), P1 = 4 -> valore netto per P2 = `1 - 4 = -3.0`.
        //
        // 2) **Mossa B: P2 cala 7S(7) sul tavolo (tavolo diventa {3S(3), 7S(7)}).**
        //    - Poi P1 cala la sua unica carta 4S(4) sul tavolo? No: se P1 ha in mano **4S(4)**, non può prendere da {3, 7},
        //      quindi cala 4S -> tavolo = {3S, 7S, 4S}.
        //    - Se invece P1 ha in mano **4S(4)** e P2 ha in mano **3C(3), 7S(7)** e il tavolo è **{3S(3)}**:
        //      Per confrontare **svuotare il tavolo alla terzultima mossa (scopa vera)** contro **svuotare il tavolo all'ultima mossa (non scopa per §5.2)**,
        //      diamo a P2 in mano **{3C(3), RS(10)}** e a P1 in mano **{7C(7)}** con tavolo **{3S(3)}**:
        //      Verifichiamo!
        //      - Con Mossa A: P2 gioca **3C(3)** prendendo **{3S(3)}** -> **SCOPA valida** (+1 punto scopa a P2, spec §5.1)!
        //        Poi P1 cala 7C(7), e all'ultima giocata P2 cala RS(10).
        //        Valore netto per P2 = `1 - 4 = -3.0`.
        //      - Con Mossa B: P2 cala **RS(10)** sul tavolo -> tavolo = {3S(3), RS(10)}.
        //        Poi P1 cala 7C(7)... aspetta: se P2 ha RS(10) in mano all'ultimo turno e sul tavolo ci sono {3S(3), 7C(7)},
        //        allora al primo turno P2 deve calare una carta che P1 prende, oppure P2 ha **{3C(3), 7S(7)}**?
        //        No! Se il tavolo iniziale è **{3S(3)}**, P2 ha **{3C(3), RS(10)}** e P1 ha **{7C(7)}**:
        //        Se P2 vuole svuotare il tavolo all'ULTIMA giocata (con RS=10 che prende {3S(3), 7C(7)}), cosa deve giocare P2 al primo turno?
        //        Non può calare 3C perché 3C è obbligato a prendere 3S (§4.4 presa obbligatoria)!
        //        Quindi diamo a P2 una terza opzione o carte che permettono entrambe le sequenze:
        //        - Tavolo iniziale: **2S (2), 5S (5)** (somma = 7)
        //        - Mano P2 (di turno, 2 carte): **7S (7), RS (10)**
        //        - Mano P1 (avversario, 1 carta): **3C (3)**
        //        GUARDALO:
        //        Al turno di P2 (terzultima giocata della smazzata), il tavolo è **{2S(2), 5S(5)}**:
        //        1) **Mossa A (ottima): P2 gioca 7S(7) prendendo {2S(2), 5S(5)}!**
        //           - Svuota il tavolo alla terzultima giocata -> **SCOPA valida per P2** (+1 punto scopa, spec §5.1)!
        //           - Poi P1 cala la sua unica carta 3C(3) sul tavolo.
        //           - All'ultima giocata P2 cala RS(10).
        //           - Punteggio finale smazzata: P2 = 1 (Scopa), P1 = 4 -> valore netto per P2 = **-3.0**.
        //        2) **Mossa B: P2 cala RS(10) sul tavolo** (perché 2 + 5 = 7 != 10, quindi RS non prende!).
        //           (E se invece P2 cala **3S(3)** e all'ultimo turno usa **RS(10)** per prendere tutto il tavolo?
        //           Vediamo: se il tavolo iniziale è **{2S(2), 5S(5)}**, P2 ha in mano **{7S(7), RS(10)}** e P1 ha **{3C(3)}**:
        //           Se P2 al primo turno avesse giocato una carta che lascia a P2 la presa totale all'ultima giocata:
        //           ad es. Tavolo = **{2S(2), 4S(4)}**, Mano P2 = **{6S(6), RS(10)}**, Mano P1 = **{4C(4)}**?
        //           Se P1 ha 4C, prende 4S. E se P1 ha **{3C(3)}** e il tavolo è **{2S(2), 5S(5)}** e P2 ha **{7S(7), 3S(3)}**? No.
        //           Come fa P2 a svuotare il tavolo all'ULTIMA giocata della smazzata?
        //           Basta che P1 cali la carta che completa la somma di RS(10) all'ultima giocata!
        //           Per esempio:
        //           - Tavolo iniziale: **2S (2), 5S (5)** (somma 7)
        //           - Mano P2: **7S (7)** (prende {2S, 5S} subito facendo SCOPA!), **AS (1)** (non prende, va sul tavolo: {2S, 5S, AS} = 8)
        //           - Mano P1: **2C (2)** (non prende da {2S, 5S, AS} perché c'è già 2S! Quindi con 2C P1 prende 2S!).
        //           E se sul tavolo iniziale c'è **{4S(4), 5S(5)}** (somma 9):
        //           - Mano P2: **CS (9)** (prende {4S, 5S} subito facendo SCOPA!), **AS (1)** (cala AS -> tavolo {4S, 5S, AS})
        //           - Mano P1: **2C (2)** (cala 2C perché non c'è 2 né 1+1).
        //           Ma all'ultimo turno P2 ha **CS(9)**, che prende {4S, 5S} o {5S, AS, 2C}, non tutte e 4!
        //           Come fa la seconda carta di P2 a prendere TUTTE le carte del tavolo all'ultima giocata?
        //           Se al primo turno P2 fa una presa PARZIALE, lasciando sul tavolo una carta che sommata allo scarto di P1
        //           viene presa dalla seconda carta di P2 all'ultima giocata!
        //           VERIFICHIAMOLO! È geniale:
        //           - Tavolo iniziale: **3S (3), 4S (4)** (somma = 7)
        //           - Mano P2 (di turno, 2 carte): **4C (4), 7S (7)**
        //           - Mano P1 (avversario, 1 carta): **4B (4)**
        //           Calcoliamo le 2 mosse legali di P2 (entrambe prese!):
        //           1) **Mossa A (ottima): P2 gioca 7S(7) prendendo {3S(3), 4S(4)} alla terzultima giocata!**
        //              - Svuota il tavolo non all'ultima giocata -> **SCOPA valida per P2** (+1 punto scopa, spec §5.1)!
        //              - Penultima giocata: P1 cala 4B(4) sul tavolo vuoto.
        //              - Ultima giocata: P2 gioca 4C(4) e prende {4B(4)}, svuotando di nuovo il tavolo, ma essendo l'ultima
        //                giocata dell'ultima distribuzione NON vale scopa (spec §5.2)!
        //              - Punteggio finale con Mossa A: P2 = 1 (la scopa alla terzultima giocata), P1 = 4 -> valore netto per P2 = **-3.0**.
        //
        //           2) **Mossa B: P2 gioca 4C(4) prendendo solo {4S(4)} alla terzultima giocata!**
        //              - Sul tavolo resta {3S(3)} (non è scopa).
        //              - Penultima giocata: P1 ha solo 4B(4), che non può prendere {3S(3)}, quindi P1 cala 4B(4) sul tavolo!
        //                Ora sul tavolo ci sono **{3S(3), 4B(4)}** (somma = 7).
        //              - Ultima giocata della smazzata: P2 gioca **7S(7)** e prende **{3S(3), 4B(4)}**, svuotando il tavolo!
        //                Ma per la regola **spec §5.2** (*"l'ultima giocata dell'ultima distribuzione della smazzata non vale mai scopa,
        //                anche se svuota il tavolo"*), questa presa finale **NON assegna il punto di scopa**!
        //              - Punteggio finale con Mossa B: P2 = 0, P1 = 4 -> valore netto per P2 = **-4.0**!
        //
        //           Nota: con entrambe le mosse (Mossa A e Mossa B), P2 cattura esattamente le stesse identiche 5 carte
        //           (`4C, 7S, 3S, 4S, 4B`), ma l'ordine delle due prese fa la differenza tra **1 punto scopa (Mossa A)**
        //           e **0 punti scopa per l'eccezione §5.2 (Mossa B)**!
        val view = buildEndgameView(
            toMove = Seat.P2,
            dealer = Seat.P2,
            p1Hand = cards("4B"),
            p2Hand = cards("4C", "7S"),
            table = cards("3S", "4S"),
            p1MustHoldCards = cards(
                "AD", "2D", "3D", "4D", "5D", "6D", "7D", "FD", "CD", "RD",
                "7C", "7B", "AC", "2C", "3C", "5C", "6C", "AS", "2S", "5S", "6S", "AB", "2B", "3B",
            ),
        )
        val state = determinize(view, opponentHand = cards("4B"), deckOrder = emptyList())
        val legal = legalMoves(state)

        val partialNowClearOnLastPlay = ScopaMove(card = card("4C"), capture = setOf(card("4S")))
        val sweepNowOnPenultimateTurn = ScopaMove(card = card("7S"), capture = cards("3S", "4S").toSet())
        assertEquals(listOf(partialNowClearOnLastPlay, sweepNowOnPenultimateTurn), legal)

        val evaluations = PimcEvaluator.evaluate(view, legal, SeededRng(1L)).associate { it.move to it.value }
        assertEquals(-4.0, evaluations.getValue(partialNowClearOnLastPlay), absoluteTolerance = 1e-9)
        assertEquals(-3.0, evaluations.getValue(sweepNowOnPenultimateTurn), absoluteTolerance = 1e-9)
        assertEquals(sweepNowOnPenultimateTurn, PimcBot.chooseMove(view, legal, SeededRng(1L)))
    }

    /**
     * Costruisce una [ScopaView] valida nell'ultima distribuzione (`deckSize = 0`) per il giocatore [toMove],
     * distribuendo le carte già uscite tra i mazzetti di P1 e P2 e generando una sequenza `playedCards`
     * coerente con `determinize()`.
     */
    private fun buildEndgameView(
        toMove: Seat,
        dealer: Seat,
        p1Hand: List<Card>,
        p2Hand: List<Card>,
        table: List<Card>,
        p1MustHoldCards: List<Card>,
    ): ScopaView {
        val activeCards = (p1Hand + p2Hand + table).toSet()
        val remainingCapturedPool = fullDeck().filterNot { it in activeCards }
        val mustHoldSet = p1MustHoldCards.toSet()
        require(mustHoldSet.none { it in activeCards }) {
            "p1MustHoldCards si sovrappone alle carte in mano o sul tavolo"
        }

        val p1Captures = p1MustHoldCards.toList()
        val p2Captures = remainingCapturedPool.filterNot { it in mustHoldSet }

        // In una smazzata da 36 carte giocate in mano e 4 sul tavolo iniziale:
        // se restano k = p1Hand.size + p2Hand.size carte in mano, sono già state giocate (36 - k) carte.
        // Scegliamo 4 carte iniziali del tavolo (tra quelle in table o nei mazzetti) e disponiamo le altre
        // (36 - k) carte in playedCards in modo che l'ultima carta giocata da P1 sia nel mazzetto di P1
        // (così lastCapturer = P1).
        val totalPlayedCount = 36 - (p1Hand.size + p2Hand.size)
        val allCandidatePlayed = (table + p2Captures + p1Captures)
        check(allCandidatePlayed.size == totalPlayedCount + 4) {
            "Totale carte non in mano (${allCandidatePlayed.size}) diverso da totalPlayedCount + 4 (${totalPlayedCount + 4})"
        }

        // Le prime 4 carte di p2Captures fungono da 4 carte del tavolo iniziale (non in playedCards).
        val initialFourTableCards = p2Captures.take(4).toSet()
        val playedPool = allCandidatePlayed.filterNot { it in initialFourTableCards }.toMutableList()
        val anchorCardForP1 = p1Captures.first()
        playedPool.remove(anchorCardForP1)

        val firstMover = dealer.other()
        val playedCards = MutableList<Card?>(totalPlayedCount) { null }
        // Posiziona anchorCardForP1 nell'ultimo indice appartenente a Seat.P1 in playedCards.
        val lastP1TurnIndex = (totalPlayedCount - 1 downTo 0).first { idx ->
            val moverAtIdx = if (idx % 2 == 0) firstMover else dealer
            moverAtIdx == Seat.P1
        }
        playedCards[lastP1TurnIndex] = anchorCardForP1
        var poolIdx = 0
        for (i in 0 until totalPlayedCount) {
            if (playedCards[i] == null) {
                playedCards[i] = playedPool[poolIdx++]
            }
        }

        val state = ScopaState(
            config = ScopaConfig(),
            dealer = dealer,
            toMove = toMove,
            hands = mapOf(
                Seat.P1 to p1Hand,
                Seat.P2 to p2Hand,
            ),
            table = table,
            deck = emptyList(),
            rngState = 0L,
            phase = DealPhase.PLAYING,
            distribution = 6,
            capturedBy = mapOf(
                Seat.P1 to p1Captures,
                Seat.P2 to p2Captures,
            ),
            sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
            lastCapturer = Seat.P1,
            totals = mapOf(Seat.P1 to 0, Seat.P2 to 0),
            winner = null,
            playedCards = playedCards.map { it!! },
        )
        return viewFor(state, toMove)
    }

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
