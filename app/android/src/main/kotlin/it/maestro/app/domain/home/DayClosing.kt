package it.maestro.app.domain.home

import java.time.LocalDate

/**
 * Che cosa è già fatto oggi (task F1G-12): letto dagli stati esistenti, nessun contatore nuovo.
 *
 * @property puzzleSolvedToday la Sfida del giorno di oggi è risolta.
 * @property dailyMatchPlayedToday la Partita del giorno di oggi è stata giocata fino in fondo (anche persa).
 * @property missionsCompleted le missioni di oggi sono tutte completate (il ritiro dei premi non conta).
 */
public data class DayClosingSignals(
    val puzzleSolvedToday: Boolean = false,
    val dailyMatchPlayedToday: Boolean = false,
    val missionsCompleted: Boolean = false,
)

/**
 * La «chiusura del giorno»: quando sfida, partita e missioni di oggi sono fatte il Maestro saluta e annuncia il
 * domani, una volta al giorno. Dominio puro: niente orologio, niente I/O.
 */
public object DayClosing {
    /** Tutte e tre le cose del giorno sono fatte e la Home le sta mostrando tutte (nessuna voce finta). */
    public fun isComplete(signals: DayClosingSignals, visible: Set<HomeFeature>): Boolean =
        signals.puzzleSolvedToday && signals.dailyMatchPlayedToday && signals.missionsCompleted &&
            HomeFeature.DAILY_PUZZLE in visible && HomeFeature.DAILY_MATCH in visible && HomeFeature.MISSIONS in visible

    /**
     * `true` se oggi la chiusura va detta: il giorno è completo e non è già stata detta [today]
     * ([shownOn] è la data salvata dell'ultima volta, vuota se mai).
     */
    public fun isDue(signals: DayClosingSignals, visible: Set<HomeFeature>, shownOn: String, today: LocalDate): Boolean =
        isComplete(signals, visible) && shownOn != today.toString()
}
