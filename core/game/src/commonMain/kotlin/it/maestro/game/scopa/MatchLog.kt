package it.maestro.game.scopa

import it.maestro.game.CoreGame
import it.maestro.game.model.Card
import it.maestro.game.model.CardOrder
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit

private const val LOG_HEADER_LINE: String = "maestro-log 1"
private const val RULES_PREFIX: String = "rules "
private const val CONFIG_PREFIX: String = "config "
private const val SEED_PREFIX: String = "seed "
private const val MOVE_KEYWORD: String = "move"
private const val EMPTY_CAPTURE_TOKEN: String = "-"
private const val MIN_LOG_HEADER_LINES: Int = 4
private const val MOVE_LINE_TOKENS: Int = 3
private const val CONFIG_ENTRIES_COUNT: Int = 3
private const val KEY_VALUE_PARTS: Int = 2
private const val CARD_NOTATION_LENGTH: Int = 2

/**
 * Registrazione completa e deterministica di una partita di Scopa (`docs/architecture.md`, ADR-0003, task F0-07).
 *
 * Una partita è interamente definita da [rulesVersion], [config], [seed] e dalla sequenza ordinata di [moves].
 * Rigiocando un [MatchLog] tramite [replay] si ottiene sempre lo stesso [ScopaState] finale.
 *
 * Formato testuale (UTF-8, una voce per riga):
 * ```
 * maestro-log 1
 * rules 1
 * config targetScore=11 redealOnThreeKings=true reBello=false
 * seed 42
 * move 7D AD,6C
 * move 3S -
 * ```
 *
 * @property rulesVersion Versione della specifica delle regole con cui il log è stato registrato
 *   (default [CoreGame.RULES_VERSION]).
 * @property config Configurazione della partita (spec §8).
 * @property seed Seme iniziale del generatore pseudocasuale (ADR-0009).
 * @property moves Sequenza ordinata delle mosse giocate dall'inizio della partita.
 */
public data class MatchLog(
    public val rulesVersion: Int = CoreGame.RULES_VERSION,
    public val config: ScopaConfig,
    public val seed: Long,
    public val moves: List<ScopaMove>,
) {
    /**
     * Serializza questo [MatchLog] nel formato testuale canonico a righe (task F0-07).
     *
     * Le carte di ogni presa non vuota sono elencate separate da virgola in ordine canonico ([CardOrder]);
     * una giocata senza presa è rappresentata da `-`.
     */
    public fun toText(): String {
        val lines = ArrayList<String>(MIN_LOG_HEADER_LINES + moves.size)
        lines.add(LOG_HEADER_LINE)
        lines.add("$RULES_PREFIX$rulesVersion")
        lines.add(
            "${CONFIG_PREFIX}targetScore=${config.targetScore} " +
                "redealOnThreeKings=${config.redealOnThreeKings} " +
                "reBello=${config.reBello}",
        )
        lines.add("$SEED_PREFIX$seed")
        for (move in moves) {
            val captureText = if (move.capture.isEmpty()) {
                EMPTY_CAPTURE_TOKEN
            } else {
                move.capture
                    .sortedWith(CardOrder)
                    .joinToString(",") { it.toLogNotation() }
            }
            lines.add("$MOVE_KEYWORD ${move.card.toLogNotation()} $captureText")
        }
        return lines.joinToString("\n")
    }

    public companion object {
        /**
         * Deserializza un [MatchLog] dal formato testuale canonico prodotto da [toText] (task F0-07).
         *
         * Rifiuta con [IllegalArgumentException] qualsiasi riga sconosciuta, intestazione mancante o fuori ordine,
         * parametri di configurazione non riconosciuti o duplicati, e notazioni di carte non valide.
         *
         * @throws IllegalArgumentException se [text] non rispetta la grammatica del formato `maestro-log 1`.
         */
        public fun parse(text: String): MatchLog {
            require(text.isNotEmpty()) { "Il testo del log non può essere vuoto" }

            val normalized = text
                .replace("\r\n", "\n")
                .removeSuffix("\n")
            val lines = normalized.split('\n')
            require(lines.size >= MIN_LOG_HEADER_LINES) {
                "Log incompleto: attese almeno $MIN_LOG_HEADER_LINES righe di intestazione, trovate ${lines.size}"
            }

            require(lines[0] == LOG_HEADER_LINE) {
                "Intestazione log non valida alla riga 1: attesa '$LOG_HEADER_LINE', trovata '${lines[0]}'"
            }

            val rulesVersion = parseRulesLine(lines[1])
            val config = parseConfigLine(lines[2])
            val seed = parseSeedLine(lines[3])

            val moves = ArrayList<ScopaMove>(lines.size - MIN_LOG_HEADER_LINES)
            for (lineIndex in MIN_LOG_HEADER_LINES until lines.size) {
                moves.add(parseMoveLine(lines[lineIndex], lineNumber = lineIndex + 1))
            }

            return MatchLog(
                rulesVersion = rulesVersion,
                config = config,
                seed = seed,
                moves = moves,
            )
        }
    }
}

/**
 * Rigioca dall'inizio un [MatchLog] partendo da `newMatch(log.config, log.seed)` e applicando
 * in sequenza ogni mossa tramite [play] (`docs/architecture.md`, ADR-0003, task F0-07).
 *
 * - Se `log.rulesVersion != CoreGame.RULES_VERSION`, restituisce [ReplayResult.WrongRulesVersion].
 * - Se la mossa di indice `i` viene rifiutata da [play], interrompe il replay e restituisce
 *   `ReplayResult.IllegalMove(index = i)`.
 * - Se tutte le mosse sono legali, restituisce `ReplayResult.Ok(state)` con lo stato finale.
 */
public fun replay(log: MatchLog): ReplayResult {
    if (log.rulesVersion != CoreGame.RULES_VERSION) {
        return ReplayResult.WrongRulesVersion(
            found = log.rulesVersion,
            expected = CoreGame.RULES_VERSION,
        )
    }

    var state = newMatch(config = log.config, seed = log.seed)
    for ((index, move) in log.moves.withIndex()) {
        when (val result = play(state, move)) {
            is PlayResult.Applied -> state = result.state
            is PlayResult.Rejected -> return ReplayResult.IllegalMove(index = index)
        }
    }
    return ReplayResult.Ok(state = state)
}

private fun parseRulesLine(line: String): Int {
    require(line.startsWith(RULES_PREFIX)) {
        "Riga 2 non valida: attesa '$RULES_PREFIX<Int>', trovata '$line'"
    }
    val rawVersion = line.removePrefix(RULES_PREFIX)
    return rawVersion.toIntOrNull()
        ?: throw IllegalArgumentException("Versione regole non valida alla riga 2: '$rawVersion'")
}

private fun parseConfigLine(line: String): ScopaConfig {
    require(line.startsWith(CONFIG_PREFIX)) {
        "Riga 3 non valida: attesa '$CONFIG_PREFIX...', trovata '$line'"
    }
    val tokens = line.removePrefix(CONFIG_PREFIX).split(' ')
    require(tokens.size == CONFIG_ENTRIES_COUNT) {
        "Riga config non valida: attesi $CONFIG_ENTRIES_COUNT parametri chiave=valore, trovati '${line.removePrefix(CONFIG_PREFIX)}'"
    }

    val entries = LinkedHashMap<String, String>(CONFIG_ENTRIES_COUNT)
    for (token in tokens) {
        val parts = token.split('=')
        require(parts.size == KEY_VALUE_PARTS && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
            "Parametro config malformato '$token' nella riga '$line'"
        }
        val previous = entries.put(parts[0], parts[1])
        require(previous == null) {
            "Parametro config duplicato '${parts[0]}' nella riga '$line'"
        }
    }

    val expectedKeys = setOf("targetScore", "redealOnThreeKings", "reBello")
    require(entries.keys == expectedKeys) {
        "Parametri config non riconosciuti o mancanti: attesi $expectedKeys, trovati ${entries.keys}"
    }

    val targetScore = entries.getValue("targetScore").toIntOrNull()
        ?: throw IllegalArgumentException("Valore targetScore non intero: '${entries["targetScore"]}'")
    val redealOnThreeKings = entries.getValue("redealOnThreeKings").toBooleanStrictOrNull()
        ?: throw IllegalArgumentException(
            "Valore redealOnThreeKings non booleano: '${entries["redealOnThreeKings"]}'",
        )
    val reBello = entries.getValue("reBello").toBooleanStrictOrNull()
        ?: throw IllegalArgumentException("Valore reBello non booleano: '${entries["reBello"]}'")

    return ScopaConfig(
        targetScore = targetScore,
        redealOnThreeKings = redealOnThreeKings,
        reBello = reBello,
    )
}

private fun parseSeedLine(line: String): Long {
    require(line.startsWith(SEED_PREFIX)) {
        "Riga 4 non valida: attesa '$SEED_PREFIX<Long>', trovata '$line'"
    }
    val rawSeed = line.removePrefix(SEED_PREFIX)
    return rawSeed.toLongOrNull()
        ?: throw IllegalArgumentException("Valore seed non valido alla riga 4: '$rawSeed'")
}

private fun parseMoveLine(line: String, lineNumber: Int): ScopaMove {
    val tokens = line.split(' ')
    require(tokens.size == MOVE_LINE_TOKENS && tokens[0] == MOVE_KEYWORD) {
        "Riga $lineNumber non riconosciuta o malformata: attesa 'move <carta> <presa|->', trovata '$line'"
    }

    val playedCard = parseCardNotation(tokens[1], lineNumber)
    val captureToken = tokens[2]
    val captureSet: Set<Card> = if (captureToken == EMPTY_CAPTURE_TOKEN) {
        emptySet()
    } else {
        val rawCards = captureToken.split(',')
        require(rawCards.isNotEmpty() && rawCards.all { it.isNotEmpty() }) {
            "Lista di presa non valida '$captureToken' alla riga $lineNumber"
        }
        val parsedCards = rawCards.map { parseCardNotation(it, lineNumber) }
        val canonicalSet = parsedCards.sortedWith(CardOrder).toSet()
        require(canonicalSet.size == parsedCards.size) {
            "Carte duplicate nella presa '$captureToken' alla riga $lineNumber"
        }
        canonicalSet
    }

    return ScopaMove(card = playedCard, capture = captureSet)
}

internal fun Card.toLogNotation(): String {
    val rankSymbol = when (rank) {
        Rank.ASSO -> 'A'
        Rank.DUE -> '2'
        Rank.TRE -> '3'
        Rank.QUATTRO -> '4'
        Rank.CINQUE -> '5'
        Rank.SEI -> '6'
        Rank.SETTE -> '7'
        Rank.FANTE -> 'F'
        Rank.CAVALLO -> 'C'
        Rank.RE -> 'R'
    }
    val suitSymbol = when (suit) {
        Suit.DENARI -> 'D'
        Suit.COPPE -> 'C'
        Suit.SPADE -> 'S'
        Suit.BASTONI -> 'B'
    }
    return "$rankSymbol$suitSymbol"
}

internal fun parseCardNotation(notation: String, lineNumber: Int? = null): Card {
    val locationSuffix = if (lineNumber != null) " (riga $lineNumber)" else ""
    require(notation.length == CARD_NOTATION_LENGTH) {
        "Notazione carta non valida '$notation'$locationSuffix: attesi $CARD_NOTATION_LENGTH caratteri <valore><seme>"
    }
    val rank = when (val r = notation[0]) {
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
            "Notazione carta non valida '$notation'$locationSuffix: simbolo di valore '$r' non riconosciuto",
        )
    }
    val suit = when (val s = notation[1]) {
        'D' -> Suit.DENARI
        'C' -> Suit.COPPE
        'S' -> Suit.SPADE
        'B' -> Suit.BASTONI
        else -> throw IllegalArgumentException(
            "Notazione carta non valida '$notation'$locationSuffix: simbolo di seme '$s' non riconosciuto",
        )
    }
    return Card(rank = rank, suit = suit)
}
