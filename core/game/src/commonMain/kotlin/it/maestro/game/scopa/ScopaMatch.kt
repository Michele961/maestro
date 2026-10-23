package it.maestro.game.scopa

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.fullDeck
import it.maestro.game.rng.SeededRng

private const val FULL_DECK_SIZE: Int = 40
private const val INITIAL_HAND_CARDS_TOTAL: Int = 6
private const val INITIAL_TABLE_END_INDEX: Int = 10
private const val REDEAL_KINGS_THRESHOLD: Int = 3

/**
 * Crea lo stato iniziale di una nuova partita di Scopa a partire da [config] e [seed]
 * (`docs/architecture.md`, spec §2.2, §3.1–3.3, ADR-0009).
 *
 * Ordine vincolante di consumo di [SeededRng] (ADR-0009):
 * 1. `rng.nextInt(2)` sceglie il primo mazziere (`0` = [Seat.P1], `1` = [Seat.P2], spec §2.2).
 * 2. `rng.shuffle(fullDeck())` mescola il mazzo canonico per la prima smazzata (spec §3.1).
 * 3. Le carte sono distribuite una alla volta dalla cima (indice `0`), alternando a partire
 *    dall'avversario del mazziere fino a 3 carte a testa (indici `0..5`), seguite da 4 carte
 *    scoperte sul tavolo (indici `6..9`) e 30 carte rimanenti nel mazzo (indici `10..39`, spec §3.2).
 * 4. Se [ScopaConfig.redealOnThreeKings] è `true` e sul tavolo iniziale ci sono 3 o 4 Re ([Rank.RE]),
 *    la smazzata si annulla e si ripete il mescolamento (`rng.shuffle(fullDeck())`) e la distribuzione
 *    con lo stesso mazziere, proseguendo sulla stessa sequenza di [SeededRng] (spec §3.3).
 *
 * @param config Configurazione della partita (default `ScopaConfig()`).
 * @param seed Seme a 64 bit per inizializzare [SeededRng].
 * @return [ScopaState] iniziale pronto per la prima giocata di `dealer.other()` (spec §4.1).
 */
public fun newMatch(config: ScopaConfig = ScopaConfig(), seed: Long): ScopaState {
    val rng = SeededRng(seed)
    // ADR-0009 (1): nextInt(2) sceglie il primo mazziere (0 = P1, 1 = P2).
    val dealer = if (rng.nextInt(2) == 0) Seat.P1 else Seat.P2
    return dealOpeningHand(
        config = config,
        dealer = dealer,
        nextShuffledDeck = { rng.shuffle(fullDeck()) },
        currentRngState = { rng.state },
    )
}

/**
 * Esegue la distribuzione iniziale di una smazzata per [dealer], applicando l'eventuale
 * rimescolamento per la regola dei 3 o 4 Re (spec §3.2–3.3).
 *
 * Esposta come `internal` per consentire ai test del modulo di verificare deterministamente
 * i casi limite di spec §3.3 (2, 3 o 4 Re sul tavolo) fornendo una sequenza di mazzi fissata.
 */
internal fun dealOpeningHand(
    config: ScopaConfig,
    dealer: Seat,
    nextShuffledDeck: () -> List<Card>,
    currentRngState: () -> Long = { 0L },
): ScopaState {
    val firstReceiver = dealer.other()
    while (true) {
        val shuffled = nextShuffledDeck()
        require(shuffled.size == FULL_DECK_SIZE) {
            "Il mazzo deve contenere $FULL_DECK_SIZE carte, trovate ${shuffled.size}"
        }
        val table = shuffled.subList(INITIAL_HAND_CARDS_TOTAL, INITIAL_TABLE_END_INDEX).toList()
        if (config.redealOnThreeKings && table.count { it.rank == Rank.RE } >= REDEAL_KINGS_THRESHOLD) {
            // spec §3.3: 3 o 4 Re sul tavolo annullano la smazzata; stesso mazziere, nuovo mescolamento.
            continue
        }

        // spec §3.2: una carta alla volta dalla cima, alternando, iniziando dall'avversario del mazziere.
        val firstReceiverHand = listOf(shuffled[0], shuffled[2], shuffled[4])
        val dealerHand = listOf(shuffled[1], shuffled[3], shuffled[5])
        val hands = mapOf(
            Seat.P1 to if (firstReceiver == Seat.P1) firstReceiverHand else dealerHand,
            Seat.P2 to if (firstReceiver == Seat.P2) firstReceiverHand else dealerHand,
        )
        val remainingDeck = shuffled.subList(INITIAL_TABLE_END_INDEX, FULL_DECK_SIZE).toList()

        return ScopaState(
            config = config,
            dealer = dealer,
            toMove = firstReceiver,
            hands = hands,
            table = table,
            deck = remainingDeck,
            rngState = currentRngState(),
        )
    }
}
