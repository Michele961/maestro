package it.maestro.app.feature.profile.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.ai.mastery.AbilityRadar
import it.maestro.ai.mastery.MasteryProfile
import it.maestro.app.R
import it.maestro.app.data.local.datastore.MasteryRepository
import it.maestro.app.data.local.datastore.MatchHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Tab selezionabile nella schermata Profilo.
 */
public enum class ProfileTab {
    MASTERY,
    HISTORY,
}

/**
 * Stato della UI per la schermata Profilo & Radar delle Abilità (task F2-04).
 */
public data class ProfileUiState(
    val profile: MasteryProfile = MasteryProfile.INITIAL,
    @get:StringRes val rankTitleRes: Int = R.string.mastery_rank_principiante,
    @get:StringRes val maestroAdviceRes: Int = R.string.profile_advice_default,
    val matchHistory: List<MatchHistoryEntry> = emptyList(),
    val selectedTab: ProfileTab = ProfileTab.MASTERY,
    val isLoading: Boolean = false,
)

/**
 * ViewModel per l'osservazione e l'interazione con l'Indice di Maestria e il Radar (task F2-04).
 */
public class ProfileViewModel(
    private val masteryRepository: MasteryRepository,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(ProfileTab.MASTERY)

    public val uiState: StateFlow<ProfileUiState> = combine(
        masteryRepository.observeMasteryProfile(),
        masteryRepository.observeMatchHistory(),
        _selectedTab,
    ) { profile, history, tab ->
        ProfileUiState(
            profile = profile,
            rankTitleRes = mapEloToRankTitle(profile.cognitiveElo),
            maestroAdviceRes = computeMaestroAdvice(profile.radar, profile.matchesPlayed),
            matchHistory = history,
            selectedTab = tab,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ProfileUiState(isLoading = true),
    )

    public fun selectTab(tab: ProfileTab) {
        _selectedTab.value = tab
    }

    public companion object {
        @StringRes
        public fun mapEloToRankTitle(elo: Int): Int = when {
            elo < 950 -> R.string.mastery_rank_principiante
            elo < 1250 -> R.string.mastery_rank_dilettante
            elo < 1550 -> R.string.mastery_rank_intermedio
            elo < 1850 -> R.string.mastery_rank_avanzato
            else -> R.string.mastery_rank_maestro
        }

        @StringRes
        public fun computeMaestroAdvice(radar: AbilityRadar, matchesPlayed: Int): Int {
            if (matchesPlayed == 0) {
                return R.string.profile_advice_default
            }
            if (radar.sevens >= 85 && radar.denari >= 85 && radar.primiera >= 85 &&
                radar.board >= 85 && radar.endgame >= 85
            ) {
                return R.string.profile_advice_master
            }

            // Identifica il punto debole su cui concentrare i consigli del Maestro
            val minStat = listOf(
                radar.sevens to R.string.profile_advice_sevens,
                radar.denari to R.string.profile_advice_denari,
                radar.primiera to R.string.profile_advice_primiera,
                radar.board to R.string.profile_advice_board,
                radar.endgame to R.string.profile_advice_endgame,
            ).minByOrNull { it.first }

            return minStat?.second ?: R.string.profile_advice_default
        }
    }
}
