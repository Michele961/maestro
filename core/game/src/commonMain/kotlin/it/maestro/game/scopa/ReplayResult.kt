package it.maestro.game.scopa

/**
 * Risultato tipizzato del replay di un [MatchLog] tramite [replay] (`docs/architecture.md`, ADR-0003, task F0-07).
 */
public sealed interface ReplayResult {
    /**
     * Il log è valido e tutte le mosse sono state applicate, producendo lo stato finale [state].
     *
     * @property state Stato risultante al termine dell'ultima mossa del log.
     */
    public data class Ok(
        public val state: ScopaState,
    ) : ReplayResult

    /**
     * La versione delle regole dichiarata nel log ([found]) non corrisponde a quella supportata
     * dal motore ([expected], pari a [it.maestro.game.CoreGame.RULES_VERSION]).
     *
     * @property found Versione delle regole trovata nel log.
     * @property expected Versione delle regole attesa dal motore.
     */
    public data class WrongRulesVersion(
        public val found: Int,
        public val expected: Int,
    ) : ReplayResult

    /**
     * La mossa in posizione [index] (base `0`) in [MatchLog.moves] è stata rifiutata da [play].
     *
     * @property index Indice della prima mossa illegale incontrata durante il replay.
     */
    public data class IllegalMove(
        public val index: Int,
    ) : ReplayResult
}
