package it.maestro.game.model

/**
 * Converte una notazione compatta di test a due caratteri (`<valore><seme>`, es. `"7D"`, `"FC"`)
 * nella corrispondente [Card] (task F0-03).
 *
 * Simboli di valore validi: `A`, `2`, `3`, `4`, `5`, `6`, `7`, `F`, `C`, `R`.
 * Simboli di seme validi: `D` ([Suit.DENARI]), `C` ([Suit.COPPE]), `S` ([Suit.SPADE]), `B` ([Suit.BASTONI]).
 *
 * @throws IllegalArgumentException se [notation] non rispetta il formato `<valore><seme>`.
 */
fun card(notation: String): Card {
    require(notation.length == 2) {
        "Notazione carta non valida '$notation': attesi esattamente 2 caratteri <valore><seme> " +
            "(valore in A,2..7,F,C,R; seme in D,C,S,B)."
    }
    val rank = when (val rankChar = notation[0]) {
        'A' -> Rank.ASSO
        '2' -> Rank.DUE
        '3' -> Rank.TRE
        '4' -> Rank.QUATTRO
        '5' -> Rank.CINQUE
        '6' -> Rank.SEI
        '7' -> Rank.SETTE
        'F' -> Rank.FANTE
        'C' -> Rank.CAVALLO
        'R' -> Rank.RE
        else -> throw IllegalArgumentException(
            "Notazione carta non valida '$notation': simbolo di valore '$rankChar' non riconosciuto " +
                "(ammessi: A, 2, 3, 4, 5, 6, 7, F, C, R).",
        )
    }
    val suit = when (val suitChar = notation[1]) {
        'D' -> Suit.DENARI
        'C' -> Suit.COPPE
        'S' -> Suit.SPADE
        'B' -> Suit.BASTONI
        else -> throw IllegalArgumentException(
            "Notazione carta non valida '$notation': simbolo di seme '$suitChar' non riconosciuto " +
                "(ammessi: D, C, S, B).",
        )
    }
    return Card(rank = rank, suit = suit)
}

/**
 * Converte una sequenza di notazioni compatte di test (es. `cards("AD", "3C")`) in una [List] di [Card].
 *
 * @throws IllegalArgumentException se almeno una notazione in [notations] non è valida.
 */
fun cards(vararg notations: String): List<Card> = notations.map(::card)
