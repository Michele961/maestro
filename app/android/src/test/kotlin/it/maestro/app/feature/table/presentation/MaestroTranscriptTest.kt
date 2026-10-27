package it.maestro.app.feature.table.presentation

import it.maestro.app.domain.coach.Chattiness
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Task F1R-24f: le trascrizioni si scrivono in `app/android/build/maestro-transcripts/` e ogni frase ha il suo testo. */
class MaestroTranscriptTest {

    private val seeds = (1L..5L).toList()

    @Test
    fun everySpokenLineHasItalianAndEnglishText() {
        listOf("it" to "src/main/res/values/strings.xml", "en" to "src/main/res/values-en/strings.xml").forEach { (lang, path) ->
            val strings = MaestroTranscript.Strings.load(path)
            val entries = MaestroTranscript.write(File("build/maestro-transcripts/$lang-normal.md"), seeds, Chattiness.NORMAL, humor = 1, strings = strings)
            assertTrue(entries.isNotEmpty(), "Nessuna battuta ($lang)")
            entries.forEach { assertTrue("«" !in it.text, "Stringa mancante ($lang): ${it.text}") }
        }
    }

    @Test
    fun theTranscriptIsReproducible() {
        val strings = MaestroTranscript.Strings.load("src/main/res/values/strings.xml")
        val a = MaestroTranscript.play(3L, Chattiness.TALKATIVE, 2, strings).map { it.text }
        val b = MaestroTranscript.play(3L, Chattiness.TALKATIVE, 2, strings).map { it.text }
        assertEquals(a, b)
    }

    @Test
    fun writesTheVariantsMichelePicksFrom() {
        val strings = MaestroTranscript.Strings.load("src/main/res/values/strings.xml")
        listOf(Chattiness.TALKATIVE, Chattiness.NORMAL, Chattiness.QUIET).forEach { level ->
            MaestroTranscript.write(File("build/maestro-transcripts/it-${level.name.lowercase()}.md"), seeds, level, humor = 2, strings = strings)
        }
    }
}
