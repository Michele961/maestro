package it.maestro.app.domain.coach

import it.maestro.game.model.Card
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.training.SeatLead

/** Posa del Maestro che accompagna la battuta (`docs/design/maestro_persona.md` §4); la UI la traduce in immagine. */
public enum class Pose { NEUTRAL, THINKING, PLEASED, TRIUMPHANT, ENCOURAGING, WARNING, DESPAIR }

/** Argomento di una frase: la UI lo formatta nella lingua dell'utente (nomi delle carte, numeri). */
public sealed interface VoiceArg {
    public data class CardArg(val card: Card) : VoiceArg
    public data class NumberArg(val value: Int) : VoiceArg
}

/** Una frase del prontuario con i suoi argomenti: [id] è il nome della stringa in `strings.xml` (it/en). */
public data class Phrase(val id: String, val args: List<VoiceArg> = emptyList())

/** Battuta composta, ancora senza testo: frasi in ordine, tono e posa. */
public data class VoiceLine(val kind: MomentKind, val phrases: List<Phrase>, val tone: Tone, val pose: Pose)

/** Variante del prontuario: [humor] da 0 (serio) a 2 (scherzoso); si tara con le trascrizioni (task F1R-24f). */
public data class VoiceVariant(val id: String, val humor: Int)

/**
 * Prontuario del Maestro (`docs/app/features/maestro-voice.md` §3.1): per ogni chiave le varianti disponibili.
 * Gli id sono i nomi delle stringhe `voice_*` in it e en; un test verifica che esistano tutte.
 * Testi in bozza: li approva Michele prima del rilascio (D16).
 */
public object VoiceCatalog {

    private fun v(key: String, vararg humor: Int): List<VoiceVariant> =
        humor.mapIndexed { i, h -> VoiceVariant("voice_${key}_${i + 1}", h) }

    /** Corpo della battuta per chiave. */
    public val bodies: Map<String, List<VoiceVariant>> = mapOf(
        "opportunity_sweep" to v("opportunity_sweep", 0, 1, 2),
        "opportunity_settebello" to v("opportunity_settebello", 0, 1, 2),
        "warn_settebello_exposed" to v("warn_settebello_exposed", 0, 1, 2),
        "warn_low_table_safe" to v("warn_low_table_safe", 0, 1, 2),
        "warn_low_table" to v("warn_low_table", 0, 1),
        "react_your_sweep" to v("react_your_sweep", 0, 1, 2),
        "react_opponent_sweep" to v("react_opponent_sweep", 0, 1, 2),
        "react_gift" to v("react_gift", 0, 1, 2),
        "react_settebello_yours" to v("react_settebello_yours", 0, 1, 2),
        "react_settebello_theirs" to v("react_settebello_theirs", 0, 1, 2),
        "secured_cards_yours" to v("secured_cards_yours", 0, 1),
        "secured_cards_theirs" to v("secured_cards_theirs", 0, 1),
        "secured_denari_yours" to v("secured_denari_yours", 0, 1),
        "secured_denari_theirs" to v("secured_denari_theirs", 0, 1),
        "status_last_deal" to v("status_last_deal", 0, 1, 2),
        "match_point_yours" to v("match_point_yours", 0, 1),
        "match_point_theirs" to v("match_point_theirs", 0, 1),
        "offer_help" to v("offer_help", 0, 1, 2),
        "intro_help" to v("intro_help", 0, 1),
        "greeting" to v("greeting", 0, 1, 2),
    )

    /** Aperture del consiglio su richiesta (task F1R-24e): giocare una carta che prende una carta, più carte, o che cala. */
    public val hintLeads: Map<String, List<VoiceVariant>> = mapOf(
        "hint_take_one" to v("hint_take_one", 0, 1),
        "hint_take_many" to v("hint_take_many", 0, 1),
        "hint_discard" to v("hint_discard", 0, 1),
    )

    /** Ragioni del consiglio: una sola frase per ragione. */
    public val hintReasons: Map<String, List<VoiceVariant>> = mapOf(
        "hint_why_sweep" to v("hint_why_sweep", 0),
        "hint_why_settebello" to v("hint_why_settebello", 0),
        "hint_why_denari" to v("hint_why_denari", 0),
        "hint_why_primiera" to v("hint_why_primiera", 0),
        "hint_why_capture" to v("hint_why_capture", 0),
        "hint_why_safe" to v("hint_why_safe", 0),
        "hint_why_least_bad" to v("hint_why_least_bad", 0),
    )

    /** Frasi della revisione (task F1R-24g): titolo, corpo e regola per ogni [ReviewReason]; id senza numero di variante. */
    public val reviewPhrases: List<VoiceVariant> = ReviewReason.entries.flatMap { reason ->
        val bodies = if (reason == ReviewReason.IGNORED_CAPTURE) listOf("ignored_capture_one", "ignored_capture_many") else listOf(reason.key)
        listOf("voice_review_title_${reason.key}", "voice_review_rule_${reason.key}") + bodies.map { "voice_review_body_$it" }
    }.plus(listOf("primiera", "denari", "last_deal", "score", "played").map { "voice_review_extra_$it" }).map { VoiceVariant(it, humor = 0) }

    /** Code brevi che si possono aggiungere a avvisi e opportunità per dare varietà. */
    public val tails: List<VoiceVariant> = v("tail", 0, 0, 1)
}

/**
 * Compone la battuta di un [Moment]: sceglie la variante (riproducibile col seed), mette gli argomenti dai fatti
 * e, a volte, una coda breve. Non genera testo: restituisce id e argomenti (task F1R-24c).
 *
 * @property humor quanto è scherzoso il Maestro: sono ammesse le varianti con `humor` fino a questo livello.
 */
public class VoiceComposer(
    private val rng: SeededRng = SeededRng(0L),
    private val humor: Int = 1,
) {
    private val lastVariant = mutableMapOf<String, String>()

    /** [VoiceLine] per [moment], vista dal giocatore seduto in [human]. */
    public fun compose(moment: Moment, human: Seat = Seat.P1): VoiceLine {
        val (key, args) = keyAndArgs(moment, human)
        val variants = VoiceCatalog.bodies.getValue(key).filter { it.humor <= humor }.ifEmpty { VoiceCatalog.bodies.getValue(key).take(1) }
        val fresh = variants.filter { it.id != lastVariant[key] }.ifEmpty { variants }
        val body = fresh[rng.nextInt(fresh.size)]
        lastVariant[key] = body.id
        val phrases = mutableListOf(Phrase(body.id, args))
        if (wantsTail(moment.kind) && rng.nextInt(TAIL_ONE_IN) == 0) {
            val tails = VoiceCatalog.tails.filter { it.humor <= humor }
            phrases += Phrase(tails[rng.nextInt(tails.size)].id)
        }
        return VoiceLine(moment.kind, phrases, moment.kind.tone, poseFor(moment.kind, moment, human))
    }

    private fun wantsTail(kind: MomentKind): Boolean =
        kind == MomentKind.OPPORTUNITY_SWEEP || kind == MomentKind.OPPORTUNITY_SETTEBELLO || kind == MomentKind.WARN_LOW_TABLE

    private fun keyAndArgs(moment: Moment, human: Seat): Pair<String, List<VoiceArg>> {
        val facts = moment.facts
        return when (moment.kind) {
            MomentKind.OPPORTUNITY_SWEEP -> {
                val move = facts.filterIsInstance<Fact.SweepAvailable>().first().move
                "opportunity_sweep" to listOf(VoiceArg.CardArg(move.card))
            }
            MomentKind.OPPORTUNITY_SETTEBELLO -> {
                val move = facts.filterIsInstance<Fact.SettebelloOnTable>().first().capture!!
                "opportunity_settebello" to listOf(VoiceArg.CardArg(move.card))
            }
            MomentKind.WARN_SETTEBELLO_EXPOSED -> "warn_settebello_exposed" to emptyList()
            MomentKind.WARN_LOW_TABLE -> {
                val low = facts.filterIsInstance<Fact.TableSumLow>().first()
                val safe = facts.filterIsInstance<Fact.SafeDiscardExists>().firstOrNull()
                if (safe != null) {
                    "warn_low_table_safe" to listOf(VoiceArg.NumberArg(low.sum), VoiceArg.CardArg(safe.move.card))
                } else {
                    "warn_low_table" to listOf(VoiceArg.NumberArg(low.sum))
                }
            }
            MomentKind.REACT_YOUR_SWEEP -> "react_your_sweep" to emptyList()
            MomentKind.REACT_OPPONENT_SWEEP -> "react_opponent_sweep" to emptyList()
            MomentKind.REACT_GIFT -> "react_gift" to emptyList()
            MomentKind.REACT_SETTEBELLO_TAKEN -> {
                val taker = facts.filterIsInstance<Fact.SettebelloCaptured>().first().seat
                (if (taker == human) "react_settebello_yours" else "react_settebello_theirs") to emptyList()
            }
            MomentKind.STATUS_SECURED -> {
                val cards = facts.filterIsInstance<Fact.CardsSecured>().firstOrNull()
                val denari = facts.filterIsInstance<Fact.DenariSecured>().firstOrNull()
                val (what, owner) = if (cards != null) "cards" to cards.owner else "denari" to denari!!.owner
                "secured_${what}_${if (owner == SeatLead.HUMAN) "yours" else "theirs"}" to emptyList()
            }
            MomentKind.STATUS_LAST_DEAL -> "status_last_deal" to emptyList()
            MomentKind.STATUS_MATCH_POINT -> {
                val owner = facts.filterIsInstance<Fact.MatchPoint>().first().owner
                "match_point_${if (owner == SeatLead.HUMAN) "yours" else "theirs"}" to emptyList()
            }
            MomentKind.OFFER_HELP -> "offer_help" to emptyList()
            MomentKind.INTRO_HELP -> "intro_help" to emptyList()
            MomentKind.GREETING -> "greeting" to emptyList()
        }
    }

    private fun poseFor(kind: MomentKind, moment: Moment, human: Seat): Pose = when (kind) {
        MomentKind.WARN_SETTEBELLO_EXPOSED, MomentKind.WARN_LOW_TABLE -> Pose.WARNING
        MomentKind.OPPORTUNITY_SWEEP, MomentKind.OPPORTUNITY_SETTEBELLO, MomentKind.REACT_GIFT -> Pose.PLEASED
        MomentKind.REACT_YOUR_SWEEP -> Pose.TRIUMPHANT
        MomentKind.REACT_OPPONENT_SWEEP -> Pose.DESPAIR
        MomentKind.REACT_SETTEBELLO_TAKEN ->
            if (moment.facts.filterIsInstance<Fact.SettebelloCaptured>().first().seat == human) Pose.TRIUMPHANT else Pose.ENCOURAGING
        MomentKind.STATUS_SECURED, MomentKind.STATUS_LAST_DEAL, MomentKind.STATUS_MATCH_POINT -> Pose.NEUTRAL
        MomentKind.OFFER_HELP, MomentKind.INTRO_HELP -> Pose.ENCOURAGING
        MomentKind.GREETING -> Pose.PLEASED
    }

    private companion object {
        const val TAIL_ONE_IN = 3
    }
}
