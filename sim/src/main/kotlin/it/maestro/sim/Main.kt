package it.maestro.sim

import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.bot.Bot
import it.maestro.ai.bot.DifficultyLevel
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
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.system.exitProcess

private const val DEFAULT_P1_BOT: String = "greedy"
private const val DEFAULT_P2_BOT: String = "random"
private const val NANOS_PER_MS: Double = 1_000_000.0
private const val PERCENT_MULTIPLIER: Double = 100.0
private const val P95_QUANTILE: Double = 0.95

/**
 * Configurazione degli argomenti da riga di comando per la modalità simulazione (task F0-09, F0-10, F1-03).
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
 */
data class AnalyzeCliOptions(
    val logFile: Path,
    val samples: Int = SearchBudget.DEFAULT_SAMPLES,
)

/**
 * Configurazione per il calcolo dell'Elo tra i livelli di difficoltà (`--elo`, task F1-03, decisione D3).
 */
data class EloCliOptions(
    val games: Int = 100,
    val seed: Long = 20261024L,
    val outFile: Path? = null,
)

internal sealed interface CliCommand {
    data class Simulate(val options: SimCliOptions) : CliCommand
    data class Analyze(val options: AnalyzeCliOptions) : CliCommand
    data class Elo(val options: EloCliOptions) : CliCommand
}

/**
 * Risultato aggregato di una sessione di simulazione (task F0-09).
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
    val winRateP1Percent: Double get() = (winsP1.toDouble() / games) * PERCENT_MULTIPLIER
    val winRateP2Percent: Double get() = (winsP2.toDouble() / games) * PERCENT_MULTIPLIER

    fun toStdoutText(): String = buildString {
        appendLine("=== Maestro — Report di Simulazione ===")
        appendLine("Partite: $games | Seme: $seed")
        appendLine("Bot P1: $p1BotName | Bot P2: $p2BotName")
        appendLine(
            "Vittorie: P1 = $winsP1 (${formatDecimal(winRateP1Percent)}%) | " +
                "P2 = $winsP2 (${formatDecimal(winRateP2Percent)}%)",
        )
        appendLine("--- Punti medi per smazzata/partita ---")
        for (category in ScoreCategory.entries) {
            val p1Avg = avgPointsP1[category] ?: 0.0
            val p2Avg = avgPointsP2[category] ?: 0.0
            appendLine(
                "  %-12s: P1 = %-6s | P2 = %-6s".format(
                    Locale.US,
                    category.name,
                    formatDecimal(p1Avg),
                    formatDecimal(p2Avg),
                ),
            )
        }
        appendLine("--- Tempi per mossa ---")
        appendLine("  Media: ${formatDecimal(meanMsPerMove)} ms | p95: ${formatDecimal(p95MsPerMove)} ms")
    }

    fun toJson(): String = buildString {
        append("{\"games\":").append(games)
        append(",\"seed\":").append(seed)
        append(",\"bots\":{\"p1\":\"").append(p1BotName).append("\",\"p2\":\"").append(p2BotName).append("\"}")
        append(",\"wins\":{\"p1\":").append(winsP1).append(",\"p2\":").append(winsP2).append("}")
        append(",\"avgPoints\":{\"p1\":").append(formatCategoryMapJson(avgPointsP1))
        append(",\"p2\":").append(formatCategoryMapJson(avgPointsP2)).append("}")
        append(",\"msPerMove\":{\"mean\":").append(formatDecimal(meanMsPerMove))
        append(",\"p95\":").append(formatDecimal(p95MsPerMove)).append("}}")
    }
}

/**
 * Risultato del confronto tra due livelli consecutivi nel torneo Elo.
 */
data class EloMatchupResult(
    val lowerLevel: DifficultyLevel,
    val higherLevel: DifficultyLevel,
    val winsLower: Int,
    val winsHigher: Int,
    val games: Int,
) {
    val winRateHigher: Double get() = winsHigher.toDouble() / games
    val deltaElo: Double
        get() {
            val w = winRateHigher.coerceIn(0.01, 0.99)
            return 400.0 * log10(w / (1.0 - w))
        }
}

/**
 * Report aggregato del torneo Elo tra i 5 livelli di difficoltà del Maestro (task F1-03).
 */
data class EloTournamentReport(
    val gamesPerMatchup: Int,
    val seed: Long,
    val matchups: List<EloMatchupResult>,
    val measuredElos: Map<DifficultyLevel, Int>,
    val avgAccuracies: Map<DifficultyLevel, Int>,
) {
    fun toStdoutText(): String = buildString {
        appendLine("=== Maestro — Torneo e Misurazione Elo Livelli ===")
        appendLine("Partite per scontro: $gamesPerMatchup | Seme base: $seed")
        appendLine("--------------------------------------------------------------------------------")
        appendLine("%-14s | %-10s | %-10s | %-14s | %-10s".format("Livello", "Elo Target", "Elo Misurato", "Precisione Media", "Vs Precedente"))
        appendLine("--------------------------------------------------------------------------------")
        for (level in DifficultyLevel.entries) {
            val target = level.targetElo
            val measured = measuredElos[level] ?: target
            val acc = "${avgAccuracies[level] ?: 0}%"
            val vsPrev = matchups.firstOrNull { it.higherLevel == level }
            val vsPrevText = if (vsPrev != null) {
                "${formatDecimal(vsPrev.winRateHigher * 100.0)}% win"
            } else {
                "— (ancora base)"
            }
            appendLine("%-14s | %-10d | %-10d | %-14s | %-10s".format(level.name, target, measured, acc, vsPrevText))
        }
        appendLine("--------------------------------------------------------------------------------")
    }

    fun toJson(): String = buildString {
        append("{\"gamesPerMatchup\":").append(gamesPerMatchup)
        append(",\"seed\":").append(seed)
        append(",\"levels\":{")
        val levelEntries = DifficultyLevel.entries.joinToString(",") { level ->
            val target = level.targetElo
            val measured = measuredElos[level] ?: target
            val acc = avgAccuracies[level] ?: 0
            "\"${level.name}\":{\"targetElo\":$target,\"measuredElo\":$measured,\"avgAccuracy\":$acc}"
        }
        append(levelEntries).append("}")
        append(",\"matchups\":[")
        val matchupEntries = matchups.joinToString(",") { m ->
            "{\"lower\":\"${m.lowerLevel.name}\",\"higher\":\"${m.higherLevel.name}\",\"games\":${m.games},\"winsLower\":${m.winsLower},\"winsHigher\":${m.winsHigher},\"deltaElo\":${formatDecimal(m.deltaElo)}}"
        }
        append(matchupEntries).append("]}")
    }
}

fun main(args: Array<String>) {
    val exitCode = runCli(args)
    if (exitCode != 0) {
        exitProcess(exitCode)
    }
}

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
        is CliCommand.Elo -> {
            val report = runEloTournament(command.options)
            out.appendLine(report.toStdoutText())

            val outFile = command.options.outFile
            if (outFile != null) {
                outFile.parent?.let { Files.createDirectories(it) }
                Files.writeString(outFile, report.toJson())
            }
            0
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

/**
 * Esegue il torneo di calibrazione Elo tra i 5 livelli di difficoltà consecutivi (task F1-03).
 */
fun runEloTournament(options: EloCliOptions): EloTournamentReport {
    val levels = DifficultyLevel.entries
    val matchups = mutableListOf<EloMatchupResult>()
    val measuredElos = mutableMapOf<DifficultyLevel, Int>()
    val avgAccuracies = mutableMapOf<DifficultyLevel, Int>()

    var currentElo = DifficultyLevel.PRINCIPIANTE.targetElo
    measuredElos[DifficultyLevel.PRINCIPIANTE] = currentElo

    for (i in 0 until levels.size - 1) {
        val lower = levels[i]
        val higher = levels[i + 1]

        val simReport = runSimulation(
            SimCliOptions(
                games = options.games,
                seed = options.seed + i * 1000L,
                p1Name = "level:${lower.name}",
                p2Name = "level:${higher.name}",
            ),
        )

        val matchup = EloMatchupResult(
            lowerLevel = lower,
            higherLevel = higher,
            winsLower = simReport.winsP1,
            winsHigher = simReport.winsP2,
            games = options.games,
        )
        matchups.add(matchup)

        currentElo += matchup.deltaElo.roundToInt()
        measuredElos[higher] = currentElo
    }

    // Calcolo della precisione media su un campione di partite analizzate per ciascun livello
    val analyzer = MatchAnalyzer(evaluator = PimcEvaluator, budget = SearchBudget(samples = 10))
    for (level in levels) {
        val bot = level.toBot()
        val master = SeededRng(options.seed + level.ordinal * 10000L)
        val precisions = mutableListOf<Int>()
        for (sampleMatch in 0 until 5) {
            val seed = master.nextLong()
            val state = simulateSingleMatch(bot, bot, seed)
            val log = MatchLog(
                rulesVersion = CoreGame.RULES_VERSION,
                config = ScopaConfig(),
                seed = seed,
                moves = state.playedMoves,
            )
            val analysis = analyzer.analyze(log)
            precisions.add(analysis.accuracy(Seat.P1))
            precisions.add(analysis.accuracy(Seat.P2))
        }
        avgAccuracies[level] = precisions.average().roundToInt()
    }

    return EloTournamentReport(
        gamesPerMatchup = options.games,
        seed = options.seed,
        matchups = matchups,
        measuredElos = measuredElos,
        avgAccuracies = avgAccuracies,
    )
}

private data class SingleMatchResult(
    val winner: Seat,
    val playedMoves: List<ScopaMove>,
)

private fun simulateSingleMatch(p1Bot: Bot, p2Bot: Bot, seed: Long): SingleMatchResult {
    val master = SeededRng(seed)
    val p1Rng = SeededRng(master.nextLong())
    val p2Rng = SeededRng(master.nextLong())
    var state = newMatch(config = ScopaConfig(), seed = seed)
    val moves = mutableListOf<ScopaMove>()

    while (state.phase != DealPhase.MATCH_OVER) {
        val seat = state.toMove
        val bot = if (seat == Seat.P1) p1Bot else p2Bot
        val rng = if (seat == Seat.P1) p1Rng else p2Rng
        val move = bot.chooseMove(viewFor(state, seat), legalMoves(state), rng)
        moves.add(move)
        val applied = play(state, move) as PlayResult.Applied
        state = applied.state
    }
    return SingleMatchResult(winner = state.winner ?: Seat.P1, playedMoves = moves)
}

internal fun parseCliArgs(args: Array<String>): SimCliOptions? =
    (parseCliCommand(args) as? CliCommand.Simulate)?.options

internal fun parseCliCommand(args: Array<String>): CliCommand? {
    if (args.isEmpty()) return null

    var isElo = false
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
        if (flag == "--elo") {
            isElo = true
            index++
            continue
        }

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

    if (isElo) {
        return CliCommand.Elo(
            EloCliOptions(
                games = games ?: 100,
                seed = seed ?: 20261024L,
                outFile = outFile,
            ),
        )
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
        "  sim --games <N> --seed <S> [--p1 <random|greedy|pimc|level:<name>>] [--p2 <...>] " +
        "[--samples <K>] [--out <file.json>] [--save-logs <dir>]\n" +
        "  sim --analyze <log-file> [--samples <K>]\n" +
        "  sim --elo [--games <N>] [--seed <S>] [--out <file.json>]"

private fun isSupportedBotName(name: String): Boolean =
    name == "random" || name == "greedy" || name == "pimc" ||
        DifficultyLevel.fromNameOrNull(name) != null

private fun resolveBot(name: String, samples: Int = SearchBudget.DEFAULT_SAMPLES): Bot? {
    val level = DifficultyLevel.fromNameOrNull(name)
    if (level != null) return level.toBot()
    return when (name) {
        "random" -> RandomBot
        "greedy" -> GreedyBot
        "pimc" -> PimcBot(SearchBudget(samples = samples))
        else -> null
    }
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
