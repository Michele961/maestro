package it.maestro.app.feature.puzzle.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

/**
 * Formattatore e gestore della condivisione social per la "Sfida del Giorno" (task F2-03).
 *
 * Genera stringhe formattate senza spoiler in stile Wordle ("Maestro Scopa #14 — Risolto al 1° tentativo!")
 * e attiva l'Intent standard di Android [Intent.ACTION_SEND].
 */
public object PuzzleShareFormatter {

    /**
     * Genera la stringa di condivisione senza rivelare le carte o la soluzione.
     */
    public fun formatShareText(
        puzzleNumber: Int,
        attemptsCount: Int,
        solved: Boolean = true,
    ): String {
        return if (solved) {
            val attemptSuffix = when (attemptsCount) {
                1 -> "1°"
                2 -> "2°"
                3 -> "3°"
                else -> "${attemptsCount}°"
            }
            "Maestro Scopa #$puzzleNumber — Risolto al $attemptSuffix tentativo!"
        } else {
            "Maestro Scopa #$puzzleNumber — Ho giocato la Sfida del Giorno!"
        }
    }

    private const val CLIP_LABEL: String = "Maestro Scopa"

    /**
     * Copia il testo negli appunti e avvia l'Intent [Intent.ACTION_SEND] standard di Android.
     */
    public fun shareViaIntent(context: Context, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(CLIP_LABEL, text)
            clipboard?.setPrimaryClip(clip)
        } catch (_: Throwable) {
            // Se gli appunti non sono accessibili, prosegue con l'intent
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooserIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(chooserIntent)
    }
}
