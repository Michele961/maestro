package it.maestro.app.architecture

/**
 * Divieti di `docs/app/layers.md` §6 verificati sui sorgenti (task F1R-01, ADR-0014…0017).
 *
 * Il controllo è testuale: le righe di commento sono ignorate, le stringhe no
 * (una violazione dentro una stringa è comunque un segnale da guardare).
 */
internal enum class Rule(val description: String) {
    UI_IMPORTS_DATA("UI e presentazione non importano il layer dati"),
    DOMAIN_IMPORTS_PLATFORM("Il dominio non importa Android, Compose, dati o UI"),
    TIMER_OUTSIDE_DIRECTOR("delay/Thread.sleep solo in TableDirector e MotionSpec (ADR-0015)"),
    TIME_OUTSIDE_CLOCK("System.currentTimeMillis/LocalDate.now/Random( solo in Clock"),
    DESTRUCTIVE_MIGRATION("fallbackToDestructiveMigration vietato (ADR-0017)"),
    SCOPA_STATE_OUTSIDE_MATCH("ScopaState solo in domain/match"),
    CREDITS_OUTSIDE_REWARD_ENGINE("I crediti si accreditano solo da RewardEngine (task F1R-14)"),
}

internal data class Violation(val path: String, val rule: Rule, val line: Int, val text: String)

internal object ArchitectureRules {

    private const val APP_PACKAGE = "it/maestro/app/"

    private val uiPrefixes = listOf("feature/", "designsystem/")
    private val domainPrefix = "domain/"
    private val matchPrefix = "domain/match/"

    private val timerRegex = Regex("""\bdelay\(|\bThread\.sleep\(""")
    private val timeRegex = Regex("""\bSystem\.currentTimeMillis\(|\bLocalDate\.now\(|\bRandom\(""")
    private val scopaStateRegex = Regex("""\bScopaState\b""")
    private val creditsRegex = Regex("""\.(addCredits|ensureWelcomeBonus)\(""")
    private val domainForbiddenImports = listOf(
        "import android.",
        "import androidx.",
        "import it.maestro.app.data",
        "import it.maestro.app.ui",
        "import it.maestro.app.feature",
    )

    /**
     * Restituisce le violazioni di [content], il sorgente che si trova in [path]
     * (relativo a `src/main/kotlin`, es. `it/maestro/app/ui/screens/HomeScreen.kt`).
     */
    fun check(path: String, content: String): List<Violation> {
        val local = path.removePrefix(APP_PACKAGE)
        val fileName = local.substringAfterLast('/')
        val isUi = uiPrefixes.any { local.startsWith(it) }
        val isDomain = local.startsWith(domainPrefix)
        val timerAllowed = fileName.startsWith("TableDirector") || fileName.startsWith("MotionSpec")
        val timeAllowed = fileName.contains("Clock")
        val scopaStateAllowed = local.startsWith(matchPrefix)
        val creditsAllowed = local.startsWith("domain/rewards/") || local.startsWith("data/")

        val violations = mutableListOf<Violation>()
        content.lines().forEachIndexed { index, raw ->
            val line = raw.trim()
            if (line.startsWith("//") || line.startsWith("*") || line.startsWith("/*")) return@forEachIndexed
            fun add(rule: Rule) = violations.add(Violation(path, rule, index + 1, line))

            if (isUi && line.startsWith("import it.maestro.app.data")) add(Rule.UI_IMPORTS_DATA)
            if (isDomain && domainForbiddenImports.any { line.startsWith(it) }) add(Rule.DOMAIN_IMPORTS_PLATFORM)
            if (line.startsWith("import ")) return@forEachIndexed
            if (!timerAllowed && timerRegex.containsMatchIn(line)) add(Rule.TIMER_OUTSIDE_DIRECTOR)
            if (!timeAllowed && timeRegex.containsMatchIn(line)) add(Rule.TIME_OUTSIDE_CLOCK)
            if (line.contains("fallbackToDestructiveMigration")) add(Rule.DESTRUCTIVE_MIGRATION)
            if (!scopaStateAllowed && scopaStateRegex.containsMatchIn(line)) add(Rule.SCOPA_STATE_OUTSIDE_MATCH)
            if (!creditsAllowed && creditsRegex.containsMatchIn(line)) add(Rule.CREDITS_OUTSIDE_REWARD_ENGINE)
        }
        return violations
    }
}
