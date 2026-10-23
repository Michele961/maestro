package it.maestro.ai.analysis

import it.maestro.ai.bot.MoveValue
import it.maestro.game.model.Seat
import it.maestro.game.scopa.ScopaMove

/**
 * Risultato dell'analisi di una singola mossa di un [it.maestro.game.scopa.MatchLog]
 * (`docs/ai/maestro.md` §Analizzatore, task F0-11).
 *
 * Tutte le mosse legali disponibili al turno vengono valutate dal punto di vista del giocatore [seat]
 * con la sola informazione lecita (`viewFor(state, seat)`).
 *
 * @property moveIndex Indice a base zero della mossa nel `MatchLog` (`>= 0`).
 * @property seat Giocatore che ha effettuato la mossa.
 * @property playedMove Mossa effettivamente giocata nel log (carta e presa).
 * @property playedValue Valore atteso stimato (o esatto nell'ultima distribuzione) della mossa giocata.
 * @property bestMove Mossa legale con valore atteso massimo (a parità di valore, la prima in ordine canonico).
 * @property bestValue Valore atteso massimo tra tutte le mosse legali disponibili al turno.
 * @property loss Perdita di valore atteso della mossa giocata rispetto a [bestMove] (`>= 0.0`).
 * @property grade Classificazione della mossa giocata ([MoveGrade.BEST], [MoveGrade.INACCURACY], [MoveGrade.BLUNDER]).
 * @property alternatives Tutte le mosse legali valutate, ordinate per valore atteso decrescente e, a parità,
 *   nell'ordine canonico di `legalMoves`.
 */
public data class MoveAnalysis(
    public val moveIndex: Int,
    public val seat: Seat,
    public val playedMove: ScopaMove,
    public val playedValue: Double,
    public val bestMove: ScopaMove,
    public val bestValue: Double,
    public val loss: Double,
    public val grade: MoveGrade,
    public val alternatives: List<MoveValue>,
) {
    init {
        require(moveIndex >= 0) { "L'indice della mossa deve essere >= 0, trovato: $moveIndex" }
        require(loss >= 0.0) { "La perdita deve essere >= 0.0, trovata: $loss" }
        require(alternatives.isNotEmpty()) { "La lista delle alternative valutate non può essere vuota" }
    }

    /** Numero della mossa a base 1 (`moveIndex + 1`). */
    public val moveNumber: Int
        get() = moveIndex + 1

    /** Le migliori alternative legali al turno (fino a [MatchAnalyzer.TOP_ALTERNATIVES_COUNT], cioè 3). */
    public val topAlternatives: List<MoveValue>
        get() = alternatives.take(MatchAnalyzer.TOP_ALTERNATIVES_COUNT)
}
