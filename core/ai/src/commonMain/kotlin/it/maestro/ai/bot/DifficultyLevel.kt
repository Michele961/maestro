package it.maestro.ai.bot

import it.maestro.game.model.Card
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.ScopaView

/**
 * Configurazione dei parametri operativi per un bot basato su PIMC (`docs/ai/maestro.md`, task F1-03, decisione D3).
 *
 * @property budget Budget di ricerca in campioni e nodi per mossa.
 * @property noiseProbability Probabilità di errore o mossa casuale nell'intervallo `[0.0, 1.0]`.
 * @property forgetRate Tasso di decadimento mnemonico nell'intervallo `[0.0, 1.0]` con cui il bot "dimentica"
 *   carte passate già catturate, trattandole come se fossero ancora incognite.
 */
public data class PimcConfig(
    public val budget: SearchBudget = SearchBudget(),
    public val noiseProbability: Double = 0.0,
    public val forgetRate: Double = 0.0,
) {
    init {
        require(noiseProbability in 0.0..1.0) {
            "noiseProbability deve essere compresa tra 0.0 e 1.0, trovato: $noiseProbability"
        }
        require(forgetRate in 0.0..1.0) {
            "forgetRate deve essere compreso tra 0.0 e 1.0, trovato: $forgetRate"
        }
    }
}

/**
 * I 5 livelli di difficoltà del Maestro per la Scopa offline (`docs/roadmap.md` §3 decisione D3, task F1-03).
 *
 * Ciascun livello è derivato interamente dall'architettura [PimcBot], variando i quattro parametri:
 * numero di campioni, nodi, rumore nella scelta e memoria imperfetta.
 *
 * @property targetElo Valore Elo di riferimento atteso per la calibrazione relativa.
 * @property config Configurazione [PimcConfig] specifica del livello.
 */
public enum class DifficultyLevel(
    public val targetElo: Int,
    public val config: PimcConfig,
) {
    /**
     * Livello 1: Principiante.
     * Alto rumore decisionale, forte amnesia sulle carte passate, budget minimo.
     */
    PRINCIPIANTE(
        targetElo = 800,
        config = PimcConfig(
            budget = SearchBudget(samples = 2, maxNodes = 10),
            noiseProbability = 0.35,
            forgetRate = 0.60,
        ),
    ),

    /**
     * Livello 2: Dilettante.
     * Moderato rumore decisionale, parziale amnesia sulle carte passate, budget ridotto.
     */
    DILETTANTE(
        targetElo = 1100,
        config = PimcConfig(
            budget = SearchBudget(samples = 5, maxNodes = 25),
            noiseProbability = 0.18,
            forgetRate = 0.35,
        ),
    ),

    /**
     * Livello 3: Intermedio.
     * Basso rumore decisionale, lieve amnesia, budget moderato.
     */
    INTERMEDIO(
        targetElo = 1400,
        config = PimcConfig(
            budget = SearchBudget(samples = 12, maxNodes = 60),
            noiseProbability = 0.05,
            forgetRate = 0.15,
        ),
    ),

    /**
     * Livello 4: Avanzato.
     * Zero rumore, memoria perfetta delle carte uscite, budget consistente.
     */
    AVANZATO(
        targetElo = 1700,
        config = PimcConfig(
            budget = SearchBudget(samples = 25, maxNodes = 120),
            noiseProbability = 0.0,
            forgetRate = 0.0,
        ),
    ),

    /**
     * Livello 5: Maestro.
     * Zero rumore, memoria perfetta, massima profondità e campionamento PIMC completo.
     */
    MAESTRO(
        targetElo = 2000,
        config = PimcConfig(
            budget = SearchBudget(samples = 50, maxNodes = 200),
            noiseProbability = 0.0,
            forgetRate = 0.0,
        ),
    );

    /**
     * Crea un'istanza di [PimcBot] calibrata con i parametri di questo livello di difficoltà.
     */
    public fun toBot(evaluator: MoveEvaluator = PimcEvaluator): PimcBot =
        PimcBot(config = config, evaluator = evaluator)

    public companion object {
        /**
         * Ricerca un livello di difficoltà a partire dal nome (case-insensitive) o prefisso `level:<nome>`.
         */
        public fun fromNameOrNull(name: String): DifficultyLevel? {
            val cleanName = name.removePrefix("level:").trim()
            return entries.firstOrNull { it.name.equals(cleanName, ignoreCase = true) }
        }
    }
}

/**
 * Applica il meccanismo di memoria imperfetta a partire unicamente da [view] e da [rng] (`docs/ai/maestro.md`).
 *
 * Non accede **mai** allo stato globale della partita né a carte nascoste. Rimuove stocasticamente
 * carte dai mazzetti di presa visibili (`view.capturedBy`) e dalle carte giocate (`view.playedCards`),
 * incrementando contestualmente [ScopaView.deckSize] in modo tale da preservare rigorosamente l'invariante
 * delle 40 carte totali per la determinizzazione PIMC.
 */
internal fun applyImperfectMemory(
    view: ScopaView,
    forgetRate: Double,
    rng: SeededRng,
): ScopaView {
    if (forgetRate <= 0.0) return view
    val p1Captured = view.capturedBy[Seat.P1].orEmpty()
    val p2Captured = view.capturedBy[Seat.P2].orEmpty()
    if (p1Captured.isEmpty() && p2Captured.isEmpty()) return view

    val threshold = (forgetRate.coerceIn(0.0, 1.0) * 10000).toInt()
    val forgotten = mutableSetOf<Card>()

    val newP1 = mutableListOf<Card>()
    for (card in p1Captured) {
        if (rng.nextInt(10000) < threshold) {
            forgotten.add(card)
        } else {
            newP1.add(card)
        }
    }

    val newP2 = mutableListOf<Card>()
    for (card in p2Captured) {
        if (rng.nextInt(10000) < threshold) {
            forgotten.add(card)
        } else {
            newP2.add(card)
        }
    }

    if (forgotten.isEmpty()) return view

    val newPlayed = view.playedCards.filterNot { it in forgotten }
    val newDeckSize = view.deckSize + forgotten.size

    return view.copy(
        capturedBy = mapOf(
            Seat.P1 to newP1,
            Seat.P2 to newP2,
        ),
        playedCards = newPlayed,
        deckSize = newDeckSize,
    )
}
