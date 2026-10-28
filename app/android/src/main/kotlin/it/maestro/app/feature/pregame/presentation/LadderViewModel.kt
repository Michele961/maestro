package it.maestro.app.feature.pregame.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.opponents.OpponentProgress
import it.maestro.app.domain.opponents.OpponentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Dove si trova un avversario sulla scala. */
public enum class LadderStatus { LOCKED, NEXT, BEATEN }

/**
 * Un gradino della scala: [opponent], il suo [status] e, se è bloccato, [unlockedBy] cioè l'avversario da battere
 * per sbloccarlo.
 */
public data class LadderEntry(val opponent: Opponent, val status: LadderStatus, val unlockedBy: Opponent?)

/** I cinque avversari dal più debole al più forte con il loro stato; `null` finché il progresso non è letto. */
public data class LadderState(val entries: List<LadderEntry>) {
    /** Il prossimo da battere, `null` a scala completata. */
    val next: Opponent? get() = entries.firstOrNull { it.status == LadderStatus.NEXT }?.opponent

    public companion object {
        public fun of(progress: OpponentProgress): LadderState = LadderState(
            Opponent.entries.mapIndexed { index, opponent ->
                val status = when {
                    progress.isDefeated(opponent) -> LadderStatus.BEATEN
                    progress.isUnlocked(opponent) -> LadderStatus.NEXT
                    else -> LadderStatus.LOCKED
                }
                LadderEntry(opponent, status, unlockedBy = Opponent.entries.getOrNull(index - 1).takeIf { status == LadderStatus.LOCKED })
            },
        )
    }
}

/** Presenta la scala degli avversari (`docs/app/features/pre-game.md` §3). */
public class LadderViewModel(repository: OpponentRepository) : ViewModel() {
    public val state: StateFlow<LadderState> = repository.observeProgress()
        .map(LadderState::of)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LadderState.of(OpponentProgress()))
}
