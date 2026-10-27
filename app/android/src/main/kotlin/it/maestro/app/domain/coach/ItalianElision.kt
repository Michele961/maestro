package it.maestro.app.domain.coach

/**
 * Elisione dell'articolo davanti ad «Asso» nei testi italiani composti con i nomi delle carte: le frasi scrivono «il %s»,
 * «col %s», «del %s» e il nome «Asso di coppe» ne esce come «il Asso». È l'unica carta che comincia per vocale.
 */
public object ItalianElision {

    private val beforeAsso = Regex("""\b(Il|il|Col|col|Del|del|Dal|dal|Al|al|Nel|nel|Sul|sul)\s+Asso\b""")

    /** [text] con gli articoli davanti ad «Asso» elisi («il Asso» → «l'Asso», «col Asso» → «con l'Asso»). */
    public fun fix(text: String): String = beforeAsso.replace(text) { m ->
        val article = m.groupValues[1]
        val capital = article.first().isUpperCase()
        val elided = when (article.lowercase()) {
            "il" -> "l'"
            "col" -> "con l'"
            "del" -> "dell'"
            "dal" -> "dall'"
            "al" -> "all'"
            "nel" -> "nell'"
            else -> "sull'"
        }
        (if (capital) elided.replaceFirstChar { it.uppercase() } else elided) + "Asso"
    }
}
