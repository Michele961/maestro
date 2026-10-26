package it.maestro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.ai.analysis.MatchAnalysis
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.crash.CrashReporter
import it.maestro.ai.analysis.MoveGrade
import it.maestro.app.audio.SoundEffect
import it.maestro.app.audio.SoundManager
import it.maestro.app.data.local.datastore.AppSettings
import it.maestro.app.data.local.datastore.MasteryRepository
import it.maestro.app.data.local.datastore.RetentionRepository
import it.maestro.app.data.local.datastore.SettingsRepository
import it.maestro.app.data.local.missions.DailyMissionType
import it.maestro.app.data.local.missions.DailyMissionsState
import it.maestro.app.data.local.missions.MatchMissionStatsExtractor
import it.maestro.app.data.local.missions.StreakState
import it.maestro.app.data.local.wallet.TransactionReason
import it.maestro.app.data.local.wallet.WalletRepository
import it.maestro.app.data.repository.MatchRepository
import it.maestro.app.data.repository.RestoreMatchResult
import it.maestro.app.game.TestMatchGenerator
import it.maestro.app.ui.dialogs.DailyMissionsDialog
import it.maestro.app.ui.dialogs.SettingsDialog
import it.maestro.app.ui.screens.DailyMissionsUiState
import it.maestro.app.ui.screens.DailyPuzzleScreen
import it.maestro.app.ui.screens.GameReviewScreen
import it.maestro.app.ui.screens.HomeScreen
import it.maestro.app.ui.screens.PostMatchPreviewScreen
import it.maestro.app.ui.screens.PreGameScreen
import it.maestro.app.ui.screens.ProfileScreen
import it.maestro.app.ui.screens.ShopScreen
import it.maestro.app.ui.screens.ShopViewModel
import it.maestro.app.ui.screens.TableScreen
import it.maestro.app.ui.screens.academy.AcademyScreen
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaState
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.LocalDate

/**
 * Destinazioni di navigazione nell'architettura a singola Activity (ADR-0012, task F1-07, F1-08, F1-09, F2-04).
 */
public sealed interface Screen {
    public data object Home : Screen
    public data object PreGame : Screen
    public data class Table(
        val level: DifficultyLevel,
        val targetScore: Int,
        val trainingFundamental: TrainingFundamental = TrainingFundamental.SEVENS_COUNT,
        val seed: Long = System.currentTimeMillis(),
        val restoredState: ScopaState? = null,
        val restoredLog: MatchLog? = null,
    ) : Screen
    public data class PostMatchPreview(
        val matchLog: MatchLog,
        val level: DifficultyLevel,
        val humanSeat: Seat = Seat.P1,
        /** Check di allenamento: risposte corrette e totali, se ce ne sono stati (F1-12). */
        val trainingScore: Pair<Int, Int>? = null,
        val trainingFundamental: TrainingFundamental = TrainingFundamental.NONE,
        val isFromHistory: Boolean = false,
    ) : Screen
    public data class GameReview(
        val matchLog: MatchLog,
        val analysis: MatchAnalysis,
        val level: DifficultyLevel,
        val humanSeat: Seat = Seat.P1,
    ) : Screen
    public data object DailyPuzzle : Screen
    public data object Profile : Screen
    public data object Academy : Screen
    public data object Shop : Screen
}

/**
 * Singola Activity dell'app Android Maestro (ADR-0012).
 */
@OptIn(ExperimentalFoundationApi::class)
public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Esperienza da gioco immersivo a tutto schermo: nasconde status bar e navigation bar
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MaestroTheme {
                // F1-26: niente rimbalzo "da web app" e contenuto ad altezza naturale.
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                val matchRepository: MatchRepository = koinInject()
                val settingsRepository: SettingsRepository = koinInject()
                val masteryRepository: MasteryRepository = koinInject()
                val retentionRepository: RetentionRepository = koinInject()
                val walletRepository: WalletRepository = koinInject()
                val soundManager: SoundManager = koinInject()
                val crashReporter: CrashReporter = koinInject()
                val scope = rememberCoroutineScope()
                val today = remember { LocalDate.now() }
                val streakState by retentionRepository.observeStreak(today).collectAsStateWithLifecycle(initialValue = StreakState.INITIAL)
                val missionsState by retentionRepository.observeDailyMissions(today).collectAsStateWithLifecycle(initialValue = DailyMissionsState(today.toString(), emptyList()))
                val credits by walletRepository.observeBalance().collectAsStateWithLifecycle(initialValue = 0)
                val settings by settingsRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = AppSettings())
                var showSettingsDialog by remember { mutableStateOf(false) }
                var showMissionsDialog by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                var hasActiveMatch by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var crashReport by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    try {
                        walletRepository.ensureWelcomeBonus(bonusAmount = 50)
                    } catch (_: Throwable) {}
                    try {
                        val file = filesDir.resolve("last_crash.txt")
                        if (file.exists()) {
                            val content = file.readText()
                            file.delete()
                            if (content.isNotBlank()) {
                                crashReport = content
                            }
                        }
                    } catch (_: Throwable) {}
                }

                if (crashReport != null) {
                    AlertDialog(
                        onDismissRequest = { crashReport = null },
                        title = { Text("Segnalazione Errore") },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                    text = "Dettaglio crash rilevato nella sessione precedente:",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.foundation.text.selection.SelectionContainer {
                                    Text(
                                        text = crashReport ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { crashReport = null }) {
                                Text("Chiudi")
                            }
                        },
                    )
                }

                LaunchedEffect(currentScreen) {
                    if (currentScreen is Screen.Home) {
                        val active = matchRepository.restoreActiveMatch()
                        hasActiveMatch = active is RestoreMatchResult.Success
                    }
                }

                if (showSettingsDialog) {
                    SettingsDialog(
                        soundEnabled = settings.soundEnabled,
                        onSoundToggled = { enabled ->
                            scope.launch { settingsRepository.updateSoundEnabled(enabled) }
                        },
                        onTriggerTestCrash = { crashReporter.triggerTestCrash() },
                        onDismiss = { showSettingsDialog = false },
                        onStartTestEndgameMatch = {
                            val (testState, testLog) = TestMatchGenerator.generateEndgameMatch()
                            showSettingsDialog = false
                            currentScreen = Screen.Table(
                                level = DifficultyLevel.INTERMEDIO,
                                targetScore = 11,
                                seed = testLog.seed,
                                restoredState = testState,
                                restoredLog = testLog,
                            )
                        },
                    )
                }

                if (showMissionsDialog) {
                    DailyMissionsDialog(
                        uiState = DailyMissionsUiState(
                            streak = streakState,
                            missionsState = missionsState,
                        ),
                        onDismissRequest = { showMissionsDialog = false },
                        onClaimReward = { mission ->
                            scope.launch {
                                val claimed = retentionRepository.claimMissionReward(today, mission.type)
                                if (claimed) {
                                    walletRepository.addCredits(
                                        amount = mission.type.rewardCredits,
                                        reason = TransactionReason.DAILY_MISSION_REWARD,
                                        referenceId = mission.id,
                                        description = "Ricompensa missione: ${mission.id}",
                                    )
                                    soundManager.play(SoundEffect.CHECK_CORRECT)
                                }
                            }
                        },
                    )
                }

                when (val screen = currentScreen) {
                    Screen.Home -> HomeScreen(
                        hasActiveMatch = hasActiveMatch,
                        onResumeGameClicked = {
                            scope.launch {
                                when (val result = matchRepository.restoreActiveMatch()) {
                                    is RestoreMatchResult.Success -> {
                                        currentScreen = Screen.Table(
                                            level = result.level,
                                            targetScore = result.targetScore,
                                            restoredState = result.state,
                                            restoredLog = result.matchLog,
                                        )
                                    }
                                    is RestoreMatchResult.WrongRulesVersion -> {
                                        errorMessage = "wrong_rules_version"
                                        matchRepository.clearActiveMatch()
                                        hasActiveMatch = false
                                    }
                                    else -> {
                                        matchRepository.clearActiveMatch()
                                        hasActiveMatch = false
                                    }
                                }
                            }
                        },
                        onStartGameClicked = { currentScreen = Screen.PreGame },
                        onSettingsClicked = { showSettingsDialog = true },
                        onDailyPuzzleClicked = { currentScreen = Screen.DailyPuzzle },
                        onProfileClicked = { currentScreen = Screen.Profile },
                        onMissionsClicked = { showMissionsDialog = true },
                        onAcademyClicked = { currentScreen = Screen.Academy },
                        onShopClicked = { currentScreen = Screen.Shop },
                        streakCount = streakState.currentStreak,
                        credits = credits,
                        avatarId = settings.selectedAvatar,
                    )
                    Screen.PreGame -> PreGameScreen(
                        onStartGame = { level, targetScore, fundamental ->
                            currentScreen = Screen.Table(
                                level = level,
                                targetScore = targetScore,
                                trainingFundamental = fundamental,
                            )
                        },
                        onNavigateBack = { currentScreen = Screen.Home },
                    )
                    is Screen.Table -> TableScreen(
                        level = screen.level,
                        targetScore = screen.targetScore,
                        trainingFundamental = screen.trainingFundamental,
                        seed = screen.seed,
                        restoreState = screen.restoredState,
                        restoreLog = screen.restoredLog,
                        tableThemeId = settings.selectedTableTheme,
                        cardBackThemeId = settings.selectedCardBackTheme,
                        onNavigateHome = { currentScreen = Screen.Home },
                        onViewAnalysis = { log, training ->
                            currentScreen = Screen.PostMatchPreview(
                                matchLog = log,
                                level = screen.level,
                                trainingScore = training,
                                trainingFundamental = screen.trainingFundamental,
                            )
                        },
                    )
                    is Screen.PostMatchPreview -> PostMatchPreviewScreen(
                        matchLog = screen.matchLog,
                        level = screen.level,
                        humanSeat = screen.humanSeat,
                        trainingScore = screen.trainingScore,
                        trainingFundamental = screen.trainingFundamental,
                        onMatchAnalyzed = { analysis ->
                            if (!screen.isFromHistory) {
                                scope.launch {
                                    masteryRepository.recordMatch(
                                        matchAnalysis = analysis,
                                        matchLog = screen.matchLog,
                                        humanSeat = screen.humanSeat,
                                        trainingScore = screen.trainingScore,
                                        trainingFundamental = screen.trainingFundamental,
                                    )
                                    retentionRepository.recordGameActivity(today)
                                    val missionStats = MatchMissionStatsExtractor.extract(screen.matchLog, screen.humanSeat)
                                    if (missionStats.sweepsCount > 0) {
                                        retentionRepository.updateMissionProgress(
                                            today,
                                            DailyMissionType.MAKE_SWEEPS,
                                            missionStats.sweepsCount,
                                        )
                                    }
                                    if (missionStats.denariCount > 0) {
                                        retentionRepository.updateMissionProgress(
                                            today,
                                            DailyMissionType.CAPTURE_DENARI,
                                            missionStats.denariCount,
                                        )
                                    }
                                    if (analysis.accuracy(screen.humanSeat) >= 80) {
                                        retentionRepository.updateMissionProgress(
                                            today,
                                            DailyMissionType.COMPLETE_MATCH_WITH_ACCURACY_80,
                                        )
                                    }
                                    if (analysis.count(screen.humanSeat, MoveGrade.BLUNDER) == 0) {
                                        retentionRepository.updateMissionProgress(
                                            today,
                                            DailyMissionType.PLAY_MATCH_WITHOUT_BLUNDERS,
                                        )
                                    }
                                    if (screen.trainingFundamental == TrainingFundamental.SEVENS_COUNT &&
                                        screen.trainingScore != null
                                    ) {
                                        retentionRepository.updateMissionProgress(
                                            today,
                                            DailyMissionType.ANSWER_SEVENS_CHECKS,
                                            screen.trainingScore.first,
                                        )
                                    }
                                }
                            }
                        },
                        onReviewClicked = { analysis ->
                            currentScreen = Screen.GameReview(
                                matchLog = screen.matchLog,
                                analysis = analysis,
                                level = screen.level,
                                humanSeat = screen.humanSeat,
                            )
                        },
                        onRematchClicked = {
                            currentScreen = Screen.Table(
                                level = screen.level,
                                targetScore = screen.matchLog.config.targetScore,
                            )
                        },
                        onHomeClicked = {
                            currentScreen = if (screen.isFromHistory) Screen.Profile else Screen.Home
                        },
                        onProfileClicked = { currentScreen = Screen.Profile },
                    )
                    is Screen.GameReview -> GameReviewScreen(
                        matchLog = screen.matchLog,
                        analysis = screen.analysis,
                        humanSeat = screen.humanSeat,
                        onNavigateBack = {
                            currentScreen = Screen.PostMatchPreview(
                                matchLog = screen.matchLog,
                                level = screen.level,
                                humanSeat = screen.humanSeat,
                                isFromHistory = true,
                            )
                        },
                    )
                    Screen.DailyPuzzle -> DailyPuzzleScreen(
                        onNavigateHome = { currentScreen = Screen.Home },
                    )
                    Screen.Profile -> ProfileScreen(
                        onBackClicked = { currentScreen = Screen.Home },
                        onReviewMatch = { log ->
                            currentScreen = Screen.PostMatchPreview(
                                matchLog = log,
                                level = DifficultyLevel.INTERMEDIO,
                                isFromHistory = true,
                            )
                        },
                    )
                    Screen.Academy -> AcademyScreen(
                        onNavigateHome = { currentScreen = Screen.Home },
                    )
                    Screen.Shop -> ShopScreen(
                        onNavigateHome = { currentScreen = Screen.Home },
                    )
                }

                if (errorMessage == "wrong_rules_version") {
                    AlertDialog(
                        onDismissRequest = { errorMessage = null },
                        title = { Text(text = stringResource(id = R.string.deal_summary_title)) },
                        text = { Text(text = stringResource(id = R.string.error_wrong_rules_version)) },
                        confirmButton = {
                            Button(onClick = { errorMessage = null }) {
                                Text(text = stringResource(id = R.string.deal_summary_continue))
                            }
                        },
                    )
                }
                }
                }
            }
        }
    }
}
