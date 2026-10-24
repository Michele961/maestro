package it.maestro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.data.repository.MatchRepository
import it.maestro.app.data.repository.RestoreMatchResult
import it.maestro.app.ui.screens.HomeScreen
import it.maestro.app.ui.screens.PostMatchPreviewScreen
import it.maestro.app.ui.screens.PreGameScreen
import it.maestro.app.ui.screens.TableScreen
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaState
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
        val restoredState: ScopaState? = null,
        val restoredLog: MatchLog? = null,
    ) : Screen
    public data class PostMatchPreview(
        val matchLog: MatchLog,
        val level: DifficultyLevel,
        val humanSeat: Seat = Seat.P1,
    ) : Screen
}

/**
 * Singola Activity dell'app Android Maestro (ADR-0012).
 */
public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaestroTheme {
                val matchRepository: MatchRepository = koinInject()
                val scope = rememberCoroutineScope()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                var hasActiveMatch by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(currentScreen) {
                    if (currentScreen is Screen.Home) {
                        val active = matchRepository.restoreActiveMatch()
                        hasActiveMatch = active is RestoreMatchResult.Success
                    }
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
                    )
                    Screen.PreGame -> PreGameScreen(
                        onStartGame = { level, targetScore ->
                            currentScreen = Screen.Table(level = level, targetScore = targetScore)
                        },
                        onNavigateBack = { currentScreen = Screen.Home },
                    )
                    is Screen.Table -> TableScreen(
                        level = screen.level,
                        targetScore = screen.targetScore,
                        restoreState = screen.restoredState,
                        restoreLog = screen.restoredLog,
                        onNavigateHome = { currentScreen = Screen.Home },
                        onViewAnalysis = { log ->
                            currentScreen = Screen.PostMatchPreview(
                                matchLog = log,
                                level = screen.level,
                            )
                        },
                    )
                    is Screen.PostMatchPreview -> PostMatchPreviewScreen(
                        matchLog = screen.matchLog,
                        level = screen.level,
                        humanSeat = screen.humanSeat,
                        onRematchClicked = {
                            currentScreen = Screen.Table(
                                level = screen.level,
                                targetScore = screen.matchLog.config.targetScore,
                            )
                        },
                        onHomeClicked = { currentScreen = Screen.Home },
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
