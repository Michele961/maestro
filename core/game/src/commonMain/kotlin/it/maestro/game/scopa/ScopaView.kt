package it.maestro.game.scopa

import it.maestro.game.model.Card
import it.maestro.game.model.Seat
import it.maestro.game.model.fullDeck

private const val FULL_DECK_SIZE: Int = 40
private const val CARDS_PER_REDEAL: Int = 6
private const val MAX_DISTRIBUTIONS_PER_DEAL: Int = 6

/**
 * Vista filtrata dello stato di una partita di Scopa per il giocatore seduto in [seat]
 * (`docs/architecture.md`, ADR-0002, task F0-07).
 *
 * Costituisce l'unica struttura dati ricevuta dai bot AI (`docs/ai/maestro.md`) e, in Fase 2,
 * dal client di gioco online. Non espone **mai** la mano dell'avversario, l'ordine delle carte
 * nel mazzo coperto né lo stato interno del generatore pseudocasuale (`rngState`).
 *
 * @property seat Posto del giocatore a cui appartiene questa vista ([Seat.P1] o [Seat.P2]).
 * @property config Configurazione delle regole e varianti della partita (spec §8).
 * @property dealer Giocatore che distribuisce le carte nella smazzata corrente (spec §2.2, §2.3).
 * @property toMove Giocatore di turno (spec §4.1).
 * @property phase Fase corrente della smazzata o della partita ([DealPhase.PLAYING], [DealPhase.DEAL_OVER]
 *   o [DealPhase.MATCH_OVER]).
 * @property hand Carte attualmente nella mano di [seat].
 * @property table Carte scoperte al centro del tavolo, nell'ordine di uscita (spec §3.2).
 * @property capturedBy Mazzetto delle prese effettuate da ciascun giocatore nella smazzata corrente (spec §4.7).
 * @property playedCards Carte giocate dalla mano nella smazzata corrente, nell'ordine di uscita.
 * @property opponentHandSize Numero di carte attualmente nella mano dell'avversario (`seat.other()`).
 * @property deckSize Numero di carte rimaste nel tallone coperto.
 * @property sweeps Conteggio delle scope realizzate da ciascun giocatore nella smazzata corrente (spec §5.1).
 * @property totals Punteggi totali di partita accumulati da ciascun giocatore a fine smazzata (spec §2.1, §7).
 */
public data class ScopaView(
    public val seat: Seat,
    public val config: ScopaConfig,
    public val dealer: Seat,
    public val toMove: Seat,
    public val phase: DealPhase,
    public val hand: List<Card>,
    public val table: List<Card>,
    public val capturedBy: Map<Seat, List<Card>>,
    public val playedCards: List<Card>,
    public val opponentHandSize: Int,
    public val deckSize: Int,
    public val sweeps: Map<Seat, Int>,
    public val totals: Map<Seat, Int>,
)

/**
 * Costruisce la [ScopaView] per il giocatore seduto in [seat] a partire dallo stato completo [state]
 * (`docs/architecture.md`, ADR-0002, task F0-07).
 *
 * La vista restituita contiene solo le informazioni visibili a [seat] e omette mano avversaria,
 * ordine del mazzo coperto e stato del generatore.
 */
public fun viewFor(state: ScopaState, seat: Seat): ScopaView = ScopaView(
    seat = seat,
    config = state.config,
    dealer = state.dealer,
    toMove = state.toMove,
    phase = state.phase,
    hand = state.hand(seat),
    table = state.table,
    capturedBy = state.capturedBy,
    playedCards = state.playedCards,
    opponentHandSize = state.hand(seat.other()).size,
    deckSize = state.deck.size,
    sweeps = state.sweeps,
    totals = state.totals,
)

/**
 * Ricostruisce uno [ScopaState] completo a partire dalla vista [view] di un giocatore e da un'ipotesi
 * sulle carte nascoste: la mano avversaria [opponentHand] e l'ordine del tallone coperto [deckOrder]
 * (`docs/architecture.md`, `docs/ai/maestro.md`, task F0-07).
 *
 * Funzione destinata al campionamento PIMC dell'AI: verifica rigorosamente che l'ipotesi sia compatibile
 * con tutte le informazioni pubbliche e private presenti in [view] e rifiuta con [IllegalArgumentException]
 * qualunque distribuzione incoerente.
 *
 * Condizioni di compatibilità verificate:
 * - `opponentHand.size == view.opponentHandSize` e `deckOrder.size == view.deckSize`;
 * - nessuna carta duplicata in [opponentHand], in [deckOrder] o tra i due;
 * - nessuna carta in [opponentHand] o [deckOrder] appartiene alle carte già note nella vista
 *   ([ScopaView.hand], [ScopaView.table], [ScopaView.capturedBy], [ScopaView.playedCards]);
 * - l'unione delle carte visibili e delle carte ipotizzate forma esattamente le 40 carte del mazzo canonico.
 *
 * @throws IllegalArgumentException se [opponentHand] o [deckOrder] sono incompatibili con [view].
 */
public fun determinize(
    view: ScopaView,
    opponentHand: List<Card>,
    deckOrder: List<Card>,
): ScopaState {
    require(opponentHand.size == view.opponentHandSize) {
        "Dimensione opponentHand incompatibile con la vista: attese ${view.opponentHandSize}, trovate ${opponentHand.size}"
    }
    require(deckOrder.size == view.deckSize) {
        "Dimensione deckOrder incompatibile con la vista: attese ${view.deckSize}, trovate ${deckOrder.size}"
    }

    val p1Captured = view.capturedBy[Seat.P1].orEmpty()
    val p2Captured = view.capturedBy[Seat.P2].orEmpty()
    val knownLocationsCount = view.hand.size + view.table.size + p1Captured.size + p2Captured.size
    val knownCards = buildSet {
        addAll(view.hand)
        addAll(view.table)
        addAll(p1Captured)
        addAll(p2Captured)
        addAll(view.playedCards)
    }
    require(knownCards.size == knownLocationsCount) {
        "Vista incoerente: carte duplicate o playedCards non coerenti con mano, tavolo e prese"
    }

    val opponentSet = opponentHand.toSet()
    require(opponentSet.size == opponentHand.size) {
        "opponentHand contiene carte duplicate: $opponentHand"
    }
    val deckSet = deckOrder.toSet()
    require(deckSet.size == deckOrder.size) {
        "deckOrder contiene carte duplicate: $deckOrder"
    }

    val overlapBetweenHidden = opponentSet intersect deckSet
    require(overlapBetweenHidden.isEmpty()) {
        "Carte duplicate tra opponentHand e deckOrder: $overlapBetweenHidden"
    }

    val seenInOpponent = opponentSet intersect knownCards
    require(seenInOpponent.isEmpty()) {
        "opponentHand contiene carte già viste nella vista: $seenInOpponent"
    }

    val seenInDeck = deckSet intersect knownCards
    require(seenInDeck.isEmpty()) {
        "deckOrder contiene carte già viste nella vista: $seenInDeck"
    }

    val allCardsCount = knownLocationsCount + opponentHand.size + deckOrder.size
    require(allCardsCount == FULL_DECK_SIZE) {
        "Il totale delle carte (viste + nascoste) deve essere $FULL_DECK_SIZE, trovato $allCardsCount"
    }

    val allUniqueCards = knownCards + opponentSet + deckSet
    require(allUniqueCards.size == FULL_DECK_SIZE && allUniqueCards == fullDeck().toSet()) {
        "L'insieme complessivo delle carte non coincide con il mazzo canonico di $FULL_DECK_SIZE carte"
    }

    val hands = mapOf(
        Seat.P1 to if (view.seat == Seat.P1) view.hand else opponentHand,
        Seat.P2 to if (view.seat == Seat.P2) view.hand else opponentHand,
    )

    val distribution = inferDistribution(view)
    val lastCapturer = inferLastCapturer(view)
    val winner = inferWinner(view)

    return ScopaState(
        config = view.config,
        dealer = view.dealer,
        toMove = view.toMove,
        hands = hands,
        table = view.table,
        deck = deckOrder,
        rngState = 0L,
        phase = view.phase,
        distribution = distribution,
        capturedBy = mapOf(
            Seat.P1 to p1Captured,
            Seat.P2 to p2Captured,
        ),
        sweeps = view.sweeps,
        lastCapturer = lastCapturer,
        totals = view.totals,
        winner = winner,
        playedCards = view.playedCards,
    )
}

private fun inferDistribution(view: ScopaView): Int {
    if (view.phase != DealPhase.PLAYING) {
        return MAX_DISTRIBUTIONS_PER_DEAL
    }
    return ( MAX_DISTRIBUTIONS_PER_DEAL - (view.deckSize / CARDS_PER_REDEAL) )
        .coerceIn(1, MAX_DISTRIBUTIONS_PER_DEAL)
}

private fun inferLastCapturer(view: ScopaView): Seat? {
    val p1Captured = view.capturedBy[Seat.P1].orEmpty()
    val p2Captured = view.capturedBy[Seat.P2].orEmpty()
    if (p1Captured.isEmpty() && p2Captured.isEmpty()) {
        return null
    }
    if (p1Captured.isEmpty()) {
        return Seat.P2
    }
    if (p2Captured.isEmpty()) {
        return Seat.P1
    }

    // Ricostruisce l'ultimo indice di turno in playedCards che ha generato una presa per ciascun Seat.
    // Nella Scopa a 2 i turni della smazzata alternano sempre: indice pari -> dealer.other(), dispari -> dealer.
    val playIndexByCard = HashMap<Card, Int>(view.playedCards.size)
    for ((index, card) in view.playedCards.withIndex()) {
        playIndexByCard[card] = index
    }
    val lastP1CaptureTurn = findLastCaptureTurnIndex(
        seat = Seat.P1,
        dealer = view.dealer,
        capturedPile = p1Captured,
        playIndexByCard = playIndexByCard,
    )
    val lastP2CaptureTurn = findLastCaptureTurnIndex(
        seat = Seat.P2,
        dealer = view.dealer,
        capturedPile = p2Captured,
        playIndexByCard = playIndexByCard,
    )
    return if (lastP1CaptureTurn >= lastP2CaptureTurn) Seat.P1 else Seat.P2
}

/**
 * Individua il più grande indice `k` in `playedCards` corrispondente a una carta giocata da [seat]
 * che ha effettuato una presa nel mazzetto [capturedPile].
 *
 * In `ScopaPlay`, ogni presa aggiunge a `capturedBy[seat]` la carta giocata `move.card` seguita da
 * almeno una carta presa dal tavolo (che era sul tavolo prima del turno `k`, dunque o era una delle
 * 4 carte iniziali non presenti in `playedCards` oppure è stata scartata in un turno `< k`).
 */
private fun findLastCaptureTurnIndex(
    seat: Seat,
    dealer: Seat,
    capturedPile: List<Card>,
    playIndexByCard: Map<Card, Int>,
): Int {
    val firstMover = dealer.other()
    var maxTurn = -1
    var i = 0
    while (i < capturedPile.size) {
        val candidatePlayedCard = capturedPile[i]
        val turnIndex = playIndexByCard[candidatePlayedCard]
        val moverAtTurn = if (turnIndex != null && turnIndex % 2 == 0) firstMover else dealer
        if (turnIndex != null && moverAtTurn == seat && turnIndex > maxTurn) {
            maxTurn = turnIndex
        }
        i++
    }
    return maxTurn
}

private fun inferWinner(view: ScopaView): Seat? {
    if (view.phase != DealPhase.MATCH_OVER) {
        return null
    }
    val p1Total = view.totals[Seat.P1] ?: 0
    val p2Total = view.totals[Seat.P2] ?: 0
    return when {
        p1Total > p2Total -> Seat.P1
        p2Total > p1Total -> Seat.P2
        else -> null
    }
}
