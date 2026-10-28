package it.maestro.app.feature.missions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.domain.progress.AchievementId
import it.maestro.app.domain.progress.AchievementRepository
import it.maestro.app.domain.progress.Experience
import it.maestro.app.domain.progress.ExperienceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Esperienza e obiettivi del giocatore per la schermata «Oggi» (F1R-29f). */
public data class ProgressUiState(
    val points: Int = 0,
    val unlocked: Set<AchievementId> = emptySet(),
) {
    /** Livello di esperienza (da 1). */
    val level: Int get() = Experience.levelOf(points)

    /** Punti già fatti dentro il livello corrente. */
    val pointsIntoLevel: Int get() = Experience.pointsIntoLevel(points)

    /** True al livello massimo, dove non c'è un livello successivo da raggiungere. */
    val isMaxLevel: Boolean get() = level == Experience.MAX_LEVEL

    /** Obiettivi sbloccati, nell'ordine di definizione. */
    val unlockedInOrder: List<AchievementId> get() = AchievementId.entries.filter { it in unlocked }
}

/** Legge i punti esperienza e gli obiettivi sbloccati; non scrive nulla (lo fa solo `RewardEngine`). */
public class ProgressViewModel(
    experience: ExperienceRepository,
    achievements: AchievementRepository,
) : ViewModel() {
    public val uiState: StateFlow<ProgressUiState> = combine(
        experience.observePoints(),
        achievements.observeUnlocked(),
    ) { points, unlocked -> ProgressUiState(points, unlocked) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ProgressUiState())
}
