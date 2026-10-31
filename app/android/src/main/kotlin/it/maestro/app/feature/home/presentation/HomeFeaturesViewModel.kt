package it.maestro.app.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.guide.GuideRepository
import it.maestro.app.domain.guide.GuidedHandStatus
import it.maestro.app.domain.home.DayClosing
import it.maestro.app.domain.home.DayClosingSignals
import it.maestro.app.domain.home.HomeFeature
import it.maestro.app.domain.home.GuideSignals
import it.maestro.app.domain.home.HomeFeaturesRepository
import it.maestro.app.domain.home.HomeGuide
import it.maestro.app.domain.home.HomeGuides
import it.maestro.app.domain.home.HomeSignals
import it.maestro.app.domain.home.HomeUnlocks
import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.app.domain.opponents.OpponentRepository
import it.maestro.app.domain.retention.RetentionRepository
import it.maestro.app.domain.season.SeasonProgress
import it.maestro.app.domain.story.StoryDirector
import it.maestro.app.domain.story.StoryRepository
import it.maestro.app.domain.story.StoryScene
import it.maestro.app.domain.time.Clock
import it.maestro.app.domain.wallet.TransactionReason
import it.maestro.app.domain.wallet.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Cosa la Home mostra: le funzioni [visible] e la [introduction] che il Maestro dice in questa visita (una sola per
 * volta, `null` se non c'è nulla da presentare).
 *
 * [guide] è il suggerimento «cosa premere» e [returnScene] il saluto di rientro dopo giorni di assenza (F1G-16a).
 *
 * [closing] è `true` quando il Maestro chiude il giorno (F1G-12): sfida, partita e missioni di oggi fatte, detto una
 * sola volta al giorno.
 *
 * [firstLaunch] è `true` per chi è nuovo e non ha ancora fatto né saltato la mano d'esordio: la shell mostra il saluto del
 * Maestro (F1G-01). [loaded] è `false` finché i dati non sono letti.
 *
 * Prima che i dati siano letti non si mostra niente di opzionale ([LOADING]): meglio una Home spoglia per un
 * istante che una Home piena che poi si svuota.
 */
public data class HomeFeaturesUi(
    val visible: Set<HomeFeature>,
    val introduction: HomeFeature?,
    val guide: HomeGuide? = null,
    val returnScene: StoryScene? = null,
    val closing: Boolean = false,
    val firstLaunch: Boolean = false,
    val loaded: Boolean = true,
) {
    public companion object {
        public val LOADING: HomeFeaturesUi = HomeFeaturesUi(emptySet(), null, loaded = false)
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
    /** La Scalata (F1G-16a): per il suggerimento «apri La Scalata» e il saluto di rientro. */
    seasonProgress: Flow<SeasonProgress> = flowOf(SeasonProgress(emptyList())),
    /** `true` quando la Sfida del giorno di oggi è risolta. */
    puzzleSolvedToday: Flow<Boolean> = flowOf(false),
    /** Le scene viste, per non ripetere il saluto di rientro; senza, il saluto non compare. */
    private val story: StoryRepository? = null,
    /** La mano d'esordio guidata (F1G-16b): decide il primo avvio; senza, il primo avvio non compare. */
    private val guide: GuideRepository? = null,
    /** `true` quando la Partita del giorno di oggi è stata giocata (F1G-12). */
    dailyMatchPlayedToday: Flow<Boolean> = flowOf(false),
) : ViewModel() {

    private val _state = MutableStateFlow(HomeFeaturesUi.LOADING)

    /** Stato osservato dalla Home. */
    public val state: StateFlow<HomeFeaturesUi> = _state.asStateFlow()

    private var introduction: HomeFeature? = null

    /** La chiusura del giorno scelta per questa visita: resta per tutta la visita, come la presentazione. */
    private var closing: Boolean = false

    /** Oltre ai segnali delle funzioni: la Scalata, la Sfida di oggi, le scene viste, l'ultimo giorno giocato. */
    private data class GuideInputs(
        val season: SeasonProgress,
        val solvedToday: Boolean,
        val seen: Set<String>,
        val lastPlayed: String,
        val hand: GuidedHandStatus,
        val dayDone: DayClosingSignals,
    )

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
        val dayDone = combine(puzzleSolvedToday, dailyMatchPlayedToday, retention.observeDailyMissions(today)) { puzzle, match, missions ->
            DayClosingSignals(puzzle, match, missions.allCompleted)
        }
        val guideBase = combine(
            seasonProgress, puzzleSolvedToday, story?.observeSeen() ?: flowOf(emptySet()), retention.observeStreak(today),
            guide?.observe()?.map { it.hand } ?: flowOf(GuidedHandStatus.SKIPPED),
        ) { season, solved, seen, streak, hand ->
            GuideInputs(season, solved, seen, streak.lastPlayedDate, hand, DayClosingSignals(false, false, false))
        }
        val guideInputs = combine(guideBase, dayDone) { base, day -> base.copy(dayDone = day) }
        viewModelScope.launch {
            combine(signals, rest, guideInputs) { base, (lessons, reward), inputs ->
                base.copy(academyLessonsDone = lessons, rewardReceived = reward) to inputs
            }.collect { (home, inputs) -> evaluate(home, inputs, today) }
        }
    }

    /** Il saluto di rientro è stato visto o saltato: non si ripete per questa assenza. */
    public fun onReturnSceneDone(sceneId: String) {
        viewModelScope.launch { story?.markSeen(sceneId) }
    }

    private suspend fun evaluate(signals: HomeSignals, inputs: GuideInputs, today: LocalDate) {
        val stored = repository.load()
        var next = HomeUnlocks.advance(stored, signals)
        if (introduction == null) {
            introduction = next.pendingIntroduction
            introduction?.let { next = next.withIntroduced(it) }
        }
        // La chiusura non si somma a una presentazione: se oggi parla l'altra, resta per la prossima visita.
        if (!closing && introduction == null && DayClosing.isDue(inputs.dayDone, next.revealed, next.closingDay, today)) {
            closing = true
            next = next.withClosingShown(today.toString())
        }
        if (next != stored) repository.save(next)
        val guide = HomeGuides.choose(
            GuideSignals(
                matchesPlayed = signals.matchesPlayed,
                matchesWon = signals.matchesWon,
                puzzleEverSolved = signals.dailyPuzzleSolved,
                puzzleSolvedToday = inputs.solvedToday,
                chapterToPlay = inputs.season.current != null,
            ),
            next.revealed,
        )
        val lastPlayed = runCatching { LocalDate.parse(inputs.lastPlayed) }.getOrNull()
        val returnScene = if (story == null) null else StoryDirector.returnGreeting(inputs.season, lastPlayed, today, inputs.seen)
        // Chi aveva già giocato prima di questa funzione non rivede il primo avvio: la guida conta come saltata.
        val newcomer = inputs.hand == GuidedHandStatus.NOT_DONE && !signals.hasActivity
        if (inputs.hand == GuidedHandStatus.NOT_DONE && signals.hasActivity) this.guide?.setHand(GuidedHandStatus.SKIPPED)
        _state.value = HomeFeaturesUi(next.revealed, introduction, guide, returnScene, closing, firstLaunch = newcomer)
    }
}
