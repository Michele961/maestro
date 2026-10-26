package it.maestro.ai.puzzle

import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView

/**
 * Obiettivo tattico che definisce la condizione di risoluzione di un [TacticalPuzzle]
 * (`docs/ai/maestro.md`, task F2-02).
 */
public enum class PuzzleGoal {
    /**
     * Realizza una scopa decisiva ripulendo il tavolo.
     */
    MAKE_SWEEP,

    /**
     * Vinci la partita raggiungendo o superando la soglia di punteggio (spec §7.2–7.3).
     */
    WIN_MATCH,

    /**
     * Massimizza il saldo punti della smazzata con un'unica mossa strettamente superiore alle alternative.
     */
    MAXIMIZE_POINTS,
}

/**
 * Modello dati puro e immutabile che rappresenta un puzzle tattico di Scopa (`docs/backlog/fase-2.md` F2-02).
 *
 * @property id Identificatore univoco del puzzle (es. derivato dal seed deterministico).
 * @property title Chiave simbolica del titolo per la localizzazione nella UI (ADR-0007).
 * @property initialView Vista parziale dello stato di smazzata presentata al giocatore ([ScopaView]).
 * @property goal Obiettivo tattico richiesto per risolvere il puzzle ([PuzzleGoal]).
 * @property solutionMoves Sequenza ordinata delle mosse vincenti del giocatore risolutore.
 */
public data class TacticalPuzzle(
    public val id: String,
    public val title: String,
    public val initialView: ScopaView,
    public val goal: PuzzleGoal,
    public val solutionMoves: List<ScopaMove>,
) {
    init {
        require(initialView.hand.size >= 2) {
            "Un puzzle tattico richiede almeno 2 carte in mano per offrire una scelta, trovate: ${initialView.hand.size}"
        }
        require(solutionMoves.isNotEmpty()) {
            "Un puzzle tattico deve definire almeno una mossa nella sequenza di soluzione"
        }
    }

    /**
     * Prima mossa vincente che il giocatore deve individuare per avviare la risoluzione del puzzle.
     */
    public val firstMove: ScopaMove
        get() = solutionMoves.first()
}
