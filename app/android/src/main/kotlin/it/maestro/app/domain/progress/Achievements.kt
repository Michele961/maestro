package it.maestro.app.domain.progress

import it.maestro.app.domain.opponents.Opponent
import kotlinx.coroutines.flow.Flow

/**
 * Gli obiettivi (achievements) del giocatore (`docs/app/features/missions.md` §3.4). L'[id] è stabile e non cambia
 * mai: è la chiave con cui in futuro si agganciano gli obiettivi di Google Play Games. Nomi e descrizioni stanno
 * nelle risorse (ADR-0007), non qui. Nessun obiettivo dà monete (D19, decisione 5 di `pre-game.md`).
 *
 * Qui ci sono solo gli obiettivi legati a eventi che oggi esistono; quelli dell'Accademia, della Bottega e dei
 * titoli arrivano con le rispettive feature.
 */
public enum class AchievementId(public val id: String) {
    FIRST_WIN("first_win"),
    BEAT_GIGI("beat_gigi"),
    BEAT_NONNA_ROSA("beat_nonna_rosa"),
    BEAT_TONINO("beat_tonino"),
    BEAT_DON_ALFONSO("beat_don_alfonso"),
    BEAT_MAESTRO("beat_maestro"),
    LADDER_COMPLETE("ladder_complete"),
    FIRST_SWEEP("first_sweep"),
    TEN_SWEEPS("ten_sweeps"),
    PERFECT_MATCH("perfect_match"),
    MATCHES_10("matches_10"),
    MATCHES_50("matches_50"),
    MATCHES_100("matches_100"),
    STREAK_7("streak_7"),
    STREAK_30("streak_30"),
    FIRST_SPARRING("first_sparring"),
    PUZZLE_FULL_DAY("puzzle_full_day"),
    PUZZLE_WEEK("puzzle_week"),
    STUDENT("student"),
    ACADEMY_COMPLETE("academy_complete"),
    ;

    public companion object {
        /** L'obiettivo di aver battuto [opponent] per la prima volta. */
        public fun beating(opponent: Opponent): AchievementId = when (opponent) {
            Opponent.GIGI -> BEAT_GIGI
            Opponent.NONNA_ROSA -> BEAT_NONNA_ROSA
            Opponent.TONINO -> BEAT_TONINO
            Opponent.DON_ALFONSO -> BEAT_DON_ALFONSO
            Opponent.MAESTRO -> BEAT_MAESTRO
        }
    }
}

/** Contatori cumulativi che servono a sbloccare alcuni obiettivi. */
public enum class AchievementCounter { SWEEPS, PUZZLES_SOLVED }

/**
 * Ciò che un evento di gioco dice sul giocatore, in ingresso alla regola [achievementsFor]. Un campo con il
 * valore di default significa che l'evento non lo tocca.
 *
 * @property winOverOpponent avversario battuto in una partita completa in Classica, se l'evento è una vittoria.
 * @property firstTimeOverOpponent la vittoria è la prima su quell'avversario.
 * @property defeatedOpponents quanti avversari della scala sono stati battuti, compreso questo.
 * @property perfectMatch partita con precisione almeno 90 e nessun errore grave.
 * @property matchesPlayed partite analizzate finora.
 * @property sweepsTotal scope messe a segno finora.
 * @property streak giorni di serie dopo l'evento.
 * @property sparringPlayed la partita era di Sparring.
 * @property puzzleFullDay l'evento ha chiuso il terzo livello della Sfida del Giorno.
 * @property puzzlesSolved sfide del giorno risolte finora.
 * @property chapterCompleted l'evento ha chiuso un capitolo dell'Accademia.
 * @property academyComplete l'evento ha chiuso l'ultimo capitolo mancante: tutta l'Accademia è fatta.
 */
public data class AchievementFacts(
    val winOverOpponent: Opponent? = null,
    val firstTimeOverOpponent: Boolean = false,
    val defeatedOpponents: Int = 0,
    val perfectMatch: Boolean = false,
    val matchesPlayed: Int = 0,
    val sweepsTotal: Int = 0,
    val streak: Int = 0,
    val sparringPlayed: Boolean = false,
    val puzzleFullDay: Boolean = false,
    val puzzlesSolved: Int = 0,
    val chapterCompleted: Boolean = false,
    val academyComplete: Boolean = false,
)

/** Soglie degli obiettivi a conteggio. */
private const val TEN_SWEEPS_THRESHOLD = 10
private const val PUZZLE_WEEK_THRESHOLD = 7
private const val WEEK_DAYS = 7
private const val MONTH_DAYS = 30

/** Gli obiettivi che i [facts] di un evento soddisfano (anche quelli già ottenuti: li filtra il repository). */
public fun achievementsFor(facts: AchievementFacts): Set<AchievementId> = buildSet {
    if (facts.winOverOpponent != null) {
        add(AchievementId.FIRST_WIN)
        if (facts.firstTimeOverOpponent) add(AchievementId.beating(facts.winOverOpponent))
        if (facts.defeatedOpponents >= Opponent.entries.size) add(AchievementId.LADDER_COMPLETE)
    }
    if (facts.sweepsTotal >= 1) add(AchievementId.FIRST_SWEEP)
    if (facts.sweepsTotal >= TEN_SWEEPS_THRESHOLD) add(AchievementId.TEN_SWEEPS)
    if (facts.perfectMatch) add(AchievementId.PERFECT_MATCH)
    if (facts.matchesPlayed >= 10) add(AchievementId.MATCHES_10)
    if (facts.matchesPlayed >= 50) add(AchievementId.MATCHES_50)
    if (facts.matchesPlayed >= 100) add(AchievementId.MATCHES_100)
    if (facts.streak >= WEEK_DAYS) add(AchievementId.STREAK_7)
    if (facts.streak >= MONTH_DAYS) add(AchievementId.STREAK_30)
    if (facts.sparringPlayed) add(AchievementId.FIRST_SPARRING)
    if (facts.puzzleFullDay) add(AchievementId.PUZZLE_FULL_DAY)
    if (facts.chapterCompleted) add(AchievementId.STUDENT)
    if (facts.academyComplete) add(AchievementId.ACADEMY_COMPLETE)
    if (facts.puzzlesSolved >= PUZZLE_WEEK_THRESHOLD) add(AchievementId.PUZZLE_WEEK)
}

/** Persistenza degli obiettivi ottenuti e dei contatori (oggi in locale, domani anche su Google Play Games). */
public interface AchievementRepository {
    public fun observeUnlocked(): Flow<Set<AchievementId>>

    /** Segna come ottenuti [ids] e restituisce solo quelli che prima non lo erano. */
    public suspend fun unlock(ids: Set<AchievementId>): Set<AchievementId>

    /** Somma [delta] (non negativo) al contatore e restituisce il nuovo valore. */
    public suspend fun addToCounter(counter: AchievementCounter, delta: Int): Int
}
