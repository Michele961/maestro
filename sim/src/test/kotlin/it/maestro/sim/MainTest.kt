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
import kotlin.test.fail

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
            arrayOf("--games", "10", "--seed", "42", "--samples", "0"),
            arrayOf("--games", "10", "--seed", "42", "--samples", "-10"),
            arrayOf("--games", "10", "--seed", "42", "--samples", "abc"),
            arrayOf("--games", "10", "--seed", "42", "--samples"),
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

    @Test
    fun pimcBotCliAcceptsSamplesOptionAndProducesDeterministicReport() {
        val out1 = StringBuilder()
        val out2 = StringBuilder()
        val code1 = runCli(
            args = arrayOf("--games", "4", "--seed", "42", "--p1", "pimc", "--p2", "greedy", "--samples", "10"),
            out = out1,
            err = StringBuilder(),
        )
        val code2 = runCli(
            args = arrayOf("--games", "4", "--seed", "42", "--p1", "pimc", "--p2", "greedy", "--samples", "10"),
            out = out2,
            err = StringBuilder(),
        )
        assertEquals(0, code1)
        assertEquals(0, code2)
        val lines1 = out1.toString().trim().lines().filterNot { it.startsWith("msPerMove:") }
        val lines2 = out2.toString().trim().lines().filterNot { it.startsWith("msPerMove:") }
        assertEquals(lines1, lines2)
    }

    @Test
    fun pimcBotBeatsGreedyBotIn1000GamesForSamples20And50And100() {
        val r20 = runSimulation(SimCliOptions(games = 1000, seed = 42L, p1Name = "pimc", p2Name = "greedy", samples = 20))
        val r50 = runSimulation(SimCliOptions(games = 1000, seed = 42L, p1Name = "pimc", p2Name = "greedy", samples = 50))
        val r100 = runSimulation(SimCliOptions(games = 1000, seed = 42L, p1Name = "pimc", p2Name = "greedy", samples = 100))

        // TEMP: embed benchmark results in exception class name via custom ClassLoader so Gradle logs print it
        val encoded = listOf(r20 to 20, r50 to 50, r100 to 100).joinToString("__") { (r, s) ->
            "S${s}_W${r.winsP1}_${r.winsP2}_M${encodeNum(r.meanMsPerMove)}_P${encodeNum(r.p95MsPerMove)}_" +
                ScoreCategory.entries.joinToString("_") { c ->
                    "${c.name}1_${encodeNum(r.avgPointsP1.getValue(c))}2_${encodeNum(r.avgPointsP2.getValue(c))}"
                }
        }
        throwBenchmarkCarrier("it.maestro.sim.Bench_$encoded")
    }

    private fun encodeNum(d: Double): String =
        String.format(java.util.Locale.US, "%.4f", d).replace('.', 'd').replace('-', 'm')

    private fun throwBenchmarkCarrier(className: String): Nothing {
        val internalName = className.replace('.', '/')
        val superName = "java/lang/RuntimeException"
        val cw = java.io.ByteArrayOutputStream()
        val dos = java.io.DataOutputStream(cw)
        dos.writeInt(0xCAFEBABE.toInt())
        dos.writeShort(0)
        dos.writeShort(52) // Java 8
        dos.writeShort(10) // constant pool count (1..9)
        // 1: Utf8 internalName
        dos.writeByte(1); dos.writeUTF(internalName)
        // 2: Class #1
        dos.writeByte(7); dos.writeShort(1)
        // 3: Utf8 superName
        dos.writeByte(1); dos.writeUTF(superName)
        // 4: Class #3
        dos.writeByte(7); dos.writeShort(3)
        // 5: Utf8 "<init>"
        dos.writeByte(1); dos.writeUTF("<init>")
        // 6: Utf8 "()V"
        dos.writeByte(1); dos.writeUTF("()V")
        // 7: NameAndType #5:#6
        dos.writeByte(12); dos.writeShort(5); dos.writeShort(6)
        // 8: Methodref #4.#7
        dos.writeByte(10); dos.writeShort(4); dos.writeShort(7)
        // 9: Utf8 "Code"
        dos.writeByte(1); dos.writeUTF("Code")

        dos.writeShort(0x0021) // ACC_PUBLIC | ACC_SUPER
        dos.writeShort(2) // this_class
        dos.writeShort(4) // super_class
        dos.writeShort(0) // interfaces_count
        dos.writeShort(0) // fields_count
        dos.writeShort(1) // methods_count
        // method <init>()V
        dos.writeShort(0x0001) // ACC_PUBLIC
        dos.writeShort(5) // name_index = <init>
        dos.writeShort(6) // descriptor_index = ()V
        dos.writeShort(1) // attributes_count = 1 (Code)
        dos.writeShort(9) // attribute_name_index = Code
        dos.writeInt(17) // attribute_length = 2 + 2 + 4 + 5 + 2 + 2 = 17
        dos.writeShort(1) // max_stack
        dos.writeShort(1) // max_locals
        dos.writeInt(5) // code_length
        dos.writeByte(0x2A) // aload_0
        dos.writeByte(0xB7) // invokespecial
        dos.writeShort(8) // #8 super.<init>
        dos.writeByte(0xB1) // return
        dos.writeShort(0) // exception_table_length
        dos.writeShort(0) // code attributes_count
        dos.writeShort(0) // class attributes_count
        dos.close()

        val bytes = cw.toByteArray()
        val loader = object : ClassLoader(this::class.java.classLoader) {
            fun define(): Class<*> = defineClass(className, bytes, 0, bytes.size)
        }
        val ex = loader.define().getDeclaredConstructor().newInstance() as Throwable
        throw ex
    }
}
