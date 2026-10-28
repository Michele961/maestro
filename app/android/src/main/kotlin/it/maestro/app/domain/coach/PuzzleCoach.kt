package it.maestro.app.domain.coach

import it.maestro.game.model.Card
import it.maestro.game.scopa.ScopaMove

/**
 * Il Maestro nella Sfida del Giorno (task F1R-27b): indizi in due livelli che nominano le carte della posizione e la
 * soluzione spiegata. Frasi in codici e argomenti, come [HintComposer]; i testi stanno in `strings.xml` (ADR-0007).
 */
public object PuzzleCoach {

    /**
     * Indizio di [level] per la mossa giusta [move] sul banco [table]. Livello 1: nomina la carta del banco che conta
     * (o avverte che non si prende); dal livello 2 una domanda sulla ragione della mossa, senza nominarla.
     */
    public fun hint(move: ScopaMove, table: List<Card>, level: Int): HintLine {
        if (level >= 2) return HintComposer.nudge(move, table)
        val target = move.capture.minOrNull()
        return if (target != null) {
            HintLine(listOf(Phrase("puzzle_hint_card", listOf(VoiceArg.CardArg(target)))), Pose.ENCOURAGING)
        } else {
            HintLine(listOf(Phrase("puzzle_hint_no_capture")), Pose.THINKING)
        }
    }

    /** La soluzione: la carta da giocare, cosa prende e perché, con le carte nominate. */
    public fun solution(move: ScopaMove, table: List<Card>): HintLine = HintComposer.compose(move, table)
}
