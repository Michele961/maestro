package it.maestro.app.ui.screens

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScopaView

/**
 * Spiegazione pedagogica strutturata per la Game Review del Maestro.
 *
 * @property title Titolo sintetico del principio tattico (es. "Gestione dei Semi e Primiera").
 * @property explanation Spiegazione discorsiva in italiano chiaro e naturale sul perché la mossa consigliata è superiore.
 * @property keyConcept Regola aurea del circolo da ricordare.
 */
public data class ReviewExplanation(
    val title: String,
    val explanation: String,
    val keyConcept: String,
)

/**
 * Motore di analisi didattica per la Game Review.
 *
 * Analizza le differenze tattiche e strategiche tra la mossa effettivamente giocata dall'utente
 * e la mossa migliore raccomandata dal motore di Scopa, spiegando all'utente il *perché* logico
 * della correzione (Primiera, Denari, copertura dei semi, prevenzione scope, gestione della mano).
 */
public object ReviewExplanationEngine {

    /**
     * Genera la spiegazione didattica del Maestro confrontando [playedMove] e [bestMove].
     */
    public fun explain(
        playedMove: ScopaMove,
        bestMove: ScopaMove,
        playerView: ScopaView,
    ): ReviewExplanation {
        val playedCard = playedMove.card
        val bestCard = bestMove.card
        val table = playerView.table

        val settebello = Card(Rank.SETTE, Suit.DENARI)

        // 1. Il Settebello è coinvolto
        val bestTakesSettebello = bestMove.capture.contains(settebello) || (bestCard == settebello && bestMove.capture.isNotEmpty())
        val playedTakesSettebello = playedMove.capture.contains(settebello) || (playedCard == settebello && playedMove.capture.isNotEmpty())
        if (bestTakesSettebello && !playedTakesSettebello) {
            return ReviewExplanation(
                title = "Il Settebello",
                explanation = "La mossa migliore cattura il 7 di Denari: la carta più preziosa del gioco, che garantisce il punto secco del Settebello, oro per i Denari e 21 per la Primiera.",
                keyConcept = "Non lasciare mai il Settebello a terra quando puoi catturarlo.",
            )
        }

        // 2. Realizzazione di una Scopa
        val bestMakesSweep = table.isNotEmpty() && bestMove.capture.containsAll(table) && table.containsAll(bestMove.capture)
        val playedMakesSweep = table.isNotEmpty() && playedMove.capture.containsAll(table) && table.containsAll(playedMove.capture)
        if (bestMakesSweep && !playedMakesSweep) {
            return ReviewExplanation(
                title = "Occasione di Scopa",
                explanation = "La mossa consigliata ripulisce completamente il tavolo mettendo a segno una Scopa (+1 punto) e lasciando il banco vuoto all'avversario.",
                keyConcept = "Cogliere sempre l'opportunità di fare Scopa per guadagnare punti ed azzerare il gioco avversario.",
            )
        }

        // 3. Rischio di Scopa concessa all'avversario
        val playedRemainingTable = if (playedMove.capture.isEmpty()) table + playedCard else table.filterNot { it in playedMove.capture }
        val playedRemainingSum = playedRemainingTable.sumOf { it.rank.value }
        val bestRemainingTable = if (bestMove.capture.isEmpty()) table + bestCard else table.filterNot { it in bestMove.capture }
        val bestRemainingSum = bestRemainingTable.sumOf { it.rank.value }

        val playedLeavesSweepDanger = playedRemainingTable.isNotEmpty() && playedRemainingSum in 1..10
        val bestLeavesSweepDanger = bestRemainingTable.isNotEmpty() && bestRemainingSum in 1..10

        if (playedLeavesSweepDanger && !bestLeavesSweepDanger) {
            return ReviewExplanation(
                title = "Rischio di Scopa Avversaria",
                explanation = "Con la tua giocata, le carte rimaste a terra sommano esattamente a $playedRemainingSum: l'avversario può fare Scopa immediata con una sola carta da $playedRemainingSum.",
                keyConcept = "Cerca sempre di non lasciare a terra combinazioni con somma da 1 a 10.",
            )
        }

        // 4. Differenza nei Denari catturati
        val bestDenari = bestMove.capture.count { it.suit == Suit.DENARI } + if (bestCard.suit == Suit.DENARI && bestMove.capture.isNotEmpty()) 1 else 0
        val playedDenari = playedMove.capture.count { it.suit == Suit.DENARI } + if (playedCard.suit == Suit.DENARI && playedMove.capture.isNotEmpty()) 1 else 0
        if (bestDenari > playedDenari) {
            return ReviewExplanation(
                title = "Corsa all'Oro (Denari)",
                explanation = "La mossa migliore assicura $bestDenari carte di Denari rispetto alle $playedDenari della tua scelta, avvicinandoti alla soglia di 6 Denari necessaria per il punto.",
                keyConcept = "I Denari decidono spesso le partite equilibrate: massimizzane la cattura.",
            )
        }

        // 5. Primiera: Presa di 7 o 6
        val bestSevensOrSixes = bestMove.capture.count { it.rank == Rank.SETTE || it.rank == Rank.SEI }
        val playedSevensOrSixes = playedMove.capture.count { it.rank == Rank.SETTE || it.rank == Rank.SEI }
        if (bestSevensOrSixes > playedSevensOrSixes) {
            return ReviewExplanation(
                title = "Valore di Primiera",
                explanation = "In Primiera il 7 vale 21 punti e il 6 vale 18, mentre le Figure valgono solo 10. Dare la precedenza a 7 e 6 è fondamentale per vincere il punto di Primiera.",
                keyConcept = "In Primiera la scala è capovolta: i 7 e i 6 valgono più di Re e Cavalli.",
            )
        }

        // 6. Stessa presa, ma carta da mano differente (es. Fante di Coppe vs Fante di Spade)
        if (playedMove.capture.isNotEmpty() && bestMove.capture.isNotEmpty() && playedCard != bestCard) {
            val myCaptures = playerView.capturedBy[playerView.toMove].orEmpty()
            val playedSuitCount = myCaptures.count { it.suit == playedCard.suit }
            val bestSuitCount = myCaptures.count { it.suit == bestCard.suit }

            val suitExplanation = if (playedSuitCount < bestSuitCount) {
                "Conservare in mano ${formatCardName(playedCard)} ti permette di difendere il seme di ${formatSuitName(playedCard.suit)} nella Primiera, dove hai meno carte."
            } else {
                "Giocare ${formatCardName(bestCard)} permette di portare punti nel seme di ${formatSuitName(bestCard.suit)} senza sguarnire la difesa delle carte che tieni in mano."
            }

            return ReviewExplanation(
                title = "Gestione dei Semi e Primiera",
                explanation = "Entrambe le carte effettuavano la stessa presa, ma $suitExplanation Questo mantiene bilanciata la tua Primiera su tutti e quattro i semi.",
                keyConcept = "A parità di presa, sacrifica la carta del seme in cui sei già coperto in Primiera.",
            )
        }

        // 7. Presa vs Calata
        if (playedMove.capture.isEmpty() && bestMove.capture.isNotEmpty()) {
            return ReviewExplanation(
                title = "Presa Ignorata",
                explanation = "Sul tavolo c'era una presa disponibile con ${formatCardName(bestCard)}. Calare a terra senza prendere regala l'iniziativa e le carte all'avversario.",
                keyConcept = "Se puoi prendere carte utili, non calare a terra senza motivo.",
            )
        }

        // 8. Calata difensiva: ha calato carta di valore
        if (playedMove.capture.isEmpty() && bestMove.capture.isEmpty()) {
            if (playedCard.rank == Rank.SETTE || playedCard.rank == Rank.SEI || playedCard.suit == Suit.DENARI) {
                return ReviewExplanation(
                    title = "Carta di Valore Regalata",
                    explanation = "Hai calato a terra ${formatCardName(playedCard)}, una carta preziosa per Denari o Primiera che l'avversario può catturare facilmente. Meglio calare ${formatCardName(bestCard)}.",
                    keyConcept = "Quando non hai prese, scarta sempre carte basse di semi non contesi.",
                )
            }

            return ReviewExplanation(
                title = "Controllo della Calata",
                explanation = "Calare ${formatCardName(bestCard)} rende più difficile per l'avversario combinare prese o realizzare Scopa rispetto a ${formatCardName(playedCard)}.",
                keyConcept = "Calare carte alte o superare quota 10 protegge il banco dalle scope avversarie.",
            )
        }

        // 9. Numero di carte catturate
        val bestCount = bestMove.capture.size + 1
        val playedCount = playedMove.capture.size + 1
        if (bestCount > playedCount) {
            return ReviewExplanation(
                title = "Punto delle Carte",
                explanation = "La mossa consigliata incassa $bestCount carte contro le $playedCount della tua giocata, aumentando le tue probabilità di superare la soglia di 21 carte.",
                keyConcept = "La quantità di carte è uno dei 4 punti fissi: prendi di più quando possibile.",
            )
        }

        // Fallback generale se non rientra nelle categorie specifiche
        return ReviewExplanation(
            title = "Valutazione Posizionale",
            explanation = "La mossa con ${formatCardName(bestCard)} offre un valore atteso superiore (+${"%.2f".format(bestMove.card.rank.value.toDouble())} pt stimati), mantenendo una migliore struttura difensiva per le mani successive.",
            keyConcept = "Ogni carta giocata influenza le combinazioni future: valuta sempre la mano nel suo complesso.",
        )
    }

    private fun formatCardName(card: Card): String {
        val rankStr = when (card.rank) {
            Rank.ASSO -> "l'Asso"
            Rank.DUE -> "il 2"
            Rank.TRE -> "il 3"
            Rank.QUATTRO -> "il 4"
            Rank.CINQUE -> "il 5"
            Rank.SEI -> "il 6"
            Rank.SETTE -> "il 7"
            Rank.FANTE -> "il Fante"
            Rank.CAVALLO -> "il Cavallo"
            Rank.RE -> "il Re"
        }
        val suitStr = when (card.suit) {
            Suit.DENARI -> "di Denari"
            Suit.COPPE -> "di Coppe"
            Suit.SPADE -> "di Spade"
            Suit.BASTONI -> "di Bastoni"
        }
        return "$rankStr $suitStr"
    }

    private fun formatSuitName(suit: Suit): String = when (suit) {
        Suit.DENARI -> "Denari"
        Suit.COPPE -> "Coppe"
        Suit.SPADE -> "Spade"
        Suit.BASTONI -> "Bastoni"
    }
}
