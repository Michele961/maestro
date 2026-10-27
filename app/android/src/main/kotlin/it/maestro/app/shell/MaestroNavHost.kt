package it.maestro.app.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.R
import it.maestro.app.shell.crash.CrashReporter
import it.maestro.app.data.local.datastore.AppSettings
import it.maestro.app.domain.retention.RetentionRepository
import it.maestro.app.data.local.datastore.SettingsRepository
import it.maestro.app.domain.retention.DailyMissionsState
import it.maestro.app.domain.retention.StreakState
import it.maestro.app.domain.wallet.WalletRepository
import it.maestro.app.domain.time.Clock
import it.maestro.app.feature.missions.ui.DailyMissionsDialog
import it.maestro.app.feature.settings.ui.SettingsDialog
import it.maestro.app.feature.missions.presentation.DailyMissionsUiState
import it.maestro.app.feature.puzzle.ui.DailyPuzzleScreen
import it.maestro.app.feature.review.ui.GameReviewScreen
import it.maestro.app.feature.home.ui.HomeScreen
import it.maestro.app.feature.review.ui.PostMatchPreviewScreen
import it.maestro.app.feature.pregame.ui.PreGameScreen
import it.maestro.app.feature.profile.ui.ProfileScreen
import it.maestro.app.feature.shop.ui.ShopScreen
import it.maestro.app.feature.table.ui.TableScreen
import it.maestro.app.feature.academy.ui.AcademyScreen
import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.game.model.Seat
import it.maestro.game.scopa.MatchLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import it.maestro.game.training.TrainingFundamental
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel

/**
 * Navigazione dell'app con back stack di sistema e rotte tipizzate (`docs/app/layers.md` §4, task F1R-04).
 *
 * Le feature espongono callback; solo qui si decide dove andare.
 */
@Composable
public fun MaestroNavHost(
    navController: NavHostController = rememberNavController(),
    shell: ShellViewModel = koinViewModel(),
    pendingCrashReport: String? = null,
) {
    val settingsRepository: SettingsRepository = koinInject()
    val retentionRepository: RetentionRepository = koinInject()
    val walletRepository: WalletRepository = koinInject()
    val crashReporter: CrashReporter = koinInject()
    val clock: Clock = koinInject()
    val scope = rememberCoroutineScope()

    // Il giorno si rilegge al ritorno in primo piano: a mezzanotte cambiano streak e missioni (F1R-02).
    var today by remember { mutableStateOf(clock.today()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { today = clock.today() }

    val streak by retentionRepository.observeStreak(today)
        .collectAsStateWithLifecycle(initialValue = StreakState.INITIAL)
    val missions by retentionRepository.observeDailyMissions(today)
        .collectAsStateWithLifecycle(initialValue = DailyMissionsState(today.toString(), emptyList()))
    val settings by settingsRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = AppSettings())
    val hasActiveMatch by shell.hasActiveMatch.collectAsStateWithLifecycle()
    val credits by walletRepository.observeBalance().collectAsStateWithLifecycle(initialValue = 0)

    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showMissions by rememberSaveable { mutableStateOf(false) }
    var showWrongRules by rememberSaveable { mutableStateOf(false) }
    var crashReport by rememberSaveable { mutableStateOf(pendingCrashReport) }

    fun goHome() {
        navController.navigate(Route.Home) { popUpTo(Route.Home) { inclusive = true } }
    }

    NavHost(navController = navController, startDestination = Route.Home) {
        composable<Route.Home> {
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { shell.refreshActiveMatch() }
            HomeScreen(
                hasActiveMatch = hasActiveMatch,
                onResumeGameClicked = {
                    scope.launch {
                        when (shell.checkResumable()) {
                            ResumeOutcome.RESUMED -> navController.navigate(
                                Route.Table(
                                    level = DifficultyLevel.INTERMEDIO.name,
                                    targetScore = 0,
                                    fundamental = TrainingFundamental.NONE.name,
                                    seed = 0L,
                                    resume = true,
                                ),
                            )
                            ResumeOutcome.WRONG_RULES_VERSION -> showWrongRules = true
                            ResumeOutcome.NOTHING_TO_RESUME -> Unit
                        }
                    }
                },
                onStartGameClicked = { navController.navigate(Route.PreGame) },
                onSettingsClicked = { showSettings = true },
                onDailyPuzzleClicked = { navController.navigate(Route.DailyPuzzle) },
                onProfileClicked = { navController.navigate(Route.Profile) },
                onMissionsClicked = { showMissions = true },
                onAcademyClicked = { navController.navigate(Route.Academy) },
                onShopClicked = { navController.navigate(Route.Shop) },
                streakCount = streak.currentStreak,
                credits = credits,
                avatarId = settings.selectedAvatar,
            )
        }
        composable<Route.PreGame> {
            PreGameScreen(
                onStartGame = { level, targetScore, fundamental ->
                    navController.navigate(
                        Route.Table(level.name, targetScore, fundamental.name, seed = shell.newSeed()),
                    ) { popUpTo(Route.Home) }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<Route.Table> { entry ->
            val route = entry.toRoute<Route.Table>()
            val level = DifficultyLevel.valueOf(route.level)
            TableScreen(
                level = level,
                targetScore = route.targetScore,
                trainingFundamental = TrainingFundamental.valueOf(route.fundamental),
                seed = route.seed,
                resume = route.resume,
                tableThemeId = settings.selectedTableTheme,
                cardBackThemeId = settings.selectedCardBackTheme,
                onNavigateHome = { goHome() },
                onViewAnalysis = { log, training ->
                    navController.navigate(
                        Route.PostMatch(
                            matchLog = log.toText(),
                            level = route.level,
                            fundamental = route.fundamental,
                            trainingCorrect = training?.first ?: -1,
                            trainingTotal = training?.second ?: -1,
                        ),
                    ) { popUpTo(Route.Home) }
                },
            )
        }
        composable<Route.PostMatch> { entry ->
            val route = entry.toRoute<Route.PostMatch>()
            val matchLog = remember(route.matchLog) { MatchLog.parse(route.matchLog) }
            val training = if (route.trainingTotal >= 0) route.trainingCorrect to route.trainingTotal else null
            val fundamental = TrainingFundamental.valueOf(route.fundamental)
            PostMatchPreviewScreen(
                matchLog = matchLog,
                level = DifficultyLevel.valueOf(route.level),
                trainingScore = training,
                trainingFundamental = fundamental,
                isFromHistory = route.fromHistory,
                onMatchAnalyzed = { analysis ->
                    shell.onMatchAnalyzed(
                        matchLog = matchLog,
                        analysis = analysis,
                        humanSeat = Seat.P1,
                        trainingScore = training,
                        trainingFundamental = fundamental,
                        fromHistory = route.fromHistory,
                    )
                },
                onReviewClicked = {
                    navController.navigate(Route.Review(route.matchLog, route.level, route.fromHistory))
                },
                onRematchClicked = {
                    navController.navigate(
                        Route.Table(route.level, matchLog.config.targetScore, route.fundamental, shell.newSeed()),
                    ) { popUpTo(Route.Home) }
                },
                onHomeClicked = { if (route.fromHistory) navController.popBackStack() else goHome() },
                onProfileClicked = { navController.navigate(Route.Profile) },
            )
        }
        composable<Route.Review> { entry ->
            val route = entry.toRoute<Route.Review>()
            val matchLog = remember(route.matchLog) { MatchLog.parse(route.matchLog) }
            // Analisi già pronta dalla fine partita; dopo la chiusura del processo si ricalcola fuori dal thread UI.
            val analysis by produceState(shell.cachedAnalysis(route.matchLog), route.matchLog) {
                if (value == null) value = withContext(Dispatchers.Default) { MatchAnalyzer().analyze(matchLog) }
            }
            analysis?.let {
                GameReviewScreen(
                    matchLog = matchLog,
                    analysis = it,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
        composable<Route.DailyPuzzle> {
            DailyPuzzleScreen(onNavigateHome = { navController.popBackStack() })
        }
        composable<Route.Profile> {
            ProfileScreen(
                onBackClicked = { navController.popBackStack() },
                onReviewMatch = { log ->
                    navController.navigate(
                        Route.PostMatch(
                            matchLog = log.toText(),
                            level = DifficultyLevel.INTERMEDIO.name,
                            fundamental = TrainingFundamental.NONE.name,
                            fromHistory = true,
                        ),
                    )
                },
            )
        }
        composable<Route.Academy> {
            AcademyScreen(onNavigateHome = { navController.popBackStack() })
        }
        composable<Route.Shop> {
            ShopScreen(onNavigateHome = { navController.popBackStack() })
        }
    }

    if (showSettings) {
        SettingsDialog(
            soundEnabled = settings.soundEnabled,
            onSoundToggled = { enabled -> scope.launch { settingsRepository.updateSoundEnabled(enabled) } },
            gameSpeed = settings.gameSpeed,
            onGameSpeedChanged = { speed -> scope.launch { settingsRepository.updateGameSpeed(speed) } },
            maestroChattiness = settings.maestroChattiness,
            onMaestroChattinessChanged = { level -> scope.launch { settingsRepository.updateMaestroChattiness(level) } },
            onTriggerTestCrash = { crashReporter.triggerTestCrash() },
            onDismiss = { showSettings = false },
            onStartTestEndgameMatch = {
                showSettings = false
                scope.launch {
                    val log = shell.prepareTestEndgameMatch()
                    navController.navigate(
                        Route.Table(
                            level = DifficultyLevel.INTERMEDIO.name,
                            targetScore = log.config.targetScore,
                            fundamental = TrainingFundamental.NONE.name,
                            seed = log.seed,
                            resume = true,
                        ),
                    )
                }
            },
        )
    }

    if (showMissions) {
        DailyMissionsDialog(
            uiState = DailyMissionsUiState(streak = streak, missionsState = missions),
            onDismissRequest = { showMissions = false },
            onClaimReward = { mission -> shell.claimMissionReward(mission) },
        )
    }

    if (showWrongRules) {
        AlertDialog(
            onDismissRequest = { showWrongRules = false },
            title = { Text(text = stringResource(id = R.string.deal_summary_title)) },
            text = { Text(text = stringResource(id = R.string.error_wrong_rules_version)) },
            confirmButton = {
                Button(onClick = { showWrongRules = false }) {
                    Text(text = stringResource(id = R.string.deal_summary_continue))
                }
            },
        )
    }

    crashReport?.let { report -> CrashReportDialog(report) { crashReport = null } }
}

/** Crash della sessione precedente, mostrato solo nelle build di debug (F1R-05). */
@Composable
private fun CrashReportDialog(report: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Segnalazione Errore") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Dettaglio crash rilevato nella sessione precedente:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                SelectionContainer {
                    Text(text = report, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Chiudi") } },
    )
}
