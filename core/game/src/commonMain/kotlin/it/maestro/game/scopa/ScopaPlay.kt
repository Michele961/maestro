package it.maestro.game.scopa

import it.maestro.game.model.Card
import it.maestro.game.model.CardOrder
import it.maestro.game.model.Seat

private const val REDEAL_CARDS_TOTAL: Int = 6
private const val MIN_SUM_CAPTURE_SIZE: Int = 2

/**
 * Restituisce tutte le mosse legali per il giocatore di turno ([ScopaState.toMove]) nello stato [state],
 * in ordine deterministico e canonico (`docs/architecture.md`, spec §4.2–4.6, §4.9).
 *
 * Regole applicate:
 * - Se [ScopaState.phase] non è [DealPhase.PLAYING], restituisce una lista vuota (comportamento di confine F0-05).
 * - Le carte nella mano del giocatore di turno sono considerate in ordine canonico ([CardOrder], spec §4.9).
 * - **Precedenza della carta uguale (spec §4.2):** se sul tavolo c'è almeno una carta dello stesso valore
 *   ([Card.rank]) della carta giocata, le uniche prese ammesse per quella carta sono i singoletti
 *   delle carte uguali sul tavolo, ordinate secondo [CardOrder].
 * - **Presa per somma (spec §4.3):** se sul tavolo non c'è alcuna carta dello stesso valore, sono ammessi
 *   tutti i sottoinsiemi di almeno 2 carte del tavolo la cui somma di `rank.value` eguaglia `card.rank.value`.
 *   Ogni sottoinsieme è ordinato internamente secondo [CardOrder], e i sottoinsiemi sono ordinati
 *   lessicograficamente carta per carta (spec §4.9).
 * - **Presa obbligatoria e scarto (spec §4.4–4.6):** una mossa con [ScopaMove.capture] vuoto è generata
 *   solo se la carta giocata non può effettuare alcuna presa né per uguaglianza né per somma.
 */
public fun legalMoves(state: ScopaState): List<ScopaMove> {
    if (state.phase != DealPhase.PLAYING) {
        return emptyList()
    }

    val sortedHand = state.hand(state.toMove).sortedWith(CardOrder)
    val sortedTable = state.table.sortedWith(CardOrder)

    return buildList {
        for (card in sortedHand) {
            val equalRankCards = sortedTable.filter { it.rank == card.rank }
            if (equalRankCards.isNotEmpty()) {
                // spec §4.2: precedenza della carta uguale sulla presa per somma.
                for (matchingCard in equalRankCards) {
                    add(ScopaMove(card = card, capture = setOf(matchingCard)))
                }
            } else {
                // spec §4.3: presa per somma di due o più carte del tavolo.
                val sumCaptures = findSumCaptures(sortedTable, targetSum = card.rank.value)
                if (sumCaptures.isNotEmpty()) {
                    for (captureSet in sumCaptures) {
                        add(ScopaMove(card = card, capture = captureSet))
                    }
                } else {
                    // spec §4.4, §4.5, §4.6: nessuna presa possibile, la carta viene calata sul tavolo.
                    add(ScopaMove(card = card, capture = emptySet()))
                }
            }
        }
    }
}

/**
 * Applica la mossa [move] allo stato [state], restituendo un [PlayResult] tipizzato senza mai
 * lanciare eccezioni per mosse non valide (`docs/architecture.md`, spec §3.4, §4, §5).
 *
 * - Se [ScopaState.phase] è [DealPhase.DEAL_OVER], restituisce [PlayResult.Rejected] con [RejectReason.DEAL_OVER].
 * - Se [move] non appartiene a [legalMoves]`(state)`, restituisce [PlayResult.Rejected] con
 *   [RejectReason.NOT_IN_LEGAL_MOVES] lasciando [state] invariato.
 * - Altrimenti applica la giocata, aggiorna tavolo, mazzetto delle prese, conteggio scope, eventuale
 *   ridistribuzione automatica (spec §3.4) o passaggio a [DealPhase.DEAL_OVER] quando mani e mazzo sono vuoti,
 *   e restituisce [PlayResult.Applied] con la lista ordinata di [GameEvent].
 */
public fun play(state: ScopaState, move: ScopaMove): PlayResult {
    return when (state.phase) {
        DealPhase.DEAL_OVER -> PlayResult.Rejected(RejectReason.DEAL_OVER)
        DealPhase.PLAYING -> applyMoveIfLegal(state, move)
    }
}

private fun applyMoveIfLegal(state: ScopaState, move: ScopaMove): PlayResult {
    val legal = legalMoves(state)
    if (move !in legal) {
        return PlayResult.Rejected(RejectReason.NOT_IN_LEGAL_MOVES)
    }

    val seat = state.toMove
    val events = mutableListOf<GameEvent>()
    events.add(GameEvent.CardPlayed(seat = seat, card = move.card))

    val updatedMoverHand = state.hand(seat).filterNot { it == move.card }
    val handsAfterPlay = mapOf(
        Seat.P1 to if (seat == Seat.P1) updatedMoverHand else state.hand(Seat.P1),
        Seat.P2 to if (seat == Seat.P2) updatedMoverHand else state.hand(Seat.P2),
    )
    val bothHandsEmpty = handsAfterPlay.getValue(Seat.P1).isEmpty() &&
        handsAfterPlay.getValue(Seat.P2).isEmpty()

    val newTable: List<Card>
    val newCapturedBy: Map<Seat, List<Card>>
    val newSweeps: Map<Seat, Int>
    val newLastCapturer: Seat?

    if (move.capture.isEmpty()) {
        // spec §4.5, §4.6: la carta giocata resta scoperta sul tavolo.
        newTable = state.table + move.card
        newCapturedBy = state.capturedBy
        newSweeps = state.sweeps
        newLastCapturer = state.lastCapturer
    } else {
        // spec §4.7: le carte prese e la carta giocata vanno nel mazzetto delle prese del giocatore.
        val canonicalCaptureList = move.capture.sortedWith(CardOrder)
        val canonicalCaptureSet = canonicalCaptureList.toSet()
        newTable = state.table.filterNot { it in canonicalCaptureSet }
        val updatedSeatCaptures = state.captured(seat) + move.card + canonicalCaptureList
        newCapturedBy = mapOf(
            Seat.P1 to if (seat == Seat.P1) updatedSeatCaptures else state.captured(Seat.P1),
            Seat.P2 to if (seat == Seat.P2) updatedSeatCaptures else state.captured(Seat.P2),
        )
        newLastCapturer = seat
        events.add(GameEvent.Captured(seat = seat, cards = canonicalCaptureSet))

        // spec §5.1, §5.2: presa che svuota il tavolo vale scopa, tranne all'ultima giocata della smazzata.
        val isLastPlayOfDeal = bothHandsEmpty && state.deck.isEmpty()
        if (newTable.isEmpty() && !isLastPlayOfDeal) {
            val updatedSeatSweeps = state.sweeps.getValue(seat) + 1
            newSweeps = mapOf(
                Seat.P1 to if (seat == Seat.P1) updatedSeatSweeps else state.sweeps.getValue(Seat.P1),
                Seat.P2 to if (seat == Seat.P2) updatedSeatSweeps else state.sweeps.getValue(Seat.P2),
            )
            events.add(GameEvent.Sweep(seat = seat))
        } else {
            newSweeps = state.sweeps
        }
    }

    val finalHands: Map<Seat, List<Card>>
    val finalDeck: List<Card>
    val finalDistribution: Int
    val finalPhase: DealPhase

    if (bothHandsEmpty) {
        if (state.deck.isNotEmpty()) {
            // spec §3.4: ridistribuzione automatica di 3 carte a testa, una alla volta,
            // iniziando dall'avversario del mazziere, senza carte nuove sul tavolo.
            check(state.deck.size >= REDEAL_CARDS_TOTAL) {
                "Il mazzo deve contenere almeno $REDEAL_CARDS_TOTAL carte per ridistribuire, trovate ${state.deck.size}"
            }
            val firstReceiver = state.dealer.other()
            val firstReceiverHand = listOf(state.deck[0], state.deck[2], state.deck[4])
            val dealerHand = listOf(state.deck[1], state.deck[3], state.deck[5])
            finalHands = mapOf(
                Seat.P1 to if (firstReceiver == Seat.P1) firstReceiverHand else dealerHand,
                Seat.P2 to if (firstReceiver == Seat.P2) firstReceiverHand else dealerHand,
            )
            finalDeck = state.deck.subList(REDEAL_CARDS_TOTAL, state.deck.size).toList()
            finalDistribution = state.distribution + 1
            finalPhase = DealPhase.PLAYING
            events.add(GameEvent.CardsDealt(distribution = finalDistribution))
        } else {
            // F0-05 comportamento di confine: fine della smazzata -> DEAL_OVER.
            finalHands = handsAfterPlay
            finalDeck = state.deck
            finalDistribution = state.distribution
            finalPhase = DealPhase.DEAL_OVER
        }
    } else {
        finalHands = handsAfterPlay
        finalDeck = state.deck
        finalDistribution = state.distribution
        finalPhase = DealPhase.PLAYING
    }

    val newState = state.copy(
        toMove = seat.other(),
        hands = finalHands,
        table = newTable,
        deck = finalDeck,
        phase = finalPhase,
        distribution = finalDistribution,
        capturedBy = newCapturedBy,
        sweeps = newSweeps,
        lastCapturer = newLastCapturer,
    )

    return PlayResult.Applied(state = newState, events = events.toList())
}

/**
 * Trova tutti i sottoinsiemi di almeno [MIN_SUM_CAPTURE_SIZE] carte di [sortedTable] (già ordinato
 * secondo [CardOrder]) la cui somma di `rank.value` è uguale a [targetSum], restituendoli ordinati
 * secondo l'ordine lessicografico canonico di spec §4.9.
 */
private fun findSumCaptures(sortedTable: List<Card>, targetSum: Int): List<Set<Card>> {
    val results = mutableListOf<List<Card>>()
    val current = mutableListOf<Card>()

    fun backtrack(startIndex: Int, remainingSum: Int) {
        if (remainingSum == 0 && current.size >= MIN_SUM_CAPTURE_SIZE) {
            results.add(current.toList())
            return
        }
        if (remainingSum <= 0) {
            return
        }
        for (i in startIndex until sortedTable.size) {
            val candidate = sortedTable[i]
            val value = candidate.rank.value
            if (value <= remainingSum) {
                current.add(candidate)
                backtrack(i + 1, remainingSum - value)
                current.removeAt(current.lastIndex)
            }
        }
    }

    backtrack(startIndex = 0, remainingSum = targetSum)
    results.sortWith(CaptureListComparator)
    return results.map { it.toSet() }
}

/**
 * Comparatore lessicografico tra prese ordinate internamente secondo [CardOrder] (spec §4.9):
 * confronta carta per carta con [CardOrder]; se una presa è prefisso dell'altra, viene prima la più corta.
 */
private object CaptureListComparator : Comparator<List<Card>> {
    override fun compare(a: List<Card>, b: List<Card>): Int {
        val minSize = minOf(a.size, b.size)
        for (i in 0 until minSize) {
            val cmp = CardOrder.compare(a[i], b[i])
            if (cmp != 0) return cmp
        }
        return a.size.compareTo(b.size)
    }
}
