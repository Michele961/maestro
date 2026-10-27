package it.maestro.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.maestro.app.data.local.datastore.RetentionRepository
import it.maestro.app.data.local.missions.DailyMission
import it.maestro.app.data.local.missions.DailyMissionsState
import it.maestro.app.data.local.missions.StreakState
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.data.local.wallet.WalletRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import it.maestro.app.domain.time.Clock
import it.maestro.app.domain.time.SystemClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Stato della UI per il dialog/pannello delle Missioni Didattiche e della Streak ("Fiamma") (task F2-05).
 */
public data class DailyMissionsUiState(
    val streak: StreakState = StreakState.INITIAL,
    val missionsState: DailyMissionsState = DailyMissionsState("", emptyList()),
    val isLoading: Boolean = false,
)

/**
 * ViewModel per l'osservazione e l'interazione con Streak quotidiana e Missioni didattiche (task F2-05).
 */
public class DailyMissionsViewModel(
    private val retentionRepository: RetentionRepository,
    private val walletRepository: WalletRepository? = null,
    private val clock: Clock = SystemClock,
) : ViewModel() {

    // Il giorno si rilegge con refreshDay() (ritorno in primo piano): a mezzanotte cambiano streak e missioni.
    private val currentDay = MutableStateFlow(clock.today())

    private val today: LocalDate
        get() = currentDay.value

    @OptIn(ExperimentalCoroutinesApi::class)
    public val uiState: StateFlow<DailyMissionsUiState> = currentDay.flatMapLatest { day ->
        combine(
            retentionRepository.observeStreak(day),
            retentionRepository.observeDailyMissions(day),
        ) { streak, missions -> streak to missions }
    }.map { (streak, missions) ->
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

    public fun claimReward(mission: DailyMission) {
        viewModelScope.launch {
            if (mission.isCompleted && !mission.isClaimed) {
                val success = retentionRepository.claimMissionReward(today, mission.type)
                if (success && walletRepository != null) {
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

    /** Rilegge la data dal [Clock]; da chiamare quando l'app torna in primo piano (task F1R-02). */
    public fun refreshDay() {
        currentDay.value = clock.today()
    }

    public fun recordActivity() {
        viewModelScope.launch {
            retentionRepository.recordGameActivity(today)
        }
    }
}
