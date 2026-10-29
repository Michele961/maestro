package it.maestro.app.domain.time

import java.time.LocalDate

/**
 * Unica fonte di tempo e di seed nuovi dell'app (`docs/app/layers.md` §3, task F1R-02).
 *
 * Nessun altro punto dell'app legge l'orologio di sistema: così le regole legate al giorno
 * (streak, puzzle, missioni) si testano con un orologio finto.
 */
public interface Clock {
    /** Istante corrente in millisecondi dall'epoca Unix. */
    public fun nowMillis(): Long

    /** Data corrente nel fuso orario dell'utente. */
    public fun today(): LocalDate

    /** Ora locale del giorno, 0–23 (per la musica del circolo, task F1R-20b). */
    public fun localHour(): Int

    /** Seed per una nuova partita (ADR-0003): la partita resta riproducibile dal suo log. */
    public fun newSeed(): Long
}
