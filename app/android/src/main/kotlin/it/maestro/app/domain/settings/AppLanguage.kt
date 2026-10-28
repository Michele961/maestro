package it.maestro.app.domain.settings

/**
 * Lingua dell'interfaccia scelta dal giocatore (task F1R-31c). [tag] è il valore salvato in
 * [AppSettings.language]: `system` segue il telefono, gli altri sono tag BCP 47.
 */
public enum class AppLanguage(public val tag: String) {
    SYSTEM("system"),
    ITALIAN("it"),
    ENGLISH("en"),
    ;

    public companion object {
        /** Lingua per un valore salvato; un valore sconosciuto vale [SYSTEM]. */
        public fun fromTag(tag: String): AppLanguage = entries.firstOrNull { it.tag == tag } ?: SYSTEM
    }
}
