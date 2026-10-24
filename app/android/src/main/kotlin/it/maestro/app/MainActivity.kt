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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import it.maestro.app.data.local.datastore.AppSettings
import it.maestro.app.data.local.datastore.SettingsRepository
import it.maestro.app.data.repository.MatchRepository
import it.maestro.app.data.repository.RestoreMatchResult
import it.maestro.app.ui.dialogs.SettingsDialog
import it.maestro.app.ui.screens.GameReviewScreen
import it.maestro.app.ui.screens.HomeScreen
import it.maestro.app.ui.screens.PostMatchPreviewScreen
import it.maestro.app.ui.screens.PreGameScreen
import it.maestro.app.ui.screens.TableScreen
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaState
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Destinazioni di navigazione nell'architettura a singola Activity (ADR-0012, task F1-07, F1-08, F1-09).
 */
public sealed interface Screen {
    public data object Home : Screen
    public data object PreGame : Screen
    public data class Table(
        val level: DifficultyLevel,
        val targetScore: Int,
        val trainingFundamental: TrainingFundamental = TrainingFundamental.SEVENS_COUNT,
        val restoredState: ScopaState? = null,
        val restoredLog: MatchLog? = null,
    ) : Screen
    public data class PostMatchPreview(
        val matchLog: MatchLog,
        val level: DifficultyLevel,
        val humanSeat: Seat = Seat.P1,
        /** Check di allenamento: risposte corrette e totali, se ce ne sono stati (F1-12). */
        val trainingScore: Pair<Int, Int>? = null,
    ) : Screen
    public data class GameReview(
        val matchLog: MatchLog,
        val analysis: MatchAnalysis,
        val level: DifficultyLevel,
        val humanSeat: Seat = Seat.P1,
    ) : Screen
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
                val crashReporter: CrashReporter = koinInject()
                val scope = rememberCoroutineScope()
                val settings by settingsRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = AppSettings())
                var showSettingsDialog by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                var hasActiveMatch by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        restoreState = screen.restoredState,
                        restoreLog = screen.restoredLog,
                        onNavigateHome = { currentScreen = Screen.Home },
                        onViewAnalysis = { log, training ->
                            currentScreen = Screen.PostMatchPreview(
                                matchLog = log,
                                level = screen.level,
                                trainingScore = training,
                            )
                        },
                    )
                    is Screen.PostMatchPreview -> PostMatchPreviewScreen(
                        matchLog = screen.matchLog,
                        level = screen.level,
                        humanSeat = screen.humanSeat,
                        trainingScore = screen.trainingScore,
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
                        onHomeClicked = { currentScreen = Screen.Home },
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
                            )
                        },
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
