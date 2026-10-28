package it.maestro.game.training

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaView
import it.maestro.game.scopa.primieraPoints

/**
 * Obiettivi di allenamento (fondamentali) selezionabili per i Check del Maestro (task F1-12, vision §5.A).
 */
public enum class TrainingFundamental {
    NONE,
    SEVENS_COUNT,
    DENARI_COUNT,
    PRIMIERA_LEAD,
    SETTEBELLO_LOCATION,
    CARDS_LEAD,
}

/**
 * Esito di un confronto tra giocatori per primiera o conteggio carte.
 */
public enum class SeatLead {
    HUMAN,
    OPPONENT,
    TIE,
}

/**
 * Posizione o stato del Settebello nella vista del giocatore.
 */
public enum class SettebelloLocation {
    CAPTURED_BY_ME,
    CAPTURED_BY_OPPONENT,
    IN_PLAY_OR_UNKNOWN,
}

/**
 * Opzione di risposta a una domanda di allenamento.
 */
public sealed interface TrainingOption {
    public data class IntCount(val count: Int) : TrainingOption
    public data class Lead(val lead: SeatLead) : TrainingOption
    public data class Location(val location: SettebelloLocation) : TrainingOption
}

/**
 * Domanda a scelta multipla calcolata in modo puro a partire dalla vista di gioco (ADR-0005, task F1-12).
 */
public data class TrainingQuestion(
    public val fundamental: TrainingFundamental,
    public val options: List<TrainingOption>,
    public val correctIndex: Int,
    public val explanationValues: List<Int> = emptyList(),
)

/**
 * Restituisce l'insieme di tutte le carte che sono già passate sul tavolo o nelle prese,
 * escludendo le carte ancora segrete nel mazzo coperto o nella mano dell'avversario
 * e la mano attuale del giocatore (che non è ancora stata giocata sul tavolo).
 */
public fun ScopaView.passedCards(): Set<Card> {
    val passed = ArrayList<Card>(playedCards.size + table.size + 40)
    passed.addAll(playedCards)
    passed.addAll(table)
    capturedBy.values.forEach { passed.addAll(it) }
    return passed.toSet()
}

/**
 * Calcola il numero di Sette già passati (0..4) a partire dalla vista.
 */
public fun countPassedSevens(view: ScopaView): Int =
    view.passedCards().count { it.rank == Rank.SETTE }

/**
 * Calcola il numero di carte di Denari già passate (0..10) a partire dalla vista.
 */
public fun countPassedDenari(view: ScopaView): Int =
    view.passedCards().count { it.suit == Suit.DENARI }

/**
 * Calcola la somma di primiera parziale per un insieme di carte catturate.
 */
public fun calculatePartialPrimiera(cards: List<Card>): Int =
    Suit.entries.sumOf { suit ->
        cards.filter { it.suit == suit }.maxOfOrNull { it.rank.primieraPoints } ?: 0
    }

/**
 * Determina chi è attualmente in vantaggio nella primiera parziale a partire dalla vista.
 */
public fun determinePrimieraLead(view: ScopaView): SeatLead {
    val myCards = view.capturedBy[view.seat] ?: emptyList()
    val oppCards = view.capturedBy[view.seat.other()] ?: emptyList()
    val myScore = calculatePartialPrimiera(myCards)
    val oppScore = calculatePartialPrimiera(oppCards)
    return when {
        myScore > oppScore -> SeatLead.HUMAN
        myScore < oppScore -> SeatLead.OPPONENT
        else -> SeatLead.TIE
    }
}

/**
 * Determina la posizione del Settebello (7 di Denari) a partire dalla vista.
 */
public fun determineSettebelloLocation(view: ScopaView): SettebelloLocation {
    val settebello = Card(Rank.SETTE, Suit.DENARI)
    val myCaptures = view.capturedBy[view.seat] ?: emptyList()
    val oppCaptures = view.capturedBy[view.seat.other()] ?: emptyList()
    return when {
        settebello in myCaptures -> SettebelloLocation.CAPTURED_BY_ME
        settebello in oppCaptures -> SettebelloLocation.CAPTURED_BY_OPPONENT
        else -> SettebelloLocation.IN_PLAY_OR_UNKNOWN
    }
}

/**
 * Determina chi è attualmente in vantaggio per numero di carte catturate.
 */
public fun determineCardsLead(view: ScopaView): SeatLead {
    val myCount = view.capturedBy[view.seat]?.size ?: 0
    val oppCount = view.capturedBy[view.seat.other()]?.size ?: 0
    return when {
        myCount > oppCount -> SeatLead.HUMAN
        myCount < oppCount -> SeatLead.OPPONENT
        else -> SeatLead.TIE
    }
}

/** Carte giocate prima delle quali il Check non scatta mai: serve abbastanza storia da ricordare (F1R-25f). */
public const val TRAINING_MIN_CARDS: Int = 12

/** Carte giocate oltre le quali il Check scatta comunque, così ogni smazzata ha il suo allenamento (F1R-25f). */
public const val TRAINING_LATEST_CARDS: Int = 24

/**
 * Riconosce se è il momento giusto per il Check del [fundamental] (`docs/app/features/coach-in-game.md` §6):
 * mai prima di [TRAINING_MIN_CARDS] carte giocate, sempre da [TRAINING_LATEST_CARDS] in poi, e nel mezzo solo
 * quando la posizione è significativa per quell'obiettivo ([isGoodTrainingMoment]).
 */
public fun isCriticalTrainingMoment(fundamental: TrainingFundamental, view: ScopaView): Boolean {
    if (fundamental == TrainingFundamental.NONE) return false
    val cardsPlayed = view.playedCards.size
    if (cardsPlayed < TRAINING_MIN_CARDS) return false
    if (cardsPlayed >= TRAINING_LATEST_CARDS) return true
    return isGoodTrainingMoment(fundamental, view)
}

/**
 * La regola del momento «buono» di ogni obiettivo: ciascun obiettivo ha la sua, così una variante o un nuovo
 * obiettivo aggiunge un ramo qui (la [ScopaView] porta con sé la configurazione) senza toccare il resto.
 */
private fun isGoodTrainingMoment(fundamental: TrainingFundamental, view: ScopaView): Boolean {
    val lastCard = view.playedCards.lastOrNull()
    return when (fundamental) {
        TrainingFundamental.NONE -> false
        // Con meno di due Sette passati la risposta è quasi ovvia.
        TrainingFundamental.SEVENS_COUNT -> countPassedSevens(view) >= 2
        TrainingFundamental.DENARI_COUNT ->
            countPassedDenari(view) >= 4 ||
                (lastCard?.suit == Suit.DENARI && (lastCard.rank == Rank.SETTE || lastCard.rank == Rank.SEI))
        TrainingFundamental.PRIMIERA_LEAD -> {
            val myPrimiera = calculatePartialPrimiera(view.capturedBy[view.seat] ?: emptyList())
            val oppPrimiera = calculatePartialPrimiera(view.capturedBy[view.seat.other()] ?: emptyList())
            kotlin.math.abs(myPrimiera - oppPrimiera) <= 4 && (myPrimiera > 30 || oppPrimiera > 30)
        }
        // Finché il Settebello è in mano a qualcuno la risposta è sempre «ancora in gioco».
        TrainingFundamental.SETTEBELLO_LOCATION ->
            determineSettebelloLocation(view) != SettebelloLocation.IN_PLAY_OR_UNKNOWN ||
                view.table.any { it.suit == Suit.DENARI && it.rank == Rank.SETTE }
        TrainingFundamental.CARDS_LEAD ->
            kotlin.math.abs((view.capturedBy[view.seat]?.size ?: 0) - (view.capturedBy[view.seat.other()]?.size ?: 0)) <= 2
    }
}

/**
 * Genera la domanda a scelta multipla per il fondamentale dato, calcolata dalla vista.
 */
public fun buildTrainingQuestion(fundamental: TrainingFundamental, view: ScopaView): TrainingQuestion {
    return when (fundamental) {
        TrainingFundamental.NONE -> throw IllegalArgumentException("Nessun fondamentale selezionato")
        TrainingFundamental.SEVENS_COUNT -> {
            val correct = countPassedSevens(view)
            // Opzioni 0, 1, 2, 3 o centrate attorno a correct (0..4)
            val allOptions = listOf(0, 1, 2, 3, 4)
            val subOptions = when {
                correct <= 1 -> listOf(0, 1, 2, 3)
                correct >= 3 -> listOf(1, 2, 3, 4)
                else -> listOf(0, 1, 2, 3)
            }
            val options = subOptions.map { TrainingOption.IntCount(it) }
            val correctIdx = subOptions.indexOf(correct)
            TrainingQuestion(
                fundamental = fundamental,
                options = options,
                correctIndex = if (correctIdx >= 0) correctIdx else 0,
                explanationValues = listOf(correct),
            )
        }
        TrainingFundamental.DENARI_COUNT -> {
            val correct = countPassedDenari(view)
            // Genera 4 opzioni plausibili attorno al valore esatto
            val start = (correct - 1).coerceIn(0, 7)
            val subOptions = listOf(start, start + 1, start + 2, start + 3)
            val options = subOptions.map { TrainingOption.IntCount(it) }
            val correctIdx = subOptions.indexOf(correct)
            TrainingQuestion(
                fundamental = fundamental,
                options = options,
                correctIndex = if (correctIdx >= 0) correctIdx else 0,
                explanationValues = listOf(correct),
            )
        }
        TrainingFundamental.PRIMIERA_LEAD -> {
            val lead = determinePrimieraLead(view)
            val myCards = view.capturedBy[view.seat] ?: emptyList()
            val oppCards = view.capturedBy[view.seat.other()] ?: emptyList()
            val options = listOf(
                TrainingOption.Lead(SeatLead.HUMAN),
                TrainingOption.Lead(SeatLead.TIE),
                TrainingOption.Lead(SeatLead.OPPONENT),
            )
            val correctIdx = options.indexOfFirst { (it as TrainingOption.Lead).lead == lead }
            TrainingQuestion(
                fundamental = fundamental,
                options = options,
                correctIndex = correctIdx,
                explanationValues = listOf(calculatePartialPrimiera(myCards), calculatePartialPrimiera(oppCards)),
            )
        }
        TrainingFundamental.SETTEBELLO_LOCATION -> {
            val loc = determineSettebelloLocation(view)
            val options = listOf(
                TrainingOption.Location(SettebelloLocation.CAPTURED_BY_ME),
                TrainingOption.Location(SettebelloLocation.IN_PLAY_OR_UNKNOWN),
                TrainingOption.Location(SettebelloLocation.CAPTURED_BY_OPPONENT),
            )
            val correctIdx = options.indexOfFirst { (it as TrainingOption.Location).location == loc }
            TrainingQuestion(
                fundamental = fundamental,
                options = options,
                correctIndex = correctIdx,
            )
        }
        TrainingFundamental.CARDS_LEAD -> {
            val lead = determineCardsLead(view)
            val myCount = view.capturedBy[view.seat]?.size ?: 0
            val oppCount = view.capturedBy[view.seat.other()]?.size ?: 0
            val options = listOf(
                TrainingOption.Lead(SeatLead.HUMAN),
                TrainingOption.Lead(SeatLead.TIE),
                TrainingOption.Lead(SeatLead.OPPONENT),
            )
            val correctIdx = options.indexOfFirst { (it as TrainingOption.Lead).lead == lead }
            TrainingQuestion(
                fundamental = fundamental,
                options = options,
                correctIndex = correctIdx,
                explanationValues = listOf(myCount, oppCount),
            )
        }
    }
}
