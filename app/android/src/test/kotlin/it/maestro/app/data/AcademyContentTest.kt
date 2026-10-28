package it.maestro.app.data

import it.maestro.app.data.local.academy.AcademyContent
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * F1R-28a: i testi dell'Accademia non inventano regole. Ogni lezione dichiara i paragrafi di
 * `docs/rules/scopa.md` che usa (e devono esistere), e nessun testo cita articoli del regolamento che non
 * esistono né attribuisce al Settebello più di un punto (`docs/rules/scopa.md` §6.2).
 */
class AcademyContentTest {

    private val root = generateSequence(File(".").canonicalFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").exists() }

    private fun academyStrings(path: String): Map<String, String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(root, path))
        val nodes = doc.getElementsByTagName("string")
        return (0 until nodes.length).map { nodes.item(it) }
            .associate { it.attributes.getNamedItem("name").nodeValue to it.textContent }
            .filterKeys { it.startsWith("academy_") }
    }

    private val allAcademyTexts: Map<String, String> =
        academyStrings("app/android/src/main/res/values/strings.xml").mapKeys { "it/${it.key}" } +
            academyStrings("app/android/src/main/res/values-en/strings.xml").mapKeys { "en/${it.key}" }

    @Test
    fun everyLessonDeclaresTheSpecParagraphsItUses() {
        val spec = File(root, "docs/rules/scopa.md").readText()
        val paragraphs = Regex("""(?m)^(\d+\.\d+)\.""").findAll(spec).map { it.groupValues[1] }.toSet()

        for (lesson in AcademyContent.CHAPTERS.flatMap { it.lessons }) {
            assertTrue(lesson.specRefs.isNotEmpty(), "${lesson.id} non dichiara paragrafi della specifica")
            for (ref in lesson.specRefs) {
                assertTrue(ref in paragraphs, "${lesson.id}: il paragrafo $ref non esiste in docs/rules/scopa.md")
            }
        }
    }

    @Test
    fun noLessonTextQuotesAnArticleOfTheRules() {
        val forbidden = Regex("""\bArticol[oi]\b|\bArt\.|\bArticle\b|\bRule 3\b""", RegexOption.IGNORE_CASE)
        val offenders = allAcademyTexts.filterValues { forbidden.containsMatchIn(it) }.keys
        assertEquals(emptySet(), offenders, "Testi che citano articoli inesistenti del regolamento")
    }

    @Test
    fun noLessonTextGivesTheSettebelloMoreThanOnePoint() {
        val wrong = Regex("""(?i)(\+3 Punti|\+3 Points|fino a 3 punti|up to 3 (deck )?points|Triplo Valore|Triple Value)""")
        val offenders = allAcademyTexts.filterValues { wrong.containsMatchIn(it) }.keys
        assertEquals(emptySet(), offenders, "Testi che attribuiscono al Settebello più di un punto")
    }

    @Test
    fun englishTextsUseTheGlossaryNamesForSuitsAndFigures() {
        val offenders = allAcademyTexts.filterKeys { it.startsWith("en/") }
            .filterValues { Regex("""\bBatons?\b|\bHorse\b""").containsMatchIn(it) }.keys
        assertEquals(emptySet(), offenders, "In inglese Bastoni = Clubs e Cavallo = Knight (docs/rules/glossary.md)")
    }
}
