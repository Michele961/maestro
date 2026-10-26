package it.maestro.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.data.local.datastore.RetentionRepository
import it.maestro.app.data.local.missions.DailyMissionsState
import it.maestro.app.data.local.missions.StreakState
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.data.local.wallet.WalletRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Stato della UI per il dialog/pannello delle Missioni Didattiche e della Streak ("Fiamma") (task F2-05).
 */
public data class DailyMissionsUiState(
    val streak: StreakState = StreakState.INITIAL,
    val missionsState: DailyMissionsState = DailyMissionsState(LocalDate.now().toString(), emptyList()),
    val isLoading: Boolean = false,
)

/**
 * ViewModel per l'osservazione e l'interazione con Streak quotidiana e Missioni didattiche (task F2-05).
 */
public class DailyMissionsViewModel(
    private val retentionRepository: RetentionRepository,
    private val walletRepository: WalletRepository? = null,
    private val dateProvider: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val today: LocalDate
        get() = dateProvider()

    init {
        viewModelScope.launch {
            retentionRepository.observeDailyMissions(today).collect { state ->
                for (mission in state.missions) {
                    if (mission.isCompleted && walletRepository?.hasClaimed(TransactionReason.DAILY_MISSION_REWARD, mission.id) == false) {
                        walletRepository.addCredits(
                            amount = mission.type.rewardCredits,
                            reason = TransactionReason.DAILY_MISSION_REWARD,
                            referenceId = mission.id,
                            description = "Missione completata: ${mission.id}",
                        )
                    }
                }
            }
        }
    }

    public val uiState: StateFlow<DailyMissionsUiState> = combine(
        retentionRepository.observeStreak(today),
        retentionRepository.observeDailyMissions(today),
    ) { streak, missions ->
        DailyMissionsUiState(
            streak = streak,
            missionsState = missions,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DailyMissionsUiState(isLoading = true),
    )

    public fun recordActivity() {
        viewModelScope.launch {
            retentionRepository.recordGameActivity(today)
        }
    }
}
