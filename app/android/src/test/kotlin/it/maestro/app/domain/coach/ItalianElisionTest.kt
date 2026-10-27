package it.maestro.app.domain.coach

import kotlin.test.Test
import kotlin.test.assertEquals

/** Gli articoli davanti ad «Asso» si elidono; il resto del testo non cambia. */
class ItalianElisionTest {

    @Test
    fun articlesBeforeAssoAreElided() {
        assertEquals("Hai calato l'Asso di coppe.", ItalianElision.fix("Hai calato il Asso di coppe."))
        assertEquals("Gioca con l'Asso di spade.", ItalianElision.fix("Gioca col Asso di spade."))
        assertEquals("Meglio dell'Asso.", ItalianElision.fix("Meglio del Asso."))
        assertEquals("L'Asso di denari.", ItalianElision.fix("Il Asso di denari."))
    }

    @Test
    fun otherCardsAreLeftAlone() {
        assertEquals("Gioca col 7 di denari e il Re di spade.", ItalianElision.fix("Gioca col 7 di denari e il Re di spade."))
    }
}
