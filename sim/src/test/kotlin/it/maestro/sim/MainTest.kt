package it.maestro.sim

import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ReplayResult
import it.maestro.game.scopa.ScoreCategory
import it.maestro.game.scopa.replay
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MainTest {

    @Test
    fun sameArgumentsProduceSameReportExcludingTiming() {
        val outDir = Files.createTempDirectory("maestro-sim-determinism-")
        try {
            val json1Path = outDir.resolve("report1.json")
            val json2Path = outDir.resolve("report2.json")

            val stdout1 = StringBuilder()
            val stderr1 = StringBuilder()
            val code1 = runCli(
                args = arrayOf(
                    "--games",
                    "20",
                    "--seed",
                    "42",
                    "--p1",
                    "greedy",
                    "--p2",
                    "random",
                    "--out",
                    json1Path.toString(),
                ),
                out = stdout1,
                err = stderr1,
            )

            val stdout2 = StringBuilder()
            val stderr2 = StringBuilder()
            val code2 = runCli(
                args = arrayOf(
                    "--games",
                    "20",
                    "--seed",
                    "42",
                    "--p1",
                    "greedy",
                    "--p2",
                    "random",
                    "--out",
                    json2Path.toString(),
                ),
                out = stdout2,
                err = stderr2,
            )

            assertEquals(0, code1)
            assertEquals(0, code2)
            assertEquals("", stderr1.toString())
            assertEquals("", stderr2.toString())

            val lines1ExcludingTime = stdout1.toString().trim().lines().filterNot { it.startsWith("msPerMove:") }
            val lines2ExcludingTime = stdout2.toString().trim().lines().filterNot { it.startsWith("msPerMove:") }
            assertEquals(lines1ExcludingTime, lines2ExcludingTime)

            val json1WithoutTiming = Files.readString(json1Path).substringBefore(",\"msPerMove\":")
            val json2WithoutTiming = Files.readString(json2Path).substringBefore(",\"msPerMove\":")
            assertEquals(json1WithoutTiming, json2WithoutTiming)
        } finally {
            outDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun invalidArgumentsPrintUsageAndReturnNonZeroExitCode() {
        val invalidCases = listOf(
            emptyArray(),
            arrayOf("--unknown", "1"),
            arrayOf("--games", "10"),
            arrayOf("--seed", "42"),
            arrayOf("--games", "0", "--seed", "42"),
            arrayOf("--games", "-5", "--seed", "42"),
            arrayOf("--games", "abc", "--seed", "42"),
            arrayOf("--games", "10", "--seed", "xyz"),
            arrayOf("--games", "10", "--seed", "42", "--p1", "unknownBot"),
            arrayOf("--games", "10", "--seed", "42", "--p2", "unknownBot"),
            arrayOf("--games", "10", "--seed", "42", "--out"),
            arrayOf("--games", "10", "--seed", "42", "--save-logs", "   "),
        )

        for (args in invalidCases) {
            val stdout = StringBuilder()
            val stderr = StringBuilder()
            val exitCode = runCli(args = args, out = stdout, err = stderr)
            assertNotEquals(0, exitCode, "Atteso exit code != 0 per argomenti: ${args.joinToString(" ")}")
            assertEquals("", stdout.toString())
            assertTrue(
                stderr.toString().contains("Usage:"),
                "Atteso messaggio d'uso su stderr per argomenti: ${args.joinToString(" ")}",
            )
        }
    }

    private companion object {
        // Risultato deterministico esatto su 1000 partite con seed 42 (GreedyBot vs RandomBot):
        // wins: p1=928 (92.8000%), p2=72 (7.2000%)
        private const val EXPECTED_GREEDY_WINS_SEED_42: Int = 928
    }

    @Test
    fun greedyBotBeatsRandomBotInMoreThanHalfOf1000Games() {
        val report = runSimulation(
            SimCliOptions(
                games = 1000,
                seed = 42L,
                p1Name = "greedy",
                p2Name = "random",
            ),
        )

        assertEquals(1000, report.winsP1 + report.winsP2)
        assertTrue(
            report.winsP1 > 500,
            "GreedyBot deve vincere più di 500 partite su 1000 contro RandomBot, vinte: ${report.winsP1}",
        )
        assertEquals(
            EXPECTED_GREEDY_WINS_SEED_42,
            report.winsP1,
            "Valore deterministico atteso per seed=42 (1000 partite): report=${report.toStdoutText()}",
        )
    }

    @Test
    fun saveLogsWritesValidF007LogsThatReplayToMatchOver() {
        val logsDir = Files.createTempDirectory("maestro-sim-logs-")
        try {
            val stdout = StringBuilder()
            val stderr = StringBuilder()
            val exitCode = runCli(
                args = arrayOf(
                    "--games",
                    "5",
                    "--seed",
                    "99",
                    "--p1",
                    "greedy",
                    "--p2",
                    "random",
                    "--save-logs",
                    logsDir.toString(),
                ),
                out = stdout,
                err = stderr,
            )

            assertEquals(0, exitCode)
            assertEquals("", stderr.toString())

            for (i in 0 until 5) {
                val logFile = logsDir.resolve("match-$i.log")
                assertTrue(Files.exists(logFile), "File di log mancante: $logFile")
                val content = Files.readString(logFile)
                val parsed = MatchLog.parse(content)
                assertEquals(content, parsed.toText())
                val replayResult = replay(parsed)
                assertTrue(replayResult is ReplayResult.Ok, "Il replay del log $i deve avere successo")
                assertEquals(DealPhase.MATCH_OVER, replayResult.state.phase)
            }
        } finally {
            logsDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun outOptionWritesExpectedJsonStructure() {
        val tempDir = Files.createTempDirectory("maestro-sim-json-")
        try {
            val outFile = tempDir.resolve("nested/report.json")
            val exitCode = runCli(
                args = arrayOf(
                    "--games",
                    "6",
                    "--seed",
                    "7",
                    "--p1",
                    "random",
                    "--p2",
                    "greedy",
                    "--out",
                    outFile.toString(),
                ),
                out = StringBuilder(),
                err = StringBuilder(),
            )

            assertEquals(0, exitCode)
            assertTrue(Files.exists(outFile))
            val json = Files.readString(outFile)
            assertTrue(json.startsWith("{\"games\":6,\"seed\":7,\"bots\":{\"p1\":\"random\",\"p2\":\"greedy\"},\"wins\":{"))
            assertTrue(json.contains("\"avgPoints\":{\"p1\":{"))
            for (category in ScoreCategory.entries) {
                assertTrue(json.contains("\"${category.name}\":"))
            }
            assertTrue(json.contains("\"msPerMove\":{\"mean\":"))
            assertTrue(json.endsWith("}}"))
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun mainRunsWithDefaultBotsWhenOnlyGamesAndSeedAreProvided() {
        main(arrayOf("--games", "2", "--seed", "1"))
    }
}
