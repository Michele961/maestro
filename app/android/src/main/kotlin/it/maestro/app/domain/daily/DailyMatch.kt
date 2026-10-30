package it.maestro.app.domain.daily

import java.time.LocalDate

/**
 * La partita del giorno (task F1G-15): stessa data, stesso seed, quindi stessa mano di partenza per tutti.
 *
 * Il bot è deterministico dal seed della partita (`GameViewModel` lo semina con `seed + 9999`): a parità di
 * mosse del giocatore il bot risponde sempre allo stesso modo. Pura: nessun orologio, nessuna rete.
 */
public object DailyMatch {
    /** Punteggio da raggiungere: fisso, perché la partita sia la stessa per tutti. */
    public const val TARGET_SCORE: Int = 21

    private const val SEED_MULTIPLIER: Long = 6364136223846793005L
    private const val SEED_INCREMENT: Long = 1442695040888963407L

    /** Seed della partita di [date], calcolato solo dal giorno. */
    public fun seedFor(date: LocalDate): Long = date.toEpochDay() * SEED_MULTIPLIER + SEED_INCREMENT

    /** `true` se [seed] è quello della partita del giorno [today]. */
    public fun isDailySeed(seed: Long, today: LocalDate): Boolean = seed == seedFor(today)
}

/** Esito della partita del giorno [date]: solo il risultato, mai le mosse. */
public data class DailyMatchResult(
    val date: LocalDate,
    val won: Boolean,
    val humanTotal: Int,
    val opponentTotal: Int,
)

/** Cronologia locale delle partite del giorno, la più recente per prima. Nessuna classifica online. */
public object DailyMatchHistory {
    /** Quante partite si ricordano. */
    public const val MAX_ENTRIES: Int = 30

    /** Aggiunge [result]; il primo risultato di un giorno resta: giocare di nuovo la stessa data non lo cambia. */
    public fun add(history: List<DailyMatchResult>, result: DailyMatchResult): List<DailyMatchResult> =
        if (history.any { it.date == result.date }) history
        else (listOf(result) + history).sortedByDescending { it.date }.take(MAX_ENTRIES)

    /** Vittorie nella cronologia. */
    public fun wins(history: List<DailyMatchResult>): Int = history.count { it.won }

    /** Forma di salvataggio: `data,V|S,mio,avversario` separati da `;`. */
    public fun encode(history: List<DailyMatchResult>): String =
        history.joinToString(";") { "${it.date},${if (it.won) "W" else "L"},${it.humanTotal},${it.opponentTotal}" }

    /** Inverso di [encode]; le voci illeggibili si scartano senza fermare le altre. */
    public fun decode(text: String): List<DailyMatchResult> =
        text.split(";").mapNotNull { entry ->
            val parts = entry.split(",")
            runCatching {
                DailyMatchResult(LocalDate.parse(parts[0]), parts[1] == "W", parts[2].toInt(), parts[3].toInt())
            }.getOrNull()
        }
}
