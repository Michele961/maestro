package it.maestro.app.shell

import it.maestro.app.domain.story.StepAction

/**
 * La rotta esistente a cui porta il pulsante del «prossimo passo» della Scalata (task F1G-16a): partita contro un
 * avversario e Sparring con Check passano dalla preparazione, gli altri passi aprono la loro schermata.
 */
internal fun StepAction.toRoute(): Route = when (this) {
    is StepAction.PlayOpponent -> Route.PreGame(opponent.name)
    is StepAction.Sparring -> Route.PreGame(opponent.name, fundamental.name)
    StepAction.DailyPuzzle -> Route.DailyPuzzle
    StepAction.Academy -> Route.Academy
    StepAction.Profile -> Route.Profile
    StepAction.Play -> Route.Ladder()
}
