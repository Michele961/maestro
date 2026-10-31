package it.maestro.app.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.home.HomeFeature
import it.maestro.app.domain.home.HomeFeaturesRepository
import it.maestro.app.domain.home.HomeSignals
import it.maestro.app.domain.home.HomeUnlocks
import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.app.domain.opponents.OpponentRepository
import it.maestro.app.domain.retention.RetentionRepository
import it.maestro.app.domain.time.Clock
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.wallet.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Cosa la Home mostra: le funzioni [visible] e la [introduction] che il Maestro dice in questa visita (una sola per
 * volta, `null` se non c'è nulla da presentare).
 *
 * Prima che i dati siano letti non si mostra niente di opzionale ([LOADING]): meglio una Home spoglia per un
 * istante che una Home piena che poi si svuota.
 */
public data class HomeFeaturesUi(val visible: Set<HomeFeature>, val introduction: HomeFeature?) {
    public companion object {
        public val LOADING: HomeFeaturesUi = HomeFeaturesUi(emptySet(), null)
    }
}

/**
 * Calcola le funzioni della Home dai segnali già esistenti, le salva in «funzioni presentate» e sceglie la frase di
 * presentazione (task F1G-03). La frase scelta resta per tutta la visita e si segna come detta subito: compare una
 * sola volta.
 */
public class HomeFeaturesViewModel(
    private val repository: HomeFeaturesRepository,
    mastery: MasteryRepository,
    opponents: OpponentRepository,
    retention: RetentionRepository,
    /** `true` quando la Sfida del giorno è stata risolta almeno una volta (la costruisce il guscio dai dati). */
    dailyPuzzleSolved: Flow<Boolean>,
    /** Lezioni dell'Accademia completate (la costruisce il guscio dai dati). */
    academyLessonsDone: Flow<Int>,
    wallet: WalletRepository,
    clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeFeaturesUi.LOADING)

    /** Stato osservato dalla Home. */
    public val state: StateFlow<HomeFeaturesUi> = _state.asStateFlow()

    private var introduction: HomeFeature? = null

    init {
        val today = clock.today()
        val signals = combine(
            mastery.observeMasteryProfile(),
            mastery.observeMatchHistory(),
            opponents.observeProgress(),
            dailyPuzzleSolved,
            retention.observeStreak(today),
        ) { profile, history, progress, puzzleSolved, streak ->
            HomeSignals(
                matchesPlayed = profile.matchesPlayed,
                matchesWon = history.count { it.isWin },
                // I Check si giocano nello Sparring: ogni partita di Sparring registrata ne porta con sé.
                checkPlayed = history.any { it.sparringObjective != null },
                dailyPuzzleSolved = puzzleSolved,
                opponentsDefeated = progress.defeated.size,
                bestStreak = streak.bestStreak,
            )
        }
        val rest = combine(academyLessonsDone, wallet.observeTransactions()) { lessons, transactions ->
            lessons to transactions.any { it.isCredit && it.reason != TransactionReason.WELCOME_BONUS }
        }
        viewModelScope.launch {
            combine(signals, rest) { base, (lessons, reward) ->
                base.copy(academyLessonsDone = lessons, rewardReceived = reward)
            }.collect { evaluate(it) }
        }
    }

    private suspend fun evaluate(signals: HomeSignals) {
        val stored = repository.load()
        var next = HomeUnlocks.advance(stored, signals)
        if (introduction == null) {
            introduction = next.pendingIntroduction
            introduction?.let { next = next.withIntroduced(it) }
        }
        if (next != stored) repository.save(next)
        _state.value = HomeFeaturesUi(next.revealed, introduction)
    }
}
