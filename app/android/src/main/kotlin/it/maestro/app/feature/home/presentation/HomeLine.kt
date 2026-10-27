package it.maestro.app.feature.home.presentation

/** Di cosa parla il Maestro dalla Home (`docs/app/features/home.md` §3.5). */
public enum class HomeLineKind { RESUME, STREAK, WELCOME_NAMED, WELCOME }

/** La battuta scelta: [kind] e quale delle [HomeLines.VARIANTS] varianti. */
public data class HomeLine(val kind: HomeLineKind, val variant: Int)

/**
 * Sceglie la battuta del Maestro in Home dallo stato del giocatore: nessuna casualità, cambia col [day] così non
 * ripete la stessa frase ogni giorno (ADR-0009).
 */
public object HomeLines {
    /** Varianti per ogni tipo di battuta. */
    public const val VARIANTS: Int = 3

    /** Serie minima di giorni che il Maestro nomina. */
    public const val MIN_STREAK: Int = 2

    public fun choose(hasActiveMatch: Boolean, streakCount: Int, hasName: Boolean, day: Long): HomeLine {
        val kind = when {
            hasActiveMatch -> HomeLineKind.RESUME
            streakCount >= MIN_STREAK -> HomeLineKind.STREAK
            hasName -> HomeLineKind.WELCOME_NAMED
            else -> HomeLineKind.WELCOME
        }
        return HomeLine(kind, Math.floorMod(day, VARIANTS.toLong()).toInt())
    }
}
