package it.maestro.app.feature.table.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import it.maestro.app.R
import it.maestro.app.domain.coach.Phrase
import it.maestro.app.domain.coach.Pose
import it.maestro.app.domain.coach.VoiceArg
import it.maestro.app.domain.coach.VoiceLine

/**
 * Testo di una [VoiceLine] nella lingua dell'utente: il dominio dà id e argomenti, qui si pescano le stringhe
 * `voice_*` (it/en) e si formattano carte e numeri (ADR-0007).
 */
@Composable
internal fun voiceText(line: VoiceLine): String {
    val context = LocalContext.current
    val cardName = rememberCardFormatter()
    return line.phrases.joinToString(" ") { phrase ->
        val id = context.resources.getIdentifier(phrase.id, "string", context.packageName)
        if (id == 0) "" else context.getString(id, *phrase.formatArgs(cardName))
    }.trim()
}

private fun Phrase.formatArgs(cardName: (it.maestro.game.model.Card) -> String): Array<Any> =
    args.map { arg ->
        when (arg) {
            is VoiceArg.CardArg -> cardName(arg.card)
            is VoiceArg.NumberArg -> arg.value
        }
    }.toTypedArray()

/** Ritratto del Maestro per la [Pose] della battuta. */
@DrawableRes
internal fun Pose.portraitRes(): Int = when (this) {
    Pose.NEUTRAL -> R.drawable.maestro_neutral
    Pose.THINKING -> R.drawable.maestro_thinking
    Pose.PLEASED -> R.drawable.maestro_pleased
    Pose.TRIUMPHANT -> R.drawable.maestro_triumphant
    Pose.ENCOURAGING -> R.drawable.maestro_encouraging
    Pose.WARNING -> R.drawable.maestro_warning
    Pose.DESPAIR -> R.drawable.maestro_despair
}
