package it.maestro.ai.mastery

/**
 * Modello a 5 assi delle competenze strategiche del giocatore per il Radar delle Abilità
 * (ADR-0005, task F2-04, `docs/vision.md` §5.A).
 *
 * Ciascun valore è compreso nell'intervallo percentuale `0..100`.
 *
 * @property sevens Memoria e valorizzazione dei Sette (e conteggio carte uscite).
 * @property denari Controllo dell'oro (Denari) e protezione/cattura del Settebello.
 * @property primiera Stima e massimizzazione del punteggio di Primiera (Sette, Sei, Assi).
 * @property board Gestione del banco (calate sicure ed evitare regali di scope all'avversario).
 * @property endgame Efficacia tattica nel finale di smazzata (ultime 3 mani a mazzo vuoto).
 */
public data class AbilityRadar(
    public val sevens: Int,
    public val denari: Int,
    public val primiera: Int,
    public val board: Int,
    public val endgame: Int,
) {
    init {
        require(sevens in 0..100) { "sevens deve essere in 0..100: $sevens" }
        require(denari in 0..100) { "denari deve essere in 0..100: $denari" }
        require(primiera in 0..100) { "primiera deve essere in 0..100: $primiera" }
        require(board in 0..100) { "board deve essere in 0..100: $board" }
        require(endgame in 0..100) { "endgame deve essere in 0..100: $endgame" }
    }

    public companion object {
        /** Valori di default di partenza per un nuovo giocatore (baseline neutrale al 50%). */
        public val DEFAULT: AbilityRadar = AbilityRadar(
            sevens = 50,
            denari = 50,
            primiera = 50,
            board = 50,
            endgame = 50,
        )
    }
}
