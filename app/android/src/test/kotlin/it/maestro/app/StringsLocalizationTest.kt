package it.maestro.app

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifica la completa aderenza ad ADR-0007:
 * - Ogni risorsa stringa italiana (default) ha il corrispondente in lingua inglese.
 * - Nessuna stringa letterale destinata all'utente è scritta direttamente nel codice UI.
 */
class StringsLocalizationTest {

    @Test
    fun italianAndEnglishStringsAreSynchronized() {
        val rootDir = findProjectRoot()
        val defaultStringsFile = File(rootDir, "app/android/src/main/res/values/strings.xml")
        val enStringsFile = File(rootDir, "app/android/src/main/res/values-en/strings.xml")

        assertTrue(defaultStringsFile.exists(), "values/strings.xml non trovato: ${defaultStringsFile.absolutePath}")
        assertTrue(enStringsFile.exists(), "values-en/strings.xml non trovato: ${enStringsFile.absolutePath}")

        val defaultStrings = parseStringsXml(defaultStringsFile)
        val enStrings = parseStringsXml(enStringsFile)

        assertFalse(defaultStrings.isEmpty(), "values/strings.xml non contiene chiavi stringa")
        assertFalse(enStrings.isEmpty(), "values-en/strings.xml non contiene chiavi stringa")

        assertEquals(
            defaultStrings.keys,
            enStrings.keys,
            "Le chiavi stringa tra italiano ed inglese non coincidono!",
        )

        assertEquals("Maestro – Scopa e Briscola", defaultStrings["app_name"])
        assertEquals("Maestro – Scopa and Briscola", enStrings["app_name"])
    }

    @Test
    fun noHardcodedStringsInComposableScreenCode() {
        val rootDir = findProjectRoot()
        val screensDir = File(rootDir, "app/android/src/main/kotlin/it/maestro/app/ui/screens")
        assertTrue(screensDir.exists(), "Directory screens non trovata: ${screensDir.absolutePath}")

        // Pattern che cerca Text("...") o Text(text = "...") con stringhe letterali anziché stringResource(...)
        val hardcodedTextRegex = Regex("""Text\s*\(\s*(text\s*=\s*)?"([^"]+)"""")

        val violations = mutableListOf<String>()
        screensDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val lines = file.readLines()
            lines.forEachIndexed { index, line ->
                // Esclude righe di commento o preview
                val trimmed = line.trim()
                if (trimmed.startsWith("//") || trimmed.startsWith("*")) return@forEachIndexed

                val match = hardcodedTextRegex.find(line)
                if (match != null) {
                    val literal = match.groupValues[2]
                    // Se non è vuoto o un placeholder ammesso nei test
                    if (literal.isNotBlank()) {
                        violations += "${file.name}:${index + 1} -> trovato testo letterale hardcoded: \"$literal\""
                    }
                }
            }
        }

        assertTrue(
            violations.isEmpty(),
            "Violazione ADR-0007: rilevati testi hardcoded nella UI:\n" + violations.joinToString("\n"),
        )
    }

    private fun parseStringsXml(file: File): Map<String, String> {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(file)
        val stringNodes = doc.getElementsByTagName("string")
        val result = mutableMapOf<String, String>()
        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)
            val nameAttr = node.attributes.getNamedItem("name")?.nodeValue
            if (nameAttr != null) {
                result[nameAttr] = node.textContent.trim()
            }
        }
        return result
    }

    private fun findProjectRoot(): File {
        var current: File? = File(".").canonicalFile
        while (current != null) {
            if (File(current, "settings.gradle.kts").exists()) {
                return current
            }
            current = current.parentFile
        }
        return File(".").canonicalFile
    }
}
