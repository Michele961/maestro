package it.maestro.app.feature.table.presentation

import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.coach.VoiceArg
import it.maestro.app.domain.coach.VoiceLine
import it.maestro.app.domain.match.MatchSession
import it.maestro.app.domain.match.SessionPlay
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import java.io.File

/**
 * Trascrizione di una partita (task F1R-24f): gioca una partita a seed fisso e scrive cosa avrebbe detto il Maestro,
 * mossa per mossa, con i testi it/en letti da `strings.xml`. Serve a Michele per tarare frequenza e umorismo.
 */
internal object MaestroTranscript {

    private val human = Seat.P1

    /** Una battuta: dopo quale mossa, quale tipo, e il testo risolto. */
    data class Entry(val move: Int, val line: VoiceLine, val text: String)

    /** Testi di una lingua: nome stringa → valore, letti dal file risorse del modulo. */
    class Strings(private val values: Map<String, String>) {
        fun text(line: VoiceLine): String = text(line.phrases)

        /** Testo di una sequenza di frasi, per la revisione e i consigli. */
        fun text(phrases: List<it.maestro.app.domain.coach.Phrase>): String = it.maestro.app.domain.coach.ItalianElision.fix(textOf(phrases))

        private fun textOf(phrases: List<it.maestro.app.domain.coach.Phrase>): String = phrases.joinToString(" ") { phrase ->
            val template = values[phrase.id] ?: "«${phrase.id}?»"
            String.format(template, *phrase.args.map { arg ->
                when (arg) {
                    is VoiceArg.CardArg -> cardName(arg.card)
                    is VoiceArg.NumberArg -> arg.value
                }
            }.toTypedArray())
        }

        private fun cardName(card: Card): String {
            val rank = when (card.rank) {
                Rank.ASSO -> values["game_card_rank_asso"]
                Rank.FANTE -> values["game_card_rank_fante"]
                Rank.CAVALLO -> values["game_card_rank_cavallo"]
                Rank.RE -> values["game_card_rank_re"]
                else -> card.rank.value.toString()
            }
            val suit = when (card.suit) {
                Suit.DENARI -> values["game_suit_denari"]
                Suit.COPPE -> values["game_suit_coppe"]
                Suit.SPADE -> values["game_suit_spade"]
                Suit.BASTONI -> values["game_suit_bastoni"]
            }
            return "$rank $suit"
        }

        companion object {
            fun load(resDir: String): Strings {
                val xml = File(resDir).readText()
                val values = Regex("""<string name="([^"]+)"[^>]*>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
                    .findAll(xml).associate { it.groupValues[1] to unescape(it.groupValues[2]) }
                return Strings(values)
            }

            private fun unescape(raw: String) = raw.replace("\\'", "'").replace("\\\"", "\"").replace("\\n", "\n")
        }
    }

    /** Gioca la partita (il giocatore preferisce le prese, il bot la prima mossa) e raccoglie le battute. */
    fun play(seed: Long, chattiness: Chattiness, humor: Int, strings: Strings): List<Entry> {
        val voice = MaestroVoice(seed, human, humor)
        val entries = mutableListOf<Entry>()
        var session = MatchSession.start(ScopaConfig(targetScore = 11), seed)
        voice.greeting(chattiness, hintEverUsed = false)?.let { entries += Entry(0, it, strings.text(it)) }
        var move = 0
        while (session.phase == DealPhase.PLAYING) {
            val legal = session.legalMovesFor(session.toMove)
            val choice = (if (session.toMove == human) legal.firstOrNull { it.capture.isNotEmpty() } else null) ?: legal.firstOrNull() ?: break
            val applied = session.play(choice, human) as SessionPlay.Applied
            session = applied.session
            move++
            voice.onBeat(applied.beat, session, chattiness, hintEverUsed = false)?.let { entries += Entry(move, it, strings.text(it)) }
        }
        return entries
    }

    /** Scrive la trascrizione in Markdown. */
    fun write(file: File, seeds: List<Long>, chattiness: Chattiness, humor: Int, strings: Strings): List<Entry> {
        val all = mutableListOf<Entry>()
        file.parentFile.mkdirs()
        file.printWriter().use { out ->
            out.println("# Trascrizioni del Maestro — chiacchiere $chattiness, umorismo $humor\n")
            seeds.forEach { seed ->
                val entries = play(seed, chattiness, humor, strings)
                all += entries
                out.println("## Partita seed $seed (${entries.size} battute)\n")
                entries.forEach { out.println("- mossa ${it.move} · ${it.line.kind} · ${it.line.pose}: ${it.text}") }
                out.println()
            }
        }
        return all
    }
}
