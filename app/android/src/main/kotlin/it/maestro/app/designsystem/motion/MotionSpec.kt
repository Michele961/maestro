package it.maestro.app.designsystem.motion

import it.maestro.app.domain.settings.GameSpeed
import kotlinx.coroutines.delay
import kotlin.math.roundToLong

/**
 * Tabella unica dei tempi dell'app (`docs/app/game-feel.md` §2, ADR-0015, task F1R-09), in millisecondi a
 * velocità 1×. I valori si tarano nei playtest e si cambiano solo qui.
 */
public enum class MotionToken(public val baseMillis: Long) {
    /** Carta umana dalla mano alla zona di gioco. */
    CARD_TO_PLAY_ZONE(260),

    /** Carta avversaria che si gira. */
    OPPONENT_REVEAL(220),

    /** Pausa per leggere la carta avversaria. */
    OPPONENT_READ_PAUSE(550),

    /** Evidenza delle carte prese. */
    CAPTURE_HIGHLIGHT(420),

    /** Volo verso la pila; ogni carta successiva aggiunge [CAPTURE_FLIGHT_STAGGER]. */
    CAPTURE_FLIGHT(380),

    /** Ritardo del volo di ciascuna carta presa dopo la prima. */
    CAPTURE_FLIGHT_STAGGER(40),

    /** Calata senza presa. */
    DROP_TO_TABLE(240),

    /** Ogni carta distribuita. */
    DEAL_CARD(140),

    /** Mescolata a inizio smazzata. */
    SHUFFLE(400),

    /** Overlay Scopa! del giocatore (saltabile). */
    SWEEP_CELEBRATION(1400),

    /** Overlay Scopa! dell'avversario. */
    SWEEP_OPPONENT(900),

    /** Ogni voce del riepilogo di smazzata che si accende. */
    DEAL_SUMMARY_ROW(260),

    /** Riflessione minima del bot (una sola mossa legale). */
    BOT_THINK_MIN(450),

    /** Riflessione massima del bot (molte scelte possibili). */
    BOT_THINK_MAX(1300),

    /** Targa Vittoria/Sconfitta a inizio sequenza di fine partita (F1R-15). */
    POST_MATCH_PLAQUE(1200),

    /** Ogni passo della sequenza di fine partita (precisione, ricompensa). */
    POST_MATCH_STEP(900),

    /** Passaggio tra schermate. */
    SCREEN_TRANSITION(300),
}

/** Durate effettive dei [MotionToken] alla velocità di gioco scelta. */
public object MotionSpec {

    /** Durata di [token] in millisecondi a [speed]; 0 con [GameSpeed.INSTANT]. */
    public fun millis(token: MotionToken, speed: GameSpeed = GameSpeed.NORMAL): Long =
        scale(token.baseMillis, speed)

    /**
     * Tempo di riflessione del bot in proporzione alle scelte che ha: [legalMoves] = 1 dà il minimo,
     * da [BUSY_CHOICES] in su il massimo (`game-feel.md` P2).
     */
    public fun botThinkMillis(legalMoves: Int, speed: GameSpeed = GameSpeed.NORMAL): Long {
        val min = MotionToken.BOT_THINK_MIN.baseMillis
        val max = MotionToken.BOT_THINK_MAX.baseMillis
        val fraction = ((legalMoves - 1).coerceIn(0, BUSY_CHOICES - 1)).toDouble() / (BUSY_CHOICES - 1)
        return scale((min + (max - min) * fraction).roundToLong(), speed)
    }

    /** Attende [millis]: l'unico punto dell'app, con la regia del tavolo, in cui si aspetta del tempo (ADR-0015). */
    public suspend fun pause(millis: Long) {
        if (millis > 0L) delay(millis)
    }

    /** Numero di mosse legali da cui il bot "pensa" il massimo del tempo. */
    public const val BUSY_CHOICES: Int = 6

    private fun scale(baseMillis: Long, speed: GameSpeed): Long =
        if (speed.multiplier.isInfinite()) 0L else (baseMillis / speed.multiplier).roundToLong()
}
