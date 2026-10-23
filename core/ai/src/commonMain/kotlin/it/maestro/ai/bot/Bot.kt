package it.maestro.ai.bot

import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView

/**
 * Interfaccia comune per i bot che scelgono una mossa di Scopa (`docs/ai/maestro.md`, task F0-08).
 *
 * Per il principio di informazione lecita, il bot riceve **esclusivamente** la [ScopaView]
 * del giocatore di turno, la lista delle mosse legali [legal] e un generatore pseudocasuale
 * deterministico [rng], mai lo stato completo `ScopaState`.
 */
public interface Bot {
    /**
     * Sceglie e restituisce una mossa tra quelle presenti in [legal] per il giocatore a cui
     * appartiene [view], usando [rng] per eventuali scelte casuali o campionamenti.
     *
     * A parità di [view], [legal] e stato iniziale di [rng], la scelta è sempre deterministica.
     *
     * @param view Vista filtrata dello stato per il giocatore di turno.
     * @param legal Lista non vuota delle mosse legali disponibili nel turno corrente.
     * @param rng Generatore pseudocasuale deterministico con seed (ADR-0009).
     * @return Una mossa appartenente a [legal].
     * @throws IllegalArgumentException se [legal] è vuota.
     */
    public fun chooseMove(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng): ScopaMove
}
