package it.maestro.app.shell

import android.app.Activity
import it.maestro.app.domain.audio.AudioChannel
import it.maestro.app.data.local.datastore.DailyMatchRepository
import it.maestro.app.domain.daily.DailyMatch
import it.maestro.app.domain.daily.DailyMatchResult
import it.maestro.app.domain.match.HouseRules
import it.maestro.app.domain.audio.AudioLevels
import it.maestro.app.domain.audio.Backdrops
import it.maestro.app.designsystem.audio.SoundManager
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import it.maestro.app.domain.settings.AppLanguage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import it.maestro.app.designsystem.components.ClubBackground
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
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
import it.maestro.app.feature.puzzle.presentation.PuzzleShareFormatter
import it.maestro.app.shell.crash.CrashReporter
import it.maestro.app.domain.settings.AppSettings
import it.maestro.app.data.local.datastore.MAX_PLAYER_NAME
import it.maestro.app.domain.retention.RetentionRepository
import it.maestro.app.domain.settings.SettingsRepository
import it.maestro.app.domain.retention.StreakState
import it.maestro.app.domain.mastery.MasteryRepository
import it.maestro.app.domain.wallet.WalletRepository
import kotlinx.coroutines.flow.map
import it.maestro.app.domain.time.Clock
import it.maestro.app.feature.missions.ui.TodayScreen
import it.maestro.app.designsystem.haptics.LocalHapticsEnabled
import it.maestro.app.feature.settings.ui.SettingsScreen
import it.maestro.app.feature.puzzle.ui.DailyPuzzleScreen
import it.maestro.app.feature.review.ui.GameReviewScreen
import it.maestro.app.feature.home.ui.HomeScreen
import it.maestro.app.feature.review.presentation.PostMatchViewModel
import it.maestro.app.feature.review.ui.PostMatchScreen
import org.koin.core.parameter.parametersOf
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.feature.pregame.presentation.LadderViewModel
import it.maestro.app.feature.pregame.presentation.PreGameViewModel
import it.maestro.app.feature.pregame.ui.LadderScreen
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
    val masteryRepository: MasteryRepository = koinInject()
    val crashReporter: CrashReporter = koinInject()
    val clock: Clock = koinInject()
    val dailyMatchRepository: DailyMatchRepository = koinInject()
    val soundManager: SoundManager = koinInject()
    val scope = rememberCoroutineScope()

    // Il giorno si rilegge al ritorno in primo piano: a mezzanotte cambiano streak e missioni (F1R-02).
    var today by remember { mutableStateOf(clock.today()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { today = clock.today() }

    val streak by retentionRepository.observeStreak(today)
        .collectAsStateWithLifecycle(initialValue = StreakState.INITIAL)
    val settings by settingsRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = AppSettings())
    // Il nome si chiede solo a impostazioni lette: col valore iniziale il dialogo lampeggerebbe a ogni avvio.
    val loadedFlow = remember(settingsRepository) { settingsRepository.settingsFlow.map { true } }
    val settingsLoaded by loadedFlow.collectAsStateWithLifecycle(initialValue = false)
    val hasActiveMatch by shell.hasActiveMatch.collectAsStateWithLifecycle()
    val credits by walletRepository.observeBalance().collectAsStateWithLifecycle(initialValue = 0)
    val masteryFlow = remember(masteryRepository) { masteryRepository.observeMasteryProfile().map { it.cognitiveElo } }
    val mastery by masteryFlow.collectAsStateWithLifecycle(initialValue = null)

    var showWrongRules by rememberSaveable { mutableStateOf(false) }
    var crashReport by rememberSaveable { mutableStateOf(pendingCrashReport) }

    fun goHome() {
        navController.navigate(Route.Home) { popUpTo(Route.Home) { inclusive = true } }
    }

    // Sotto le schermate fuori partita suona il circolo; il tavolo e la fine partita scelgono da sé (F1R-20).
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val screenOwnsBackdrop = destination?.hasRoute<Route.Table>() == true
    LaunchedEffect(destination, settingsLoaded) {
        if (settingsLoaded && !screenOwnsBackdrop) soundManager.setBackdrop(Backdrops.circle(clock.localHour(), settings.musicChoice))
    }

    // La lingua si applica solo a impostazioni lette davvero: il valore iniziale non deve cambiarla (F1R-31c).
    val activity = LocalContext.current as? Activity
    LaunchedEffect(settingsLoaded, settings.language) {
        if (settingsLoaded && activity != null) AppLocale.apply(activity, AppLanguage.fromTag(settings.language))
    }
    CompositionLocalProvider(LocalHapticsEnabled provides settings.hapticsEnabled) {
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
                                        level = (shell.savedMatchLevel() ?: DifficultyLevel.INTERMEDIO).name,
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
                    onStartGameClicked = { navController.navigate(Route.Ladder()) { launchSingleTop = true } },
                    onSettingsClicked = { navController.navigate(Route.Settings) { launchSingleTop = true } },
                    onDailyPuzzleClicked = { navController.navigate(Route.DailyPuzzle) { launchSingleTop = true } },
                    onDailyMatchClicked = {
                        navController.navigate(
                            Route.Table(
                                level = DifficultyLevel.INTERMEDIO.name,
                                targetScore = DailyMatch.TARGET_SCORE,
                                fundamental = TrainingFundamental.NONE.name,
                                seed = DailyMatch.seedFor(today),
                            ),
                        ) { popUpTo(Route.Home) }
                    },
                    onProfileClicked = { navController.navigate(Route.Profile) { launchSingleTop = true } },
                    onMissionsClicked = { navController.navigate(Route.Today) { launchSingleTop = true } },
                    onAcademyClicked = { navController.navigate(Route.Academy) { launchSingleTop = true } },
                    onShopClicked = { navController.navigate(Route.Shop) { launchSingleTop = true } },
                    streakCount = streak.currentStreak,
                    credits = credits,
                    avatarId = settings.selectedAvatar,
                    playerName = settings.playerName,
                    day = today.toEpochDay(),
                    playerNameMaxLength = MAX_PLAYER_NAME,
                    askPlayerName = settingsLoaded && settings.playerName == null && !settings.playerNameAsked,
                    onPlayerNameConfirmed = { name -> scope.launch { settingsRepository.updatePlayerName(name) } },
                    onPlayerNameSkipped = { scope.launch { settingsRepository.markPlayerNameAsked() } },
                    mastery = mastery,
                )
            }
            composable<Route.Ladder> { entry ->
                val ladderRoute = entry.toRoute<Route.Ladder>()
                val viewModel: LadderViewModel = koinViewModel()
                val ladder by viewModel.state.collectAsStateWithLifecycle()
                LadderScreen(
                    state = ladder,
                    onOpponentClick = { opponent ->
                        navController.navigate(Route.PreGame(opponent.name, ladderRoute.objective)) { launchSingleTop = true }
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable<Route.PreGame> { entry ->
                val route = entry.toRoute<Route.PreGame>()
                val preGameViewModel: PreGameViewModel = koinViewModel()
                val suggested by preGameViewModel.suggestedObjective.collectAsStateWithLifecycle()
                PreGameScreen(
                    opponent = Opponent.valueOf(route.opponent),
                    suggestedObjective = route.objective?.let(TrainingFundamental::valueOf) ?: suggested,
                    onStartGame = { level, targetScore, fundamental, rules ->
                        navController.navigate(
                            Route.Table(
                                level.name,
                                targetScore,
                                fundamental.name,
                                seed = shell.newSeed(),
                                reBello = rules.reBello,
                                redealOnThreeKings = rules.redealOnThreeKings,
                                napola = rules.napola,
                                assoPigliatutto = rules.assoPigliatutto,
                            ),
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
                    houseRules = HouseRules(route.reBello, route.redealOnThreeKings, route.napola, route.assoPigliatutto),
                    trainingFundamental = TrainingFundamental.valueOf(route.fundamental),
                    seed = route.seed,
                    resume = route.resume,
                    tableThemeId = settings.selectedTableTheme,
                    cardBackThemeId = settings.selectedCardBackTheme,
                    showPointsRace = settings.showPointsRace,
                    chattiness = settings.maestroChattiness,
                    onChattinessChange = { scope.launch { settingsRepository.updateMaestroChattiness(it) } },
                    audio = AudioLevels(settings.soundEnabled, settings.effectsVolume, settings.musicVolume, settings.ambientVolume, settings.musicChoice),
                    onSoundToggle = { scope.launch { settingsRepository.updateSoundEnabled(!settings.soundEnabled) } },
                    onVolumeChange = { channel, volume ->
                        scope.launch {
                            when (channel) {
                                AudioChannel.EFFECTS -> settingsRepository.updateEffectsVolume(volume)
                                AudioChannel.MUSIC -> settingsRepository.updateMusicVolume(volume)
                                AudioChannel.AMBIENT -> settingsRepository.updateAmbientVolume(volume)
                            }
                        }
                    },
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
                LaunchedEffect(route) {
                    shell.startPostMatch(
                        matchLog, Seat.P1, training, fundamental, route.fromHistory,
                        opponent = Opponent.forLevel(DifficultyLevel.valueOf(route.level)),
                    )
                }
                val viewModel: PostMatchViewModel = koinViewModel(key = route.matchLog) {
                    parametersOf(matchLog, Seat.P1, route.fromHistory, shell.analysisOf(matchLog), shell.rewardOf(matchLog))
                }
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val isDailyMatch = !route.fromHistory && DailyMatch.isDailySeed(matchLog.seed, today)
                val context = LocalContext.current
                LaunchedEffect(route.matchLog) {
                    if (isDailyMatch) {
                        dailyMatchRepository.record(DailyMatchResult(today, state.won, state.humanTotal, state.opponentTotal))
                    }
                }
                PostMatchScreen(
                    state = state,
                    trainingScore = training,
                    onShareClicked = if (isDailyMatch) {
                        {
                            val outcome = context.getString(if (state.won) R.string.daily_match_share_won else R.string.daily_match_share_lost)
                            PuzzleShareFormatter.shareViaIntent(
                                context,
                                context.getString(R.string.daily_match_share_text, today.toString(), outcome, state.humanTotal, state.opponentTotal),
                            )
                        }
                    } else null,
                    onClaimMission = { mission ->
                        shell.claimMissionReward(mission)
                        viewModel.markClaimed(mission.id)
                    },
                    onReviewClicked = {
                        navController.navigate(Route.Review(route.matchLog, route.level, route.fromHistory))
                    },
                    onRematchClicked = {
                        navController.navigate(
                            Route.Table(
                                route.level,
                                matchLog.config.targetScore,
                                route.fundamental,
                                shell.newSeed(),
                                reBello = matchLog.config.reBello,
                                redealOnThreeKings = matchLog.config.redealOnThreeKings,
                                napola = matchLog.config.napola,
                                assoPigliatutto = matchLog.config.assoPigliatutto,
                            ),
                        ) { popUpTo(Route.Home) }
                    },
                    onHomeClicked = { if (route.fromHistory) navController.popBackStack() else goHome() },
                )
            }
            composable<Route.Review> { entry ->
                val route = entry.toRoute<Route.Review>()
                val matchLog = remember(route.matchLog) { MatchLog.parse(route.matchLog) }
                // Analisi già pronta dalla fine partita; dopo la chiusura del processo si ricalcola fuori dal thread UI.
                val analysis by produceState(shell.cachedAnalysis(route.matchLog), route.matchLog) {
                    if (value == null) value = withContext(Dispatchers.Default) { MatchAnalyzer().analyze(matchLog) }
                }
                val ready = analysis
                if (ready != null) {
                    GameReviewScreen(
                        matchLog = matchLog,
                        analysis = ready,
                        onNavigateBack = { navController.popBackStack() },
                    )
                } else {
                    ClubBackground(feltFraction = 0.6f) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                }
            }
            composable<Route.DailyPuzzle> {
                DailyPuzzleScreen(onNavigateHome = { navController.popBackStack() })
            }
            composable<Route.Profile> {
                ProfileScreen(
                    onBackClicked = { navController.popBackStack() },
                    playerName = settings.playerName,
                    onPlayerNameChanged = { name -> scope.launch { settingsRepository.updatePlayerName(name) } },
                    playerNameMaxLength = MAX_PLAYER_NAME,
                    onTrain = { objective -> navController.navigate(Route.Ladder(objective.name)) { launchSingleTop = true } },
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
            composable<Route.Today> {
                TodayScreen(onNavigateHome = { navController.popBackStack() })
            }
            composable<Route.Shop> {
                ShopScreen(onNavigateHome = { navController.popBackStack() })
            }
            composable<Route.Settings> {
                SettingsScreen(
                    onTriggerTestCrash = { crashReporter.triggerTestCrash() },
                    onStartTestEndgameMatch = {
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
                    onNavigateHome = { navController.popBackStack() },
                )
            }
        }
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
