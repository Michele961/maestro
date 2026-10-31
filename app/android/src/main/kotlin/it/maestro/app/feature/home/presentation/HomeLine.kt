package it.maestro.app.feature.home.presentation

import it.maestro.app.domain.home.HomeFeature
import it.maestro.app.domain.home.HomeGuide

/** Di cosa parla il Maestro dalla Home (`docs/app/features/home.md` §3.5). */
public enum class HomeLineKind { UNLOCK, RESUME, GUIDE, STREAK, WELCOME_NAMED, WELCOME }

/**
 * La battuta scelta: [kind] e quale delle [HomeLines.VARIANTS] varianti. Per [HomeLineKind.UNLOCK] c'è la frase di
 * presentazione di [feature] (una sola, senza varianti); per [HomeLineKind.GUIDE] il suggerimento [guide].
 */
public data class HomeLine(
    val kind: HomeLineKind,
    val variant: Int,
    val feature: HomeFeature? = null,
    val guide: HomeGuide? = null,
)

/**
 * Sceglie la battuta del Maestro in Home dallo stato del giocatore: nessuna casualità, cambia col [day] così non
 * ripete la stessa frase ogni giorno (ADR-0009).
 */
public object HomeLines {
    /** Varianti per ogni tipo di battuta. */
    public const val VARIANTS: Int = 3

    /** Serie minima di giorni che il Maestro nomina. */
    public const val MIN_STREAK: Int = 2

    /** [introducing]: la funzione appena comparsa da presentare (F1G-03); ha la precedenza su tutto, una volta sola. */
    public fun choose(
        hasActiveMatch: Boolean,
        streakCount: Int,
        hasName: Boolean,
        day: Long,
        introducing: HomeFeature? = null,
        /** Il suggerimento «cosa premere» (F1G-16a): dopo la partita da riprendere, prima di serie e saluti. */
        guide: HomeGuide? = null,
    ): HomeLine {
        if (introducing != null) return HomeLine(HomeLineKind.UNLOCK, variant = 0, feature = introducing)
        if (guide != null && !hasActiveMatch) return HomeLine(HomeLineKind.GUIDE, variant = 0, guide = guide)
        val kind = when {
            hasActiveMatch -> HomeLineKind.RESUME
            streakCount >= MIN_STREAK -> HomeLineKind.STREAK
            hasName -> HomeLineKind.WELCOME_NAMED
            else -> HomeLineKind.WELCOME
        }
        return HomeLine(kind, Math.floorMod(day, VARIANTS.toLong()).toInt())
    }
}
