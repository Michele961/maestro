package it.maestro.app.domain.story

/** Chi parla in una scena (`docs/design/stagione-scene.md`): il Maestro e i quattro avversari del Circolo. */
public enum class Speaker { MAESTRO, GIGI, ROSA, TONINO, ALFONSO }

/** Argomento di formato di una battuta: un numero (`%1$d`) o un testo da risorsa (`%2$s`). */
public sealed interface BeatArg {
    public data class Number(val value: Int) : BeatArg

    public data class Text(val res: Int) : BeatArg
}

/**
 * Una battuta. [namedTextRes] è la variante con il nome del giocatore (`%1$s`): si usa solo se il nome c'è, altrimenti
 * vale [textRes]. [args] sono gli argomenti di formato di [textRes] (vuoti nelle battute fisse).
 */
public data class Beat(
    val speaker: Speaker,
    val textRes: Int,
    val namedTextRes: Int? = null,
    val args: List<BeatArg> = emptyList(),
)

/**
 * La chiusura di una scena con un pulsante (F1G-16b): dopo l'ultima battuta compare [textRes] (se c'è) e il pulsante
 * [buttonRes]; la scena si chiude solo da lì (o da «Salta»), non più con un tocco.
 */
public data class SceneEnd(val buttonRes: Int, val textRes: Int? = null)

/**
 * Una scena: una sequenza di [beats] con un [id] stabile, che è la chiave con cui si ricorda che è stata vista.
 * [end] è il pulsante finale, se la scena ne ha uno.
 */
public data class StoryScene(val id: String, val beats: List<Beat>, val end: SceneEnd? = null)
