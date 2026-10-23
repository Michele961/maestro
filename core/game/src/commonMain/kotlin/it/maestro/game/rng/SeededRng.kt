package it.maestro.game.rng

/**
 * Generatore pseudocasuale deterministico basato sull'algoritmo SplitMix64 (ADR-0009).
 *
 * Costituisce l'unica fonte di casualità del motore di gioco e dei bot, garantendo
 * replay identici tra versioni di Kotlin e piattaforme diverse.
 *
 * Lo stato interno a 64 bit è leggibile tramite [state] e può essere ripristinato
 * passando il valore salvato al costruttore `SeededRng(state)` oppure a [fromState].
 *
 * @param seed seme iniziale (o stato interno da ripristinare) a 64 bit.
 */
public class SeededRng(seed: Long) {

    /**
     * Stato corrente a 64 bit del generatore.
     *
     * Può essere salvato (ad esempio dentro `ScopaState`) e usato per ricostruire
     * un generatore nello stesso punto esatto della sequenza tramite `SeededRng(state)`
     * o [fromState].
     */
    public var state: Long = seed
        private set

    /**
     * Genera il prossimo valore a 64 bit secondo l'algoritmo SplitMix64 di riferimento (ADR-0009).
     */
    public fun nextLong(): Long {
        state += GOLDEN_GAMMA
        var z = state
        z = (z xor (z ushr 30)) * MIX_1
        z = (z xor (z ushr 27)) * MIX_2
        return z xor (z ushr 31)
    }

    /**
     * Genera un intero uniforme nell'intervallo `[0, bound)` tramite rejection sampling (ADR-0009).
     *
     * @param bound limite superiore esclusivo; deve essere strettamente positivo.
     * @throws IllegalArgumentException se `bound <= 0`.
     */
    public fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be > 0, was $bound" }
        val boundU = bound.toULong()
        val limit = TWO_POW_63 - (TWO_POW_63 % boundU)
        while (true) {
            val x = (nextLong() ushr 1).toULong()
            if (x < limit) {
                return (x % boundU).toInt()
            }
        }
    }

    /**
     * Deriva un nuovo generatore indipendente consumando un [nextLong] da questo generatore (ADR-0009).
     */
    public fun fork(): SeededRng = SeededRng(nextLong())

    /**
     * Restituisce una nuova lista con gli elementi di [list] mescolati tramite Fisher–Yates (ADR-0009, spec §3.1).
     *
     * L'algoritmo scorre l'indice `i` da `n - 1` a `1`, estrae `j = nextInt(i + 1)` e scambia
     * gli elementi in posizione `i` e `j`. La lista passata in ingresso non viene modificata.
     */
    public fun <T> shuffle(list: List<T>): List<T> {
        val result = list.toMutableList()
        for (i in result.size - 1 downTo 1) {
            val j = nextInt(i + 1)
            val tmp = result[i]
            result[i] = result[j]
            result[j] = tmp
        }
        return result.toList()
    }

    public companion object {
        private const val GOLDEN_GAMMA: Long = -0x61c8864680b583ebL // 0x9E3779B97F4A7C15
        private const val MIX_1: Long = -0x40a7b892e31b1a47L // 0xBF58476D1CE4E5B9
        private const val MIX_2: Long = -0x6b2fb644ecceee15L // 0x94D049BB133111EB
        private const val TWO_POW_63: ULong = 1uL shl 63

        /**
         * Ricostruisce un [SeededRng] a partire da uno [state] precedentemente esportato.
         */
        public fun fromState(state: Long): SeededRng = SeededRng(state)
    }
}
