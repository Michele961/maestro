package it.maestro.app.domain.coach

import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng

/** Tono con cui il Maestro dice una cosa: guida la scelta delle parole e la posa (`maestro_persona.md` §4). */
public enum class Tone { WARM, MAIEUTIC, IRONIC, THEATRICAL }

/**
 * Di cosa può parlare il Maestro spontaneamente (`docs/app/features/maestro-voice.md` §3.1, task F1R-24b).
 *
 * @property priority più alto vince tra i candidati dello stesso turno.
 * @property cooldownMoves mosse del giocatore prima che lo stesso tipo possa ripetersi.
 * @property urgent può saltare l'intervallo minimo (ma non il tetto per smazzata né lo stesso turno).
 */
public enum class MomentKind(
    public val priority: Int,
    public val cooldownMoves: Int,
    public val tone: Tone,
    public val urgent: Boolean = false,
) {
    WARN_SETTEBELLO_EXPOSED(90, 6, Tone.MAIEUTIC, urgent = true),
    WARN_LOW_TABLE(80, 6, Tone.MAIEUTIC, urgent = true),
    OPPORTUNITY_SWEEP(85, 4, Tone.MAIEUTIC),
    OPPORTUNITY_SETTEBELLO(88, 6, Tone.MAIEUTIC),
    REACT_OPPONENT_SWEEP(70, 4, Tone.THEATRICAL, urgent = true),
    REACT_YOUR_SWEEP(60, 3, Tone.WARM),
    REACT_GIFT(65, 6, Tone.IRONIC),
    REACT_SETTEBELLO_TAKEN(62, 10, Tone.WARM),
    STATUS_SECURED(40, 12, Tone.WARM),
    STATUS_LAST_DEAL(55, 99, Tone.MAIEUTIC),
    STATUS_MATCH_POINT(50, 99, Tone.WARM),
    OFFER_HELP(45, 8, Tone.WARM),
    INTRO_HELP(30, 99, Tone.WARM),
    GREETING(10, 99, Tone.WARM),
    HAND_SETTEBELLO(58, 14, Tone.MAIEUTIC),
    REACT_BIG_CAPTURE(56, 6, Tone.WARM),
    TIP_EMPTY_TABLE(42, 10, Tone.MAIEUTIC),
    STATUS_AHEAD(36, 14, Tone.WARM),
    STATUS_BEHIND(37, 14, Tone.WARM),

    /** Una parola di compagnia quando non c'è altro da dire: colore, il più debole dei momenti. */
    COLOR(5, 10, Tone.IRONIC),
}

/** Un momento scelto: il tipo e i fatti che lo giustificano (la battuta la compone `VoiceComposer`). */
public data class Moment(val kind: MomentKind, val facts: List<Fact>)

/** Quanto parla il Maestro (impostazione «Chiacchiere», dossier §3.4); i valori si tarano con le simulazioni. */
public enum class Chattiness(public val minGapMoves: Int, public val maxPerDeal: Int) {
    TALKATIVE(minGapMoves = 1, maxPerDeal = 5),
    NORMAL(minGapMoves = 2, maxPerDeal = 4),
    QUIET(minGapMoves = 4, maxPerDeal = 2),
    OFF(minGapMoves = Int.MAX_VALUE, maxPerDeal = 0),
}

/** Cosa sa il direttore a ogni turno del giocatore. */
public data class TurnContext(
    val facts: List<Fact>,
    /** Posto del giocatore umano. */
    val humanSeat: Seat,
    /** Mosse del giocatore fatte nella partita finora. */
    val playerMoves: Int,
    /** Smazzata in corso (0, 1, …): il tetto è per smazzata. */
    val dealIndex: Int,
    /** Il giocatore sta esitando (tempo oltre la soglia) senza aver chiesto aiuto. */
    val hesitating: Boolean = false,
    /** Il giocatore ha già usato il consiglio almeno una volta in passato. */
    val everUsedHint: Boolean = false,
    /** Primo turno della partita. */
    val matchStart: Boolean = false,
)

/**
 * Decide se il Maestro parla e di cosa: una voce alla volta, mai ravvicinata (task F1R-24b). È puro e tiene solo
 * la memoria della partita: intervallo minimo, tetto per smazzata, ripetizioni, battute dette.
 */
public class MomentDirector(
    /** Si può cambiare durante la partita (impostazione «Chiacchiere»). */
    public var chattiness: Chattiness = Chattiness.NORMAL,
    private val rng: SeededRng = SeededRng(0L),
) {
    private var lastSpokenMove: Int? = null
    private var spokenInDeal = 0
    private var currentDeal = -1
    private val lastByKind = mutableMapOf<MomentKind, Int>()

    /** Momento da dire in questo turno, o `null` se tace. Va chiamato una sola volta per turno. */
    public fun consider(context: TurnContext): Moment? {
        if (context.dealIndex != currentDeal) {
            currentDeal = context.dealIndex
            spokenInDeal = 0
        }
        if (chattiness == Chattiness.OFF || spokenInDeal >= chattiness.maxPerDeal) return null
        val candidates = candidates(context)
            .filter { cooledDown(it.kind, context.playerMoves) }
            .filter { it.kind.urgent || gapRespected(context.playerMoves) }
        if (candidates.isEmpty()) return null
        // A pari priorità si varia in modo riproducibile (stesso seed, stesse scelte).
        val best = candidates.maxOf { it.kind.priority }
        val top = candidates.filter { it.kind.priority == best }
        val chosen = top[rng.nextInt(top.size)]
        lastSpokenMove = context.playerMoves
        lastByKind[chosen.kind] = context.playerMoves
        spokenInDeal++
        return chosen
    }

    private fun gapRespected(move: Int): Boolean = lastSpokenMove?.let { move - it >= chattiness.minGapMoves } ?: true

    private fun cooledDown(kind: MomentKind, move: Int): Boolean =
        lastByKind[kind]?.let { move - it >= kind.cooldownMoves } ?: true

    private fun candidates(c: TurnContext): List<Moment> = buildList {
        val facts = c.facts
        fun moment(kind: MomentKind, vararg on: Fact) = add(Moment(kind, on.toList()))
        facts.filterIsInstance<Fact.SweepAvailable>().firstOrNull()?.let { moment(MomentKind.OPPORTUNITY_SWEEP, it) }
        facts.filterIsInstance<Fact.SettebelloOnTable>().firstOrNull()?.let {
            moment(if (it.capture != null) MomentKind.OPPORTUNITY_SETTEBELLO else MomentKind.WARN_SETTEBELLO_EXPOSED, it)
        }
        val low = facts.filterIsInstance<Fact.TableSumLow>().firstOrNull()
        val safe = facts.filterIsInstance<Fact.SafeDiscardExists>().firstOrNull()
        if (low != null && Fact.MustDiscard in facts) moment(MomentKind.WARN_LOW_TABLE, *listOfNotNull(low, safe).toTypedArray())
        facts.filterIsInstance<Fact.SweepMade>().forEach {
            moment(if (it.seat == c.humanSeat) MomentKind.REACT_YOUR_SWEEP else MomentKind.REACT_OPPONENT_SWEEP, it)
        }
        if (Fact.OpponentGift in facts) moment(MomentKind.REACT_GIFT, Fact.OpponentGift)
        facts.filterIsInstance<Fact.SettebelloCaptured>().firstOrNull()?.let { moment(MomentKind.REACT_SETTEBELLO_TAKEN, it) }
        (facts.filterIsInstance<Fact.CardsSecured>() + facts.filterIsInstance<Fact.DenariSecured>()).firstOrNull()
            ?.let { moment(MomentKind.STATUS_SECURED, it) }
        if (Fact.LastDeal in facts) moment(MomentKind.STATUS_LAST_DEAL, Fact.LastDeal)
        facts.filterIsInstance<Fact.MatchPoint>().firstOrNull()?.let { moment(MomentKind.STATUS_MATCH_POINT, it) }
        val many = facts.filterIsInstance<Fact.ManyChoices>().firstOrNull()
        if (many != null && c.hesitating) moment(MomentKind.OFFER_HELP, many)
        if (!c.everUsedHint && c.playerMoves >= INTRO_AFTER_MOVES && c.dealIndex == 0) add(Moment(MomentKind.INTRO_HELP, emptyList()))
        if (c.matchStart) add(Moment(MomentKind.GREETING, emptyList()))
        if (Fact.SettebelloInHand in facts) moment(MomentKind.HAND_SETTEBELLO, Fact.SettebelloInHand)
        facts.filterIsInstance<Fact.BigCapture>().firstOrNull { it.seat == c.humanSeat }?.let { moment(MomentKind.REACT_BIG_CAPTURE, it) }
        if (Fact.TableEmpty in facts) moment(MomentKind.TIP_EMPTY_TABLE, Fact.TableEmpty)
        facts.filterIsInstance<Fact.ScoreGap>().firstOrNull { kotlin.math.abs(it.ahead) >= SCORE_GAP_MATTERS }?.let {
            moment(if (it.ahead > 0) MomentKind.STATUS_AHEAD else MomentKind.STATUS_BEHIND, it)
        }
        if (c.playerMoves >= COLOR_AFTER_MOVES) add(Moment(MomentKind.COLOR, emptyList()))
    }

    private companion object {
        const val INTRO_AFTER_MOVES = 2
        const val SCORE_GAP_MATTERS = 2
        const val COLOR_AFTER_MOVES = 3
    }
}
