package it.maestro.app.ui.screenshot

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment

/**
 * Configurazioni di riferimento per gli screenshot (ADR-0016, `docs/app/responsive-ui.md` §4, task F1R-06).
 *
 * `pixel10pro` usa le misure stimate finché Michele non conferma `adb shell wm size` e `wm density`.
 */
enum class ScreenConfig(val id: String, val qualifiers: String, val fontScale: Float) {
    SMALL("small", "it-w360dp-h640dp-xhdpi", 1f),
    MEDIUM("medium", "it-w393dp-h852dp-xxhdpi", 1f),
    PIXEL_10_PRO("pixel10pro", "it-w410dp-h914dp-xxxhdpi", 1f),
    SMALL_FONT_130("small-font130", "it-w360dp-h640dp-xhdpi", 1.3f),
    ;

    /** Percorso dello screenshot [name] per questa configurazione. */
    fun path(name: String): String = "build/outputs/roborazzi/$id/$name.png"

    override fun toString(): String = id

    companion object {
        /** Parametri per `ParameterizedRobolectricTestRunner`: ogni test gira su tutta la matrice. */
        fun parameters(): List<Array<Any>> = entries.map { arrayOf<Any>(it) }
    }
}

/**
 * Imposta dimensioni, densità e scala del testo di [config] prima che la regola Compose avvii l'activity.
 * Va dichiarata con `@get:Rule(order = 0)`, la regola Compose con `order = 1`.
 */
class ScreenConfigRule(private val config: ScreenConfig) : TestRule {
    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            RuntimeEnvironment.setQualifiers(config.qualifiers)
            RuntimeEnvironment.setFontScale(config.fontScale)
            try {
                base.evaluate()
            } finally {
                RuntimeEnvironment.setFontScale(1f)
            }
        }
    }
}
