package it.maestro.game.scopa

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit

private const val HALF_DECK_CARDS: Int = 20
private const val HALF_DENARI_CARDS: Int = 5

/**
 * Punteggio calcolato al termine di una smazzata di Scopa (spec §6.2, §6.3, task F0-06).
 *
 * Raccoglie i punti assegnati a ciascun giocatore ([Seat]) per ciascuna categoria ([ScoreCategory]),
 * oltre alla somma di primiera di entrambi i giocatori (`null` se un giocatore non possiede almeno
 * una carta in ognuno dei 4 semi, spec §6.3).
 *
 * @property pointsBySeatAndCategory Mappa immutabile che per ogni [Seat] riporta i punti ottenuti
 *   in ciascuna [ScoreCategory].
 * @property primieraSums Mappa immutabile che per ogni [Seat] riporta la somma dei valori di primiera
 *   sui 4 semi, oppure `null` se al giocatore manca almeno un seme (spec §6.3).
 */
public data class DealScore(
    public val pointsBySeatAndCategory: Map<Seat, Map<ScoreCategory, Int>>,
    public val primieraSums: Map<Seat, Int?>,
) {
    /**
     * Vista indicizzata per [ScoreCategory] e poi per [Seat] dei punti della smazzata.
     */
    public val byCategory: Map<ScoreCategory, Map<Seat, Int>> =
        ScoreCategory.entries.associateWith { category ->
            mapOf(
                Seat.P1 to points(Seat.P1, category),
                Seat.P2 to points(Seat.P2, category),
            )
        }

    /**
     * Totale dei punti della smazzata ottenuti da ciascun [Seat], pari alla somma su tutte le [ScoreCategory].
     */
    public val totalsBySeat: Map<Seat, Int> = mapOf(
        Seat.P1 to totalPoints(Seat.P1),
        Seat.P2 to totalPoints(Seat.P2),
    )

    /**
     * Restituisce i punti ottenuti da [seat] nella categoria [category] in questa smazzata.
     */
    public fun points(seat: Seat, category: ScoreCategory): Int =
        pointsBySeatAndCategory.getValue(seat).getValue(category)

    /**
     * Restituisce la mappa dei punti per [ScoreCategory] ottenuti da [seat] in questa smazzata.
     */
    public fun pointsFor(seat: Seat): Map<ScoreCategory, Int> =
        pointsBySeatAndCategory.getValue(seat)

    /**
     * Restituisce il totale dei punti della smazzata ottenuti da [seat] (somma su tutte le [ScoreCategory]).
     */
    public fun totalPoints(seat: Seat): Int =
        pointsBySeatAndCategory.getValue(seat).values.sum()

    /**
     * Restituisce la somma di primiera di [seat] sui 4 semi, oppure `null` se [seat] non ha almeno
     * una carta in ciascuno dei 4 semi (spec §6.3).
     */
    public fun primieraSum(seat: Seat): Int? = primieraSums[seat]
}

/**
 * Valore di primiera associato a ciascun [Rank] secondo la tabella normativa di spec §6.3:
 * - [Rank.SETTE] = `21`
 * - [Rank.SEI] = `18`
 * - [Rank.ASSO] = `16`
 * - [Rank.CINQUE] = `15`
 * - [Rank.QUATTRO] = `14`
 * - [Rank.TRE] = `13`
 * - [Rank.DUE] = `12`
 * - [Rank.FANTE], [Rank.CAVALLO], [Rank.RE] = `10`
 */
public val Rank.primieraPoints: Int
    get() = when (this) {
        Rank.SETTE -> 21
        Rank.SEI -> 18
        Rank.ASSO -> 16
        Rank.CINQUE -> 15
        Rank.QUATTRO -> 14
        Rank.TRE -> 13
        Rank.DUE -> 12
        Rank.FANTE, Rank.CAVALLO, Rank.RE -> 10
    }

/**
 * Calcola il [DealScore] di fine smazzata a partire dalla configurazione [config], dalle prese finali
 * [capturedBy] (comprensive delle eventuali carte residue assegnate secondo spec §6.1) e dal conteggio
 * delle scope [sweeps] (spec §6.2, §6.3).
 */
internal fun calculateDealScore(
    config: ScopaConfig,
    capturedBy: Map<Seat, List<Card>>,
    sweeps: Map<Seat, Int>,
): DealScore {
    val p1Captured = capturedBy.getValue(Seat.P1)
    val p2Captured = capturedBy.getValue(Seat.P2)

    // spec §6.2 Carte: più carte prese (oltre 20); parità 20-20 -> nessuno.
    val p1Carte = if (p1Captured.size > HALF_DECK_CARDS && p1Captured.size > p2Captured.size) 1 else 0
    val p2Carte = if (p2Captured.size > HALF_DECK_CARDS && p2Captured.size > p1Captured.size) 1 else 0

    // spec §6.2 Denari: più carte di Denari (oltre 5); parità 5-5 -> nessuno.
    val p1DenariCount = p1Captured.count { it.suit == Suit.DENARI }
    val p2DenariCount = p2Captured.count { it.suit == Suit.DENARI }
    val p1Denari = if (p1DenariCount > HALF_DENARI_CARDS && p1DenariCount > p2DenariCount) 1 else 0
    val p2Denari = if (p2DenariCount > HALF_DENARI_CARDS && p2DenariCount > p1DenariCount) 1 else 0

    // spec §6.2 Settebello: chi ha preso il 7 di Denari.
    val settebello = Card(Rank.SETTE, Suit.DENARI)
    val p1Settebello = if (settebello in p1Captured) 1 else 0
    val p2Settebello = if (settebello in p2Captured) 1 else 0

    // spec §6.3 Primiera: somma dei massimi valori di primiera in ciascuno dei 4 semi;
    // chi non ha almeno una carta in ognuno dei 4 semi non può vincere la primiera (somma = null).
    val p1PrimieraSum = computePrimieraSum(p1Captured)
    val p2PrimieraSum = computePrimieraSum(p2Captured)
    val (p1Primiera, p2Primiera) = when {
        p1PrimieraSum != null && p2PrimieraSum != null -> when {
            p1PrimieraSum > p2PrimieraSum -> 1 to 0
            p2PrimieraSum > p1PrimieraSum -> 0 to 1
            else -> 0 to 0
        }
        p1PrimieraSum != null -> 1 to 0
        p2PrimieraSum != null -> 0 to 1
        else -> 0 to 0
    }

    // spec §6.2 Scope: 1 punto per ogni scopa.
    val p1Scope = sweeps.getValue(Seat.P1)
    val p2Scope = sweeps.getValue(Seat.P2)

    // spec §6.2 Re bello: chi ha preso il Re di Denari, solo con variante reBello attiva.
    val reDiDenari = Card(Rank.RE, Suit.DENARI)
    val p1ReBello = if (config.reBello && reDiDenari in p1Captured) 1 else 0
    val p2ReBello = if (config.reBello && reDiDenari in p2Captured) 1 else 0

    // spec §6.4 Napola: sequenza di Denari dall'Asso (almeno Asso, 2, 3), solo con variante napola attiva.
    val p1Napola = if (config.napola) napolaPoints(p1Captured) else 0
    val p2Napola = if (config.napola) napolaPoints(p2Captured) else 0

    val p1Points = mapOf(
        ScoreCategory.CARTE to p1Carte,
        ScoreCategory.DENARI to p1Denari,
        ScoreCategory.SETTEBELLO to p1Settebello,
        ScoreCategory.PRIMIERA to p1Primiera,
        ScoreCategory.SCOPE to p1Scope,
        ScoreCategory.RE_BELLO to p1ReBello,
        ScoreCategory.NAPOLA to p1Napola,
    )
    val p2Points = mapOf(
        ScoreCategory.CARTE to p2Carte,
        ScoreCategory.DENARI to p2Denari,
        ScoreCategory.SETTEBELLO to p2Settebello,
        ScoreCategory.PRIMIERA to p2Primiera,
        ScoreCategory.SCOPE to p2Scope,
        ScoreCategory.RE_BELLO to p2ReBello,
        ScoreCategory.NAPOLA to p2Napola,
    )

    return DealScore(
        pointsBySeatAndCategory = mapOf(
            Seat.P1 to p1Points,
            Seat.P2 to p2Points,
        ),
        primieraSums = mapOf(
            Seat.P1 to p1PrimieraSum,
            Seat.P2 to p2PrimieraSum,
        ),
    )
}

/**
 * Calcola la somma di primiera per [captured] secondo spec §6.3:
 * restituisce `null` se manca almeno uno dei 4 semi ([Suit.entries]), altrimenti la somma
 * sui 4 semi del massimo valore [primieraPoints] tra le carte prese di ciascun seme.
 */
internal fun computePrimieraSum(captured: List<Card>): Int? {
    var sum = 0
    for (suit in Suit.entries) {
        val maxInSuit = captured
            .asSequence()
            .filter { it.suit == suit }
            .maxOfOrNull { it.rank.primieraPoints }
            ?: return null
        sum += maxInSuit
    }
    return sum
}

private const val MIN_NAPOLA_RUN: Int = 3

/**
 * Punti della napola di [captured] (spec §6.4): la lunghezza della sequenza continua di Denari che parte dall'Asso
 * (Asso, 2, 3, …), se è di almeno [MIN_NAPOLA_RUN] carte; altrimenti `0`.
 */
internal fun napolaPoints(captured: List<Card>): Int {
    var run = 0
    for (rank in Rank.entries) {
        if (Card(rank, Suit.DENARI) !in captured) break
        run++
    }
    return if (run >= MIN_NAPOLA_RUN) run else 0
}
