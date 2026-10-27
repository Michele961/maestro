package it.maestro.app.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Guardrail d'architettura dell'app (task F1R-01, `docs/app/layers.md` §6).
 *
 * Le violazioni già presenti su `main` stanno in [knownExceptions], ognuna con il task F1R che la elimina.
 * La lista può solo accorciarsi: un'eccezione non più necessaria fa fallire il test finché non viene tolta.
 */
class ArchitectureRulesTest {

    private val sourceRoot = File("src/main/kotlin")

    /** (file relativo a `src/main/kotlin`, regola) → task che rimuove la violazione. */
    private val knownExceptions: Map<Pair<String, Rule>, String> = mapOf(
        // Layer: la presentazione parla con i repository solo tramite interfacce del dominio.
        ex("feature/missions/ui/DailyMissionsDialog.kt", Rule.UI_IMPORTS_DATA) to "F1R-14",
        ex("feature/missions/presentation/DailyMissionsViewModel.kt", Rule.UI_IMPORTS_DATA) to "F1R-14",
        ex("feature/shop/presentation/ShopViewModel.kt", Rule.UI_IMPORTS_DATA) to "F1R-14",
        ex("feature/shop/ui/ShopScreen.kt", Rule.UI_IMPORTS_DATA) to "F1R-14",
        ex("feature/profile/presentation/ProfileViewModel.kt", Rule.UI_IMPORTS_DATA) to "F1R-14",
        ex("feature/profile/ui/ProfileScreen.kt", Rule.UI_IMPORTS_DATA) to "F1R-14",
        ex("feature/puzzle/presentation/DailyPuzzleViewModel.kt", Rule.UI_IMPORTS_DATA) to "F1R-14",
        ex("feature/puzzle/ui/DailyPuzzleScreen.kt", Rule.UI_IMPORTS_DATA) to "F1R-14",
        ex("feature/academy/presentation/AcademyViewModel.kt", Rule.UI_IMPORTS_DATA) to "F1R-14",
        ex("feature/academy/ui/AcademyScreen.kt", Rule.UI_IMPORTS_DATA) to "F1R-14",
        ex("feature/table/presentation/GameViewModel.kt", Rule.UI_IMPORTS_DATA) to "F1R-08",
        ex("designsystem/audio/SoundManager.kt", Rule.UI_IMPORTS_DATA) to "F1R-20",
        // Timer: la regia del tavolo sostituisce i ritardi sparsi.
        ex("feature/table/ui/TableScreen.kt", Rule.TIMER_OUTSIDE_DIRECTOR) to "F1R-12",
        ex("feature/table/ui/SweepCelebrationOverlay.kt", Rule.TIMER_OUTSIDE_DIRECTOR) to "F1R-12",
    )

    @Test
    fun sourcesRespectLayerRules() {
        val violations = scanSources()
        val unexpected = violations.filter { (it.path to it.rule) !in knownExceptions }
        if (unexpected.isNotEmpty()) {
            fail(
                "Violazioni di docs/app/layers.md §6:\n" +
                    unexpected.joinToString("\n") { "- ${it.path}:${it.line} [${it.rule.description}] ${it.text}" },
            )
        }
    }

    @Test
    fun knownExceptionsAreStillNeeded() {
        val present = scanSources().map { it.path to it.rule }.toSet()
        val stale = knownExceptions.keys - present
        assertTrue(
            stale.isEmpty(),
            "Eccezioni non più necessarie, toglile da knownExceptions:\n" +
                stale.joinToString("\n") { "- ${it.first} [${it.second}]" },
        )
    }

    @Test
    fun everyExceptionCitesAnF1rTask() {
        knownExceptions.values.forEach { task ->
            assertTrue(Regex("""F1R-\d{2}""").matches(task), "Eccezione senza task F1R valido: $task")
        }
    }

    // Test del test: ogni regola scatta su un esempio e tace sul codice ammesso.

    @Test
    fun uiImportingDataIsAViolation() {
        assertRules(
            "it/maestro/app/feature/demo/ui/NewScreen.kt",
            "import it.maestro.app.data.local.wallet.WalletRepository",
            Rule.UI_IMPORTS_DATA,
        )
        assertRules("it/maestro/app/data/Foo.kt", "import it.maestro.app.data.local.Bar")
    }

    @Test
    fun domainImportingPlatformIsAViolation() {
        assertRules(
            "it/maestro/app/domain/rewards/RewardEngine.kt",
            "import androidx.compose.runtime.Composable",
            Rule.DOMAIN_IMPORTS_PLATFORM,
        )
        assertRules("it/maestro/app/domain/rewards/RewardEngine.kt", "import it.maestro.game.model.Card")
    }

    @Test
    fun timersOutsideDirectorAreViolations() {
        assertRules("it/maestro/app/feature/demo/ui/NewScreen.kt", "    delay(300L)", Rule.TIMER_OUTSIDE_DIRECTOR)
        assertRules("it/maestro/app/feature/demo/ui/NewScreen.kt", "Thread.sleep(10)", Rule.TIMER_OUTSIDE_DIRECTOR)
        assertRules("it/maestro/app/feature/table/presentation/TableDirector.kt", "delay(step.duration)")
        assertRules("it/maestro/app/designsystem/MotionSpec.kt", "delay(ms)")
        assertRules("it/maestro/app/feature/demo/ui/NewScreen.kt", "// delay(300L) nel commento")
    }

    @Test
    fun timeSourcesOutsideClockAreViolations() {
        val path = "it/maestro/app/feature/demo/ui/NewViewModel.kt"
        assertRules(path, "val now = System.currentTimeMillis()", Rule.TIME_OUTSIDE_CLOCK)
        assertRules(path, "val today = LocalDate.now()", Rule.TIME_OUTSIDE_CLOCK)
        assertRules(path, "val rng = Random(42)", Rule.TIME_OUTSIDE_CLOCK)
        assertRules(path, "val rng = SeededRng(42)")
        assertRules("it/maestro/app/domain/time/SystemClock.kt", "LocalDate.now()")
    }

    @Test
    fun destructiveMigrationIsAViolationEverywhere() {
        assertRules(
            "it/maestro/app/data/Db.kt",
            ".fallbackToDestructiveMigration().build()",
            Rule.DESTRUCTIVE_MIGRATION,
        )
    }

    @Test
    fun scopaStateOutsideMatchIsAViolation() {
        assertRules(
            "it/maestro/app/feature/demo/ui/NewScreen.kt",
            "fun draw(state: ScopaState) {}",
            Rule.SCOPA_STATE_OUTSIDE_MATCH,
        )
        assertRules("it/maestro/app/domain/match/MatchSession.kt", "private var state: ScopaState")
    }

    @Test
    fun newViolationInRealSourceSetWouldFail() {
        // Una violazione nuova in un file senza eccezione non è coperta da knownExceptions.
        val violation = ArchitectureRules.check(
            "it/maestro/app/feature/home/ui/HomeScreen.kt",
            "import it.maestro.app.data.local.wallet.WalletRepository",
        ).single()
        assertTrue((violation.path to violation.rule) !in knownExceptions)
    }

    private fun assertRules(path: String, line: String, vararg expected: Rule) {
        assertEquals(expected.toList(), ArchitectureRules.check(path, line).map { it.rule }, "Riga: $line")
    }

    private fun scanSources(): List<Violation> {
        check(sourceRoot.isDirectory) { "Sorgenti non trovati in ${sourceRoot.absolutePath}" }
        return sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                val path = file.relativeTo(sourceRoot).invariantSeparatorsPath
                ArchitectureRules.check(path, file.readText()).asSequence()
            }
            .toList()
    }

    private fun ex(path: String, rule: Rule): Pair<String, Rule> = "it/maestro/app/$path" to rule
}
