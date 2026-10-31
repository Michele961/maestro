package it.maestro.app.shell

import kotlinx.serialization.Serializable

/**
 * Rotte di navigazione tipizzate (`docs/app/layers.md` §4, task F1R-04).
 *
 * Gli argomenti sono valori semplici che sopravvivono alla ricreazione dell'Activity e alla chiusura
 * del processo. Una partita è identificata dal suo `MatchLog` in forma testuale (ADR-0003: seed,
 * configurazione e mosse la definiscono per intero), mai da oggetti di stato.
 */
public sealed interface Route {
    @Serializable
    public data object Home : Route

    /**
     * La scala degli avversari: da qui si sceglie contro chi giocare. Con [objective] (`name` di
     * `TrainingFundamental`, dal «Allena questo» del Profilo) la preparazione parte già con quell'obiettivo.
     */
    @Serializable
    public data class Ladder(val objective: String? = null) : Route

    /** Preparazione della partita contro [opponent] (`name` dell'enum `Opponent`). */
    @Serializable
    public data class PreGame(val opponent: String, val objective: String? = null) : Route

    /**
     * Tavolo. Con [resume] si riprende la partita salvata invece di iniziarne una nuova.
     * Livello e fondamentale sono i `name` dei rispettivi enum.
     */
    @Serializable
    public data class Table(
        val level: String,
        val targetScore: Int,
        val fundamental: String,
        val seed: Long,
        val resume: Boolean = false,
        val reBello: Boolean = false,
        val redealOnThreeKings: Boolean = true,
        val napola: Boolean = false,
        val assoPigliatutto: Boolean = false,
        /** Mano d'esordio guidata (F1G-02): seme fisso, il Maestro guida e la partita non si registra. */
        val guided: Boolean = false,
    ) : Route

    /** Fine partita. [trainingCorrect]/[trainingTotal] sono -1 se non ci sono stati Check. */
    @Serializable
    public data class PostMatch(
        val matchLog: String,
        val level: String,
        val fundamental: String,
        val trainingCorrect: Int = -1,
        val trainingTotal: Int = -1,
        val fromHistory: Boolean = false,
    ) : Route

    @Serializable
    public data class Review(
        val matchLog: String,
        val level: String,
        val fromHistory: Boolean = false,
    ) : Route

    @Serializable
    public data object DailyPuzzle : Route

    @Serializable
    public data object Profile : Route

    @Serializable
    public data object Academy : Route

    @Serializable
    public data object Shop : Route

    /** La Scalata dei Circoli (task F1G-07). */
    @Serializable
    public data object Season : Route

    @Serializable
    public data object Today : Route

    @Serializable
    public data object Settings : Route
}
