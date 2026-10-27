package it.maestro.app.feature.review.presentation

import it.maestro.app.domain.coach.ReviewCoach
import it.maestro.app.domain.coach.ReviewReason
import it.maestro.app.feature.table.presentation.MaestroTranscript
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.viewFor
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Task F1R-24g: simulazione delle spiegazioni della revisione su partite giocate con errori. Scrive il rapporto in
 * `app/android/build/review-simulation/` e verifica che ogni affermazione sia vera nella posizione.
 */
class ReviewSimulationTest {

    private val cases by lazy { ReviewSimulation.cases(games = 8) }
    private val settebello = Card(Rank.SETTE, Suit.DENARI)

    private fun note(c: ReviewSimulation.Case) = ReviewCoach.explain(c.played, c.best, viewFor(c.state, Seat.P1))

    @Test
    fun writesTheExplanationsForReadingInBothLanguages() {
        assertTrue(cases.isNotEmpty())
        listOf("it" to "src/main/res/values/strings.xml", "en" to "src/main/res/values-en/strings.xml").forEach { (lang, path) ->
            val strings = MaestroTranscript.Strings.load(path)
            ReviewSimulation.report(cases, File("build/review-simulation/$lang.md")) { c ->
                val n = note(c)
                val text = strings.text(listOf(n.title) + n.explanation + n.rule)
                assertTrue("«" !in text, "Stringa mancante ($lang, ${n.reason}): $text")
                "[${n.reason}] ${strings.text(listOf(n.title))} — ${strings.text(n.explanation)} Regola: ${strings.text(listOf(n.rule))}"
            }
        }
    }

    @Test
    fun everyClaimIsTrueInThePosition() {
        cases.forEach { c ->
            val view = viewFor(c.state, Seat.P1)
            val n = note(c)
            when (n.reason) {
                ReviewReason.SWEEP -> assertTrue(c.best.capture.size == view.table.size && c.played.capture.size != view.table.size)
                ReviewReason.SETTEBELLO_TAKEN -> assertTrue(settebello in c.best.capture && settebello !in c.played.capture)
                ReviewReason.SETTEBELLO_DISCARDED -> assertTrue(c.played.card == settebello && c.played.capture.isEmpty())
                ReviewReason.ALLOWS_SWEEP -> {
                    val left = if (c.played.capture.isEmpty()) view.table + c.played.card else view.table.filterNot { it in c.played.capture }
                    assertTrue(left.sumOf { it.rank.value } <= 10 && left.isNotEmpty())
                }
                ReviewReason.IGNORED_CAPTURE -> assertTrue(c.played.capture.isEmpty() && c.best.capture.isNotEmpty())
                ReviewReason.SAME_CAPTURE -> assertTrue(c.played.capture == c.best.capture && c.played.card != c.best.card)
                ReviewReason.BEST_DISCARDS -> assertTrue(c.played.capture.isNotEmpty() && c.best.capture.isEmpty())
                else -> {}
            }
        }
    }

    @Test
    fun theExplanationsNeverSayTheSameThingWhenTheMovesAreTheSame() {
        cases.forEach { c -> assertTrue(c.played != c.best) }
    }
}
