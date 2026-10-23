package it.maestro.ai.bot

import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView

/**
 * Valutatore delle mosse legali per il giocatore di turno (`docs/ai/maestro.md`, task F0-10).
 *
 * Per il principio di informazione lecita, il valutatore riceve esclusivamente la [ScopaView]
 * del giocatore di turno, mai lo stato completo `ScopaState`. Lo stesso valutatore è condiviso
 * tra [PimcBot] e l'analizzatore delle mosse (task F0-11).
 */
public interface MoveEvaluator {
    /**
     * Calcola il valore atteso di ogni mossa legale in [legal] per il giocatore di turno,
     * nella sua vista [view], utilizzando [rng] per il campionamento e rispettando [budget].
     *
     * @param view Vista filtrata dello stato per il giocatore di turno (`view.seat == view.toMove`).
     * @param legal Lista non vuota delle mosse legali da valutare.
     * @param rng Generatore pseudocasuale deterministico con seed (ADR-0009).
     * @param budget Budget di ricerca in campioni e nodi (`SearchBudget`).
     * @return Lista di [MoveValue] nello stesso ordine di [legal].
     * @throws IllegalArgumentException se [legal] è vuota o se [view] non appartiene al giocatore di turno.
     */
    public fun evaluate(
        view: ScopaView,
        legal: List<ScopaMove>,
        rng: SeededRng,
        budget: SearchBudget = SearchBudget(),
    ): List<MoveValue>
}
