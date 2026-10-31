package it.maestro.app.domain.story

import it.maestro.app.R
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.season.ChapterState
import it.maestro.app.domain.season.ChapterStatus
import it.maestro.game.training.TrainingFundamental

/** Dove porta il pulsante del «prossimo passo»: la shell lo traduce in una rotta esistente. */
public sealed interface StepAction {
    /** Preparazione di una partita contro [opponent] (si gioca in Classica). */
    public data class PlayOpponent(val opponent: Opponent) : StepAction

    /** Preparazione di una partita di Sparring contro [opponent] con il Check [fundamental] già scelto. */
    public data class Sparring(val opponent: Opponent, val fundamental: TrainingFundamental) : StepAction

    /** La Sfida del giorno. */
    public data object DailyPuzzle : StepAction

    /** L'Accademia. */
    public data object Academy : StepAction

    /** Il Profilo, dove si rivedono le partite («Rivedi analisi»). */
    public data object Profile : StepAction
}

/** Il «prossimo passo» di un capitolo: la frase del Maestro, l'etichetta del pulsante e dove porta. */
public data class NextStep(val textRes: Int, val buttonRes: Int, val action: StepAction)

/**
 * Il prossimo passo del capitolo (`docs/design/stagione-scene.md`): per i capitoli a due passi (9 e 10) dipende da
 * dove si è arrivati. `null` per un capitolo che non è il corrente.
 */
public val ChapterState.nextStep: NextStep?
    get() {
        if (status != ChapterStatus.CURRENT) return null
        return when (chapter.number) {
            1 -> step(R.string.story_ch01_next_text, R.string.story_ch01_next_button, StepAction.PlayOpponent(Opponent.GIGI))
            2 -> step(R.string.story_ch02_next_text, R.string.story_ch02_next_button, StepAction.PlayOpponent(Opponent.GIGI))
            3 -> step(
                R.string.story_ch03_next_text, R.string.story_ch03_next_button,
                StepAction.Sparring(Opponent.GIGI, TrainingFundamental.SEVENS_COUNT),
            )
            4 -> step(R.string.story_ch04_next_text, R.string.story_ch04_next_button, StepAction.DailyPuzzle)
            5 -> step(R.string.story_ch05_next_text, R.string.story_ch05_next_button, StepAction.PlayOpponent(Opponent.NONNA_ROSA))
            6 -> step(R.string.story_ch06_next_text, R.string.story_ch06_next_button, StepAction.Academy)
            7 -> step(R.string.story_ch07_next_text, R.string.story_ch07_next_button, StepAction.Profile)
            8 -> step(R.string.story_ch08_next_text, R.string.story_ch08_next_button, StepAction.PlayOpponent(Opponent.TONINO))
            9 -> when {
                midReached -> step(
                    R.string.story_ch09_next2_text, R.string.story_ch09_next2_button,
                    StepAction.Sparring(Opponent.GIGI, TrainingFundamental.PRIMIERA_LEAD),
                )
                reversed -> step(R.string.story_ch09_next_text_reverse, R.string.story_ch09_next_button_reverse, StepAction.Academy)
                else -> step(R.string.story_ch09_next_text, R.string.story_ch09_next_button, StepAction.Academy)
            }
            10 -> if (midReached) {
                step(R.string.story_ch10_next2_text, R.string.story_ch10_next2_button, StepAction.PlayOpponent(Opponent.MAESTRO))
            } else {
                step(R.string.story_ch10_next_text, R.string.story_ch10_next_button, StepAction.PlayOpponent(Opponent.DON_ALFONSO))
            }
            else -> null
        }
    }

private fun step(text: Int, button: Int, action: StepAction) = NextStep(text, button, action)
