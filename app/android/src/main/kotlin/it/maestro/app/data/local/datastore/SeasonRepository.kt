package it.maestro.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.maestro.app.data.local.academy.AcademyContent
import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.app.domain.opponents.OpponentRepository
import it.maestro.app.domain.season.SeasonFacts
import it.maestro.app.domain.season.SeasonProgress
import it.maestro.app.domain.season.SeasonRepository
import it.maestro.app.domain.time.Clock
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * La Scalata su DataStore Preferences: salva solo i capitoli con ricompensa riscossa; tutto il resto
 * ([facts]) si legge dagli altri repository (ADR-0012).
 */
public class DataStoreSeasonRepository(
    private val dataStore: DataStore<Preferences>,
    private val facts: Flow<SeasonFacts>,
) : SeasonRepository {

    private val claimedKey = stringSetPreferencesKey("season_claimed_chapters")

    override fun observeProgress(): Flow<SeasonProgress> =
        combine(facts, dataStore.data.map { claimed(it) }) { f, claimed -> SeasonProgress.derive(f, claimed) }

    override suspend fun markClaimed(number: Int): Boolean {
        var first = false
        dataStore.edit { preferences ->
            val claimed = claimed(preferences)
            first = number !in claimed
            if (first) preferences[claimedKey] = (claimed + number).map { it.toString() }.toSet()
        }
        return first
    }

    // I valori non numerici (di una versione futura) si ignorano invece di far fallire la lettura.
    private fun claimed(preferences: Preferences): Set<Int> =
        preferences[claimedKey].orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
}

/** I fatti della Scalata, composti dallo stato che gli altri repository già tengono (nessuna fonte nuova). */
public fun seasonFacts(
    opponents: OpponentRepository,
    dailyPuzzle: DailyPuzzleRepository,
    academy: AcademyRepository,
    mastery: MasteryRepository,
    clock: Clock,
): Flow<SeasonFacts> = combine(
    opponents.observeProgress(),
    dailyPuzzle.observeDailyPuzzle(clock.today().toString()),
    academy.observeProgress(),
    mastery.observeMatchHistory(),
) { ladder, puzzle, lessons, history ->
    SeasonFacts(
        defeated = ladder.defeated,
        dailyPuzzleSolvedToday = puzzle.isSolvedToday,
        completedAcademyChapters = AcademyContent.CHAPTERS
            .filter { chapter -> chapter.lessons.all { lessons[it.id]?.isCompleted == true } }
            .map { it.id }
            .toSet(),
        sparringChecksPlayed = history
            .mapNotNull { entry -> TrainingFundamental.entries.firstOrNull { it.name == entry.sparringObjective } }
            .toSet(),
    )
}

/** Versione in memoria, per i test e le anteprime: i [facts] si cambiano a mano. */
public class InMemorySeasonRepository(
    initialFacts: SeasonFacts = SeasonFacts(),
    initialClaimed: Set<Int> = emptySet(),
) : SeasonRepository {
    private val facts = MutableStateFlow(initialFacts)
    private val claimed = MutableStateFlow(initialClaimed)

    /** Cambia lo stato da cui la Scalata deriva. */
    public fun setFacts(value: SeasonFacts) {
        facts.value = value
    }

    override fun observeProgress(): Flow<SeasonProgress> =
        combine(facts, claimed) { f, c -> SeasonProgress.derive(f, c) }

    override suspend fun markClaimed(number: Int): Boolean {
        val first = number !in claimed.value
        if (first) claimed.value = claimed.value + number
        return first
    }
}
