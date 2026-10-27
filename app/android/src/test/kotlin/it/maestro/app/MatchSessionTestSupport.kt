package it.maestro.app

import it.maestro.app.domain.match.MatchSession
import it.maestro.app.feature.table.presentation.GameInternalState
import it.maestro.game.scopa.ReplayResult
import it.maestro.game.scopa.ScopaState
import it.maestro.game.scopa.replay

/** Stato completo della sessione, ricostruito dal log: solo per i test (ADR-0003). */
fun MatchSession.fullState(): ScopaState = (replay(log) as ReplayResult.Ok).state

/** Stato completo dietro lo stato interno del tavolo: solo per i test. */
val GameInternalState.scopaState: ScopaState get() = session.fullState()
