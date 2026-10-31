package it.maestro.app.domain.guide

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaMove

/** I passi della mano d'esordio (`docs/design/stagione-scene.md`, «Mano d'esordio guidata»). */
public enum class GuideStep { PICK, SINGLE, SUM, LAY, SCOPA, SETTE, END }

/** Che cosa dice il Maestro su un passo: l'istruzione, la lode se la mossa è giusta, il consiglio se sbaglia. */
public enum class GuideTone { INSTRUCTION, OK, WRONG }

/**
 * Una battuta della guida. [card] è la carta che l'istruzione nomina (`%1$s` di `guide_single_instr`): è una carta
 * già sul tavolo, quindi pubblica.
 */
public data class GuideLine(val step: GuideStep, val tone: GuideTone, val card: Card? = null)

/**
 * La mano d'esordio guidata (task F1G-02): dominio puro, nessuna regola nuova. Dice che cosa insegnare a ogni turno
 * del giocatore, in base alla sola vista della partita, e se la mossa fatta era quella insegnata.
 *
 * Ogni passo si insegna una volta: lo si ripete finché il giocatore non fa la mossa giusta. Le occasioni rare
 * (Settebello, scopa) hanno la precedenza, poi presa singola, somma e posa.
 */
public object GuidedHand {
    /**
     * Il seme fisso della mano d'esordio: passa dal generatore del progetto (ADR-0009) come ogni altra partita, quindi
     * la mano è identica a ogni avvio.
     */
    public const val SEED: Long = 88L

    /** Il punteggio della partita che contiene la mano guidata: la guida si ferma a fine smazzata, il traguardo non conta. */
    public const val TARGET_SCORE: Int = 11

    private val SETTEBELLO = Card(Rank.SETTE, Suit.DENARI)

    /** L'ordine in cui si cerca il passo da insegnare quando più d'uno è possibile. */
    private val TEACHING_ORDER = listOf(GuideStep.SETTE, GuideStep.SCOPA, GuideStep.SINGLE, GuideStep.SUM, GuideStep.LAY)

    /**
     * Il passo da insegnare ora, o `null` se non c'è più nulla da insegnare. Il primo turno è sempre [GuideStep.PICK];
     * [moveNumber] è il numero di mosse già fatte dal giocatore, [legal] le sue mosse legali, [table] le carte a terra.
     */
    public fun step(taught: Set<GuideStep>, moveNumber: Int, legal: List<ScopaMove>, table: List<Card>): GuideLine? {
        if (legal.isEmpty()) return null
        if (moveNumber == 0 && GuideStep.PICK !in taught) return GuideLine(GuideStep.PICK, GuideTone.INSTRUCTION)
        val step = TEACHING_ORDER.firstOrNull { it !in taught && applies(it, legal, table) } ?: return null
        val card = if (step == GuideStep.SINGLE) legal.firstOrNull { it.isSingle() }?.capture?.first() else null
        return GuideLine(step, GuideTone.INSTRUCTION, card)
    }

    /** `true` se [move], fatta quando si insegnava [step], è quella giusta. PICK e LAY non hanno mosse sbagliate. */
    public fun isRight(step: GuideStep, move: ScopaMove, table: List<Card>): Boolean = when (step) {
        GuideStep.PICK, GuideStep.LAY, GuideStep.END -> true
        GuideStep.SINGLE -> move.isSingle()
        GuideStep.SUM -> move.capture.size >= 2
        GuideStep.SCOPA -> move.capture.size == table.size
        GuideStep.SETTE -> SETTEBELLO in move.capture
    }

    private fun applies(step: GuideStep, legal: List<ScopaMove>, table: List<Card>): Boolean = when (step) {
        GuideStep.SETTE -> legal.any { SETTEBELLO in it.capture }
        GuideStep.SCOPA -> table.isNotEmpty() && legal.any { it.capture.size == table.size }
        GuideStep.SINGLE -> legal.any { it.isSingle() }
        GuideStep.SUM -> legal.any { it.capture.size >= 2 }
        GuideStep.LAY -> legal.all { it.capture.isEmpty() }
        GuideStep.PICK, GuideStep.END -> false
    }

    private fun ScopaMove.isSingle(): Boolean = capture.size == 1 && capture.first().rank == card.rank
}
