package it.maestro.ai.bot

/**
 * Budget di ricerca per la valutazione PIMC delle mosse (`docs/ai/maestro.md`, task F0-10).
 *
 * Il budget è espresso in numero di distribuzioni campionate ([samples]) e in numero massimo di nodi
 * dell'albero di ricerca esplorati per ciascun campione ([maxNodes]), mai in millisecondi di tempo
 * reale, così da garantire il pieno determinismo a parità di input e seed (`docs/ai/maestro.md` §Principi).
 *
 * @property samples Numero di distribuzioni compatibili da campionare quando il mazzo non è vuoto (`> 0`).
 * @property maxNodes Numero massimo di nodi esplorabili dalla ricerca minimax alfa-beta per campione (`> 0`).
 * @throws IllegalArgumentException se [samples] `<= 0` o [maxNodes] `<= 0`.
 */
public data class SearchBudget(
    public val samples: Int = DEFAULT_SAMPLES,
    public val maxNodes: Int = DEFAULT_MAX_NODES,
) {
    init {
        require(samples > 0) { "Il numero di campioni (samples) deve essere > 0, trovato: $samples" }
        require(maxNodes > 0) { "Il limite di nodi (maxNodes) deve essere > 0, trovato: $maxNodes" }
    }

    public companion object {
        /** Numero predefinito di distribuzioni campionate per mossa. */
        public const val DEFAULT_SAMPLES: Int = 20

        /** Limite predefinito di nodi esplorati per campione nella ricerca a informazione perfetta. */
        public const val DEFAULT_MAX_NODES: Int = 2_000
    }
}
