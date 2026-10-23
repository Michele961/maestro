package it.maestro.sim

import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.bot.Bot
import it.maestro.ai.bot.GreedyBot
import it.maestro.ai.bot.PimcBot
import it.maestro.ai.bot.PimcEvaluator
import it.maestro.ai.bot.RandomBot
import it.maestro.ai.bot.SearchBudget
import it.maestro.game.CoreGame
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.GameEvent
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.ScoreCategory
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import it.maestro.game.scopa.viewFor
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import kotlin.math.ceil
import kotlin.system.exitProcess

private const val DEFAULT_P1_BOT: String = "greedy"
private const val DEFAULT_P2_BOT: String = "random"
private const val NANOS_PER_MS: Double = 1_000_000.0
private const val PERCENT_MULTIPLIER: Double = 100.0
private const val P95_QUANTILE: Double = 0.95

/**
 * Configurazione degli argomenti da riga di comando per la modalità simulazione (task F0-09, F0-10).
 *
 * @property games Numero di partite da simulare (`> 0`).
 * @property seed Seme del generatore principale (`master = SeededRng(seed)`).
 * @property p1Name Nome del bot associato a `--p1` (`random`, `greedy` o `pimc`).
 * @property p2Name Nome del bot associato a `--p2` (`random`, `greedy` o `pimc`).
 * @property samples Numero di campioni per `PimcBot` (`--samples K`, default [SearchBudget.DEFAULT_SAMPLES]).
 * @property outFile Percorso opzionale del file JSON di output (`--out`).
 * @property saveLogsDir Percorso opzionale della cartella in cui salvare i log di partita (`--save-logs`).
 */
data class SimCliOptions(
    val games: Int,
    val seed: Long,
    val p1Name: String = DEFAULT_P1_BOT,
    val p2Name: String = DEFAULT_P2_BOT,
    val samples: Int = SearchBudget.DEFAULT_SAMPLES,
    val outFile: Path? = null,
    val saveLogsDir: Path? = null,
)

/**
 * Configurazione degli argomenti da riga di comando per la modalità analisi (`--analyze`, task F0-11).
 *
 * @property logFile Percorso del file di log da analizzare.
 * @property samples Numero di campioni PIMC per mossa (`--samples K`, default [SearchBudget.DEFAULT_SAMPLES]).
 */
data class AnalyzeCliOptions(
    val logFile: Path,
    val samples: Int = SearchBudget.DEFAULT_SAMPLES,
)

internal sealed interface CliCommand {
    data class Simulate(val options: SimCliOptions) : CliCommand
    data class Analyze(val options: AnalyzeCliOptions) : CliCommand
}

/**
 * Risultato aggregato di una sessione di simulazione (task F0-09).
 *
 * @property games Numero totale di partite giocate.
 * @property seed Seme principale utilizzato.
 * @property p1BotName Nome del bot `p1`.
 * @property p2BotName Nome del bot `p2`.
 * @property winsP1 Numero di partite vinte dal bot `p1`.
 * @property winsP2 Numero di partite vinte dal bot `p2`.
 * @property avgPointsP1 Punti medi per partita per ciascuna [ScoreCategory] ottenuti dal bot `p1`.
 * @property avgPointsP2 Punti medi per partita per ciascuna [ScoreCategory] ottenuti dal bot `p2`.
 * @property meanMsPerMove Tempo medio per mossa in millisecondi.
 * @property p95MsPerMove 95° percentile del tempo per mossa in millisecondi.
 */
data class SimulationReport(
    val games: Int,
    val seed: Long,
    val p1BotName: String,
    val p2BotName: String,
    val winsP1: Int,
    val winsP2: Int,
    val avgPointsP1: Map<ScoreCategory, Double>,
    val avgPointsP2: Map<ScoreCategory, Double>,
    val meanMsPerMove: Double,
    val p95MsPerMove: Double,
) {
    /**
     * Formatta il riepilogo leggibile per stdout (task F0-09).
     */
    fun toStdoutText(): String {
        val p1WinPct = (winsP1.toDouble() / games) * PERCENT_MULTIPLIER
        val p2WinPct = (winsP2.toDouble() / games) * PERCENT_MULTIPLIER
        val p1Categories = ScoreCategory.entries.joinToString(", ") { category ->
            "${category.name}=${formatDecimal(avgPointsP1.getValue(category))}"
        }
        val p2Categories = ScoreCategory.entries.joinToString(", ") { category ->
            "${category.name}=${formatDecimal(avgPointsP2.getValue(category))}"
        }
        return buildString {
            appendLine("maestro-sim report")
            appendLine("games: $games | seed: $seed | bots: p1=$p1BotName, p2=$p2BotName")
            appendLine(
                "wins: p1=$winsP1 (${formatDecimal(p1WinPct)}%), " +
                    "p2=$winsP2 (${formatDecimal(p2WinPct)}%)",
            )
            appendLine("avgPoints p1: $p1Categories")
            appendLine("avgPoints p2: $p2Categories")
            append("msPerMove: mean=${formatDecimal(meanMsPerMove)}, p95=${formatDecimal(p95MsPerMove)}")
        }
    }

    /**
     * Serializza questo [SimulationReport] nel formato JSON richiesto da F0-09, senza librerie esterne.
     */
    fun toJson(): String {
        val p1PointsJson = formatCategoryMapJson(avgPointsP1)
        val p2PointsJson = formatCategoryMapJson(avgPointsP2)
        return buildString {
            append('{')
            append("\"games\":").append(games).append(',')
            append("\"seed\":").append(seed).append(',')
            append("\"bots\":{\"p1\":\"").append(p1BotName).append("\",\"p2\":\"").append(p2BotName).append("\"},")
            append("\"wins\":{\"p1\":").append(winsP1).append(",\"p2\":").append(winsP2).append("},")
            append("\"avgPoints\":{\"p1\":").append(p1PointsJson).append(",\"p2\":").append(p2PointsJson).append("},")
            append("\"msPerMove\":{")
            append("\"mean\":").append(formatDecimal(meanMsPerMove)).append(',')
            append("\"p95\":").append(formatDecimal(p95MsPerMove))
            append("}}")
        }
    }
}

/**
 * Punto di ingresso del simulatore da riga di comando (`docs/backlog/fase-0.md`, task F0-09).
 *
 * Uso:
 * `./gradlew :sim:run --args="--games N --seed S --p1 <random|greedy> --p2 <random|greedy> [--out file.json] [--save-logs dir]"`
 */
fun main(args: Array<String>) {
    val exitCode = runCli(args)
    if (exitCode != 0) {
        exitProcess(exitCode)
    }
}

/**
 * Esegue la CLI del simulatore scrivendo su [out] ed [err] e restituendo il codice di uscita (`0` = ok, `1` = errore).
 */
fun runCli(
    args: Array<String>,
    out: Appendable = System.out,
    err: Appendable = System.err,
): Int {
    val command = parseCliCommand(args)
    if (command == null) {
        err.appendLine(usageMessage())
        return 1
    }

    return when (command) {
        is CliCommand.Simulate -> {
            val options = command.options
            val report = runSimulation(options)
            out.appendLine(report.toStdoutText())

            val outFile = options.outFile
            if (outFile != null) {
                outFile.parent?.let { Files.createDirectories(it) }
                Files.writeString(outFile, report.toJson())
            }
            0
        }
        is CliCommand.Analyze -> {
            runAnalyzeCli(command.options, out, err)
        }
    }
}

private fun runAnalyzeCli(
    options: AnalyzeCliOptions,
    out: Appendable,
    err: Appendable,
): Int {
    if (!Files.isRegularFile(options.logFile)) {
        err.appendLine("File di log non trovato o non valido: ${options.logFile}")
        return 1
    }

    val text = try {
        Files.readString(options.logFile)
    } catch (e: IOException) {
        err.appendLine("Errore di lettura del file di log '${options.logFile}': ${e.message}")
        return 1
    }

    val log = try {
        MatchLog.parse(text)
    } catch (e: IllegalArgumentException) {
        err.appendLine("Log non valido '${options.logFile}': ${e.message}")
        return 1
    }

    val analysis = try {
        val analyzer = MatchAnalyzer(
            evaluator = PimcEvaluator,
            budget = SearchBudget(samples = options.samples),
        )
        analyzer.analyze(log)
    } catch (e: IllegalArgumentException) {
        err.appendLine("Impossibile analizzare il log '${options.logFile}': ${e.message}")
        return 1
    }

    out.appendLine(analysis.toReadableReport())
    return 0
}

/**
 * Esegue la simulazione deterministica delle partite secondo le regole di F0-09.
 */
fun runSimulation(options: SimCliOptions): SimulationReport {
    val p1Bot = resolveBot(options.p1Name, options.samples)
        ?: error("Bot p1 non valido: ${options.p1Name}")
    val p2Bot = resolveBot(options.p2Name, options.samples)
        ?: error("Bot p2 non valido: ${options.p2Name}")

    val saveLogsDir = options.saveLogsDir
    if (saveLogsDir != null) {
        Files.createDirectories(saveLogsDir)
    }

    val master = SeededRng(options.seed)
    var winsP1 = 0
    var winsP2 = 0
    val totalPointsP1 = EnumMapOfCategories()
    val totalPointsP2 = EnumMapOfCategories()
    val moveDurationsMs = ArrayList<Double>()
    val config = ScopaConfig()

    for (gameIndex in 0 until options.games) {
        val matchSeed = master.nextLong()
        val seatP1BotRng = SeededRng(master.nextLong())
        val seatP2BotRng = SeededRng(master.nextLong())

        // Nelle partite pari il bot --p1 siede in P1, nelle dispari in P2.
        val p1Seat = if (gameIndex % 2 == 0) Seat.P1 else Seat.P2
        val p2Seat = p1Seat.other()

        var state = newMatch(config = config, seed = matchSeed)
        val moves = ArrayList<ScopaMove>()

        while (state.phase != DealPhase.MATCH_OVER) {
            val activeSeat = state.toMove
            val activeBot = if (activeSeat == p1Seat) p1Bot else p2Bot
            val activeRng = if (activeSeat == Seat.P1) seatP1BotRng else seatP2BotRng

            val view = viewFor(state, activeSeat)
            val legal = legalMoves(state)

            val startNanos = System.nanoTime()
            val chosenMove = activeBot.chooseMove(view, legal, activeRng)
            val elapsedMs = (System.nanoTime() - startNanos) / NANOS_PER_MS
            moveDurationsMs.add(elapsedMs)

            moves.add(chosenMove)
            when (val result = play(state, chosenMove)) {
                is PlayResult.Applied -> {
                    for (event in result.events) {
                        if (event is GameEvent.DealEnded) {
                            for (category in ScoreCategory.entries) {
                                totalPointsP1.add(category, event.score.points(p1Seat, category))
                                totalPointsP2.add(category, event.score.points(p2Seat, category))
                            }
                        }
                    }
                    state = result.state
                }
                is PlayResult.Rejected -> {
                    error("Mossa rifiutata durante la simulazione (partita $gameIndex): ${result.reason}")
                }
            }
        }

        if (state.winner == p1Seat) {
            winsP1++
        } else {
            winsP2++
        }

        if (saveLogsDir != null) {
            val log = MatchLog(
                rulesVersion = CoreGame.RULES_VERSION,
                config = config,
                seed = matchSeed,
                moves = moves,
            )
            val logFile = saveLogsDir.resolve("match-$gameIndex.log")
            Files.writeString(logFile, log.toText())
        }
    }

    val avgPointsP1 = ScoreCategory.entries.associateWith { category ->
        totalPointsP1.get(category).toDouble() / options.games
    }
    val avgPointsP2 = ScoreCategory.entries.associateWith { category ->
        totalPointsP2.get(category).toDouble() / options.games
    }

    val (meanMs, p95Ms) = computeTimingStats(moveDurationsMs)

    return SimulationReport(
        games = options.games,
        seed = options.seed,
        p1BotName = options.p1Name,
        p2BotName = options.p2Name,
        winsP1 = winsP1,
        winsP2 = winsP2,
        avgPointsP1 = avgPointsP1,
        avgPointsP2 = avgPointsP2,
        meanMsPerMove = meanMs,
        p95MsPerMove = p95Ms,
    )
}

internal fun parseCliArgs(args: Array<String>): SimCliOptions? =
    (parseCliCommand(args) as? CliCommand.Simulate)?.options

internal fun parseCliCommand(args: Array<String>): CliCommand? {
    if (args.isEmpty()) return null

    var games: Int? = null
    var seed: Long? = null
    var p1Name: String? = null
    var p2Name: String? = null
    var samples: Int = SearchBudget.DEFAULT_SAMPLES
    var outFile: Path? = null
    var saveLogsDir: Path? = null
    var analyzeFile: Path? = null

    var index = 0
    while (index < args.size) {
        val flag = args[index]
        val value = args.getOrNull(index + 1) ?: return null
        if (value.isBlank()) return null

        when (flag) {
            "--games" -> {
                val parsed = value.toIntOrNull() ?: return null
                if (parsed <= 0) return null
                games = parsed
            }
            "--seed" -> {
                seed = value.toLongOrNull() ?: return null
            }
            "--p1" -> {
                val normalized = value.lowercase(Locale.ROOT)
                if (!isSupportedBotName(normalized)) return null
                p1Name = normalized
            }
            "--p2" -> {
                val normalized = value.lowercase(Locale.ROOT)
                if (!isSupportedBotName(normalized)) return null
                p2Name = normalized
            }
            "--samples" -> {
                val parsed = value.toIntOrNull() ?: return null
                if (parsed <= 0) return null
                samples = parsed
            }
            "--out" -> {
                outFile = Path.of(value)
            }
            "--save-logs" -> {
                saveLogsDir = Path.of(value)
            }
            "--analyze" -> {
                analyzeFile = Path.of(value)
            }
            else -> return null
        }
        index += 2
    }

    if (analyzeFile != null) {
        if (games != null || seed != null || p1Name != null || p2Name != null || outFile != null || saveLogsDir != null) {
            return null
        }
        return CliCommand.Analyze(
            AnalyzeCliOptions(
                logFile = analyzeFile,
                samples = samples,
            ),
        )
    }

    val finalGames = games ?: return null
    val finalSeed = seed ?: return null

    return CliCommand.Simulate(
        SimCliOptions(
            games = finalGames,
            seed = finalSeed,
            p1Name = p1Name ?: DEFAULT_P1_BOT,
            p2Name = p2Name ?: DEFAULT_P2_BOT,
            samples = samples,
            outFile = outFile,
            saveLogsDir = saveLogsDir,
        ),
    )
}

internal fun usageMessage(): String =
    "Usage:\n" +
        "  sim --games <N> --seed <S> [--p1 <random|greedy|pimc>] [--p2 <random|greedy|pimc>] " +
        "[--samples <K>] [--out <file.json>] [--save-logs <dir>]\n" +
        "  sim --analyze <log-file> [--samples <K>]"

private fun isSupportedBotName(name: String): Boolean =
    name == "random" || name == "greedy" || name == "pimc"

private fun resolveBot(name: String, samples: Int = SearchBudget.DEFAULT_SAMPLES): Bot? = when (name) {
    "random" -> RandomBot
    "greedy" -> GreedyBot
    "pimc" -> PimcBot(SearchBudget(samples = samples))
    else -> null
}

private fun computeTimingStats(durationsMs: List<Double>): Pair<Double, Double> {
    if (durationsMs.isEmpty()) return 0.0 to 0.0
    val mean = durationsMs.sum() / durationsMs.size
    val sorted = durationsMs.sorted()
    val p95Index = (ceil(P95_QUANTILE * sorted.size).toInt() - 1).coerceIn(0, sorted.lastIndex)
    val p95 = sorted[p95Index]
    return mean to p95
}

private fun formatCategoryMapJson(points: Map<ScoreCategory, Double>): String =
    ScoreCategory.entries.joinToString(prefix = "{", postfix = "}", separator = ",") { category ->
        "\"${category.name}\":${formatDecimal(points.getValue(category))}"
    }

private fun formatDecimal(value: Double): String =
    String.format(Locale.US, "%.4f", value)

private class EnumMapOfCategories {
    private val totals = IntArray(ScoreCategory.entries.size)

    fun add(category: ScoreCategory, amount: Int) {
        totals[category.ordinal] += amount
    }

    fun get(category: ScoreCategory): Int = totals[category.ordinal]
}
