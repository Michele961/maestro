package it.maestro.app.feature.table.presentation

import it.maestro.app.domain.coach.Chattiness
import it.maestro.app.domain.coach.MatchFacts
import it.maestro.app.domain.coach.Moment
import it.maestro.app.domain.coach.MomentDirector
import it.maestro.app.domain.coach.MomentKind
import it.maestro.app.domain.coach.TurnContext
import it.maestro.app.domain.coach.VoiceComposer
import it.maestro.app.domain.coach.VoiceLine
import it.maestro.app.domain.match.Beat
import it.maestro.app.domain.match.MatchSession
import it.maestro.game.model.Seat
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.GameEvent

/**
 * Il Maestro che parla durante una partita (task F1R-24d): tiene la memoria di una partita e, a ogni battuta,
 * chiede ai fatti, al direttore e al compositore se c'è qualcosa da dire. Solo Sparring lo usa.
 */
internal class MaestroVoice(seed: Long, private val human: Seat, humor: Int = HUMOR) {
    private val director = MomentDirector(rng = SeededRng(seed xor DIRECTOR_SALT))
    private val composer = VoiceComposer(SeededRng(seed xor COMPOSER_SALT), humor = humor)
    private var playerMoves = 0
    private var dealIndex = 0

    /** Saluto a inizio partita (se il livello di chiacchiere lo consente). */
    fun greeting(chattiness: Chattiness, hintEverUsed: Boolean): VoiceLine? {
        director.chattiness = chattiness
        val context = TurnContext(emptyList(), human, playerMoves = 0, dealIndex = 0, everUsedHint = hintEverUsed, matchStart = true)
        return director.consider(context)?.let { composer.compose(it, human) }
    }

    /** Battuta, se c'è, dopo [beat]; [session] è la partita già con quella mossa. */
    fun onBeat(beat: Beat, session: MatchSession, chattiness: Chattiness, hintEverUsed: Boolean): VoiceLine? {
        director.chattiness = chattiness
        if (beat.actor == human) playerMoves++
        val facts = MatchFacts.of(beat.viewAfter, session.legalMovesFor(human), beat)
        val context = TurnContext(facts, human, playerMoves, dealIndex, everUsedHint = hintEverUsed)
        val moment: Moment? = director.consider(context)
        if (beat.events.any { it is GameEvent.DealEnded }) dealIndex++
        return moment?.takeUnless { it.kind == MomentKind.GREETING }?.let { composer.compose(it, human) }
    }

    internal companion object {
        const val DIRECTOR_SALT = 0x4D4F4D45L
        const val COMPOSER_SALT = 0x564F4345L
        const val HUMOR = 1
    }
}
