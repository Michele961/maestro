package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.feature.table.presentation.GameUiState
import it.maestro.app.feature.table.presentation.director.SceneStep
import it.maestro.app.feature.table.ui.TableContent
import androidx.compose.runtime.setValue
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.app.feature.table.presentation.OpponentMoveSummary
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaMove
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.data.local.db.MaestroDatabase
import it.maestro.app.domain.match.MatchRepository
import it.maestro.app.data.repository.RoomMatchRepository
import it.maestro.app.designsystem.audio.SoundManager
import it.maestro.app.designsystem.audio.NoOpSoundManager
import it.maestro.app.feature.table.presentation.GameViewModel
import it.maestro.app.feature.table.ui.TableScreen
import it.maestro.game.training.TrainingFundamental
import it.maestro.game.training.TrainingOption
import it.maestro.game.training.TrainingQuestion
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import it.maestro.app.domain.time.Clock
import it.maestro.app.domain.time.FakeClock
import java.time.LocalDate

/**
 * Tavolo da gioco (F1-21): screenshot da confrontare con `docs/art/reference_table_screen.jpg`.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class TableScreenScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)


    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val midDeal = GameUiState(
        phase = DealPhase.PLAYING,
        humanSeat = Seat.P1,
        humanHand = listOf(Card(Rank.SETTE, Suit.DENARI), Card(Rank.RE, Suit.BASTONI), Card(Rank.TRE, Suit.SPADE)),
        tableCards = listOf(
            Card(Rank.ASSO, Suit.BASTONI),
            Card(Rank.SETTE, Suit.COPPE),
            Card(Rank.DUE, Suit.COPPE),
            Card(Rank.QUATTRO, Suit.SPADE),
        ),
        opponentCardCount = 3,
        deckCardCount = 24,
        humanCapturedCount = 6,
        opponentCapturedCount = 2,
        humanSweeps = 1,
        opponentSweeps = 1,
        humanSweepCards = listOf(Card(Rank.SETTE, Suit.DENARI)),
        opponentSweepCards = listOf(Card(Rank.CINQUE, Suit.SPADE)),
        humanScore = 4,
        opponentScore = 2,
        isHumanTurn = true,
        isBotThinking = false,
        pendingCaptureChoice = null,
        lastEvent = null,
        errorMessage = null,
    )

    @Test
    fun table_mid_deal_screenshot() {
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal, scene = sceneOf(midDeal), onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_mid_deal"))
    }

    @Test
    fun table_with_maestro_hint_screenshot() {
        val hint = ScopaMove(card = midDeal.humanHand[0], capture = setOf(Card(Rank.SETTE, Suit.COPPE)))
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal.copy(suggestedMove = hint), scene = sceneOf(midDeal.copy(suggestedMove = hint)), onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_hint"))
    }

    @Test
    fun table_with_bot_card_play_screenshot() {
        val botMove = ScopaMove(card = Card(Rank.SETTE, Suit.SPADE), capture = setOf(Card(Rank.SETTE, Suit.COPPE)))
        val botState = midDeal.copy(isHumanTurn = false, isBotThinking = false)
        val step = SceneStep.CaptureHighlight(Seat.P2, botMove.card, botMove.capture)
        composeRule.setContent { MaestroTheme { TableContent(uiState = botState, scene = sceneOf(botState, step), onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_bot_play"))
    }

    @Test
    fun table_with_opponent_last_move_and_card_selected_screenshot() {
        val state = midDeal.copy(
            opponentLastMove = OpponentMoveSummary(
                card = Card(Rank.SETTE, Suit.SPADE),
                capture = setOf(Card(Rank.SETTE, Suit.COPPE)),
                isSweep = false,
            ),
        )
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, scene = sceneOf(state), onIntent = {}) } }
        val cardToSelect = state.humanHand.first()
        val cardTag = "card_${cardToSelect.suit.name}_${cardToSelect.rank.name}"
        composeRule.onNodeWithTag(cardTag).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(screen.path("table_card_selected_and_opponent_hud"))
    }

    @Test
    fun table_with_opponent_sweep_screenshot() {
        val state = midDeal.copy(
            opponentLastMove = OpponentMoveSummary(
                card = Card(Rank.SETTE, Suit.DENARI),
                capture = setOf(Card(Rank.SETTE, Suit.COPPE)),
                isSweep = true,
            ),
        )
        val step = SceneStep.Sweep(Seat.P2, Seat.P1)
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, scene = sceneOf(state, step), onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_opponent_sweep"))
    }

    @Test
    fun table_with_opponent_drop_screenshot() {
        val state = midDeal.copy(
            opponentLastMove = OpponentMoveSummary(
                card = Card(Rank.SEI, Suit.DENARI),
                capture = emptySet(),
                isSweep = false,
            ),
        )
        val step = SceneStep.OpponentReadPause(Card(Rank.SEI, Suit.DENARI))
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, scene = sceneOf(state, step), onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_opponent_drop"))
    }

    @Test
    fun table_deal_start_screenshot() {
        val state = midDeal.copy(isHumanTurn = false)
        val step = SceneStep.DealCard(index = 2, toSeat = null)
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, scene = sceneOf(state, step), onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_deal_start"))
    }

    @Test
    fun table_capture_in_flight_screenshot() {
        val taken = Card(Rank.SETTE, Suit.COPPE)
        val step = SceneStep.CaptureFlight(Seat.P1, listOf(Card(Rank.SETTE, Suit.DENARI), taken))
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal, scene = sceneOf(midDeal, step), onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_capture_in_flight"))
    }

    @Test
    fun table_human_sweep_screenshot() {
        val step = SceneStep.Sweep(Seat.P1, Seat.P1)
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal, scene = sceneOf(midDeal, step), onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_human_sweep"))
    }

    @Test
    fun points_race_shows_numbers_in_sparring() {
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal, scene = sceneOf(midDeal), onIntent = {}, showPointsRace = true) } }
        composeRule.onNodeWithTag("race_cards").assertTextContains("6–2")
    }

    @Test
    fun points_race_is_hidden_unless_the_player_asks_for_it() {
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal, scene = sceneOf(midDeal), onIntent = {}) } }
        composeRule.onNodeWithTag("points_race").assertDoesNotExist()
    }

    @Test
    fun sweepCardsStayOnThePileWhileASceneIsPlaying() {
        val state = midDeal.copy(humanSweepCards = listOf(Card(Rank.SETTE, Suit.COPPE)))
        val step = SceneStep.CaptureFlight(Seat.P1, listOf(Card(Rank.SETTE, Suit.DENARI)))
        // A riposo la scopa si vede; poi parte una scena: non deve sparire e riapparire (feedback di Michele).
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, scene = sceneOf(state, step), onIntent = {}) } }
        composeRule.onNodeWithTag("human_captures_sweep_0").assertExists()
    }

    @Test
    fun points_race_shows_only_colors_in_classic() {
        val classic = midDeal.copy(isSparring = false)
        composeRule.setContent { MaestroTheme { TableContent(uiState = classic, scene = sceneOf(classic), onIntent = {}, showPointsRace = true) } }
        composeRule.onNodeWithTag("race_cards").assertTextEquals("Carte")
    }

    @Test
    fun hint_bubble_takes_the_place_of_the_points_race() {
        val hint = ScopaMove(card = midDeal.humanHand[0], capture = setOf(Card(Rank.SETTE, Suit.COPPE)))
        val state = midDeal.copy(suggestedMove = hint)
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, scene = sceneOf(state), onIntent = {}, showPointsRace = true) } }
        composeRule.onNodeWithTag("game_hint_banner").assertExists()
        composeRule.onNodeWithTag("points_race").assertDoesNotExist()
    }

    @Test
    fun pause_opens_dialog_and_menu_goes_home() {
        var home = 0
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal, scene = sceneOf(midDeal), onIntent = {}, onNavigateHome = { home++ }) } }
        composeRule.onNodeWithTag("pause_button").performClick()
        composeRule.onNodeWithTag("pause_home_button").performClick()
        org.junit.Assert.assertEquals(1, home)
    }

    @Test
    fun pause_dialog_lets_the_player_change_the_maestro_chattiness() {
        var chosen: it.maestro.app.domain.coach.Chattiness? = null
        composeRule.setContent {
            MaestroTheme {
                TableContent(uiState = midDeal, scene = sceneOf(midDeal), onIntent = {}, onChattinessChange = { chosen = it })
            }
        }
        composeRule.onNodeWithTag("pause_button").performClick()
        composeRule.onNodeWithTag("pause_chat_quiet").performClick()
        org.junit.Assert.assertEquals(it.maestro.app.domain.coach.Chattiness.QUIET, chosen)
    }

    @Test
    fun table_sets_its_backdrop_and_turns_tense_in_the_last_deal() {
        val sound = it.maestro.app.designsystem.audio.NoOpSoundManager()
        var state by androidx.compose.runtime.mutableStateOf(midDeal.copy(deckCardCount = 10))
        composeRule.setContent {
            MaestroTheme { TableContent(uiState = state, scene = sceneOf(state), onIntent = {}, soundManager = sound, localHour = 21) }
        }
        composeRule.waitForIdle()
        org.junit.Assert.assertEquals(it.maestro.app.domain.audio.MusicTrack.CIRCLE_EVENING, sound.currentBackdrop?.music)
        org.junit.Assert.assertEquals(it.maestro.app.domain.audio.AmbientTrack.TABLE, sound.currentBackdrop?.ambient)

        state = midDeal.copy(deckCardCount = 0)
        composeRule.waitForIdle()
        org.junit.Assert.assertEquals(it.maestro.app.domain.audio.MusicTrack.TENSE, sound.currentBackdrop?.music)
    }

    @Test
    fun the_top_bar_shows_the_chosen_opponent_and_not_the_maestro() {
        composeRule.setContent {
            MaestroTheme {
                TableContent(uiState = midDeal, scene = sceneOf(midDeal), onIntent = {}, opponent = it.maestro.app.domain.opponents.Opponent.GIGI)
            }
        }
        composeRule.onNodeWithTag("opponent_portrait").assertExists()
        composeRule.onNodeWithText("Gigi", substring = true).assertExists()
    }

    @Test
    fun playing_card_in_game_does_not_crash() {
        val viewModel = it.maestro.app.feature.table.presentation.GameViewModel(
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
        )
        viewModel.onIntent(
            it.maestro.app.feature.table.presentation.GameIntent.StartMatch(
                config = it.maestro.game.scopa.ScopaConfig(targetScore = 11),
                seed = 42L,
                humanSeat = it.maestro.game.model.Seat.P1,
            ),
        )

        composeRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MaestroTheme {
                val scene by viewModel.scene.collectAsStateWithLifecycle(initialValue = viewModel.currentScene)
                TableContent(
                    uiState = uiState,
                    scene = scene,
                    onIntent = viewModel::onIntent,
                )
            }
        }

        // Troviamo la carta nella mano dell'umano e clicchiamola
        val humanCard = viewModel.uiState.value.humanHand.first()
        composeRule.onNodeWithTag("card_${humanCard.suit.name}_${humanCard.rank.name}").performClick()
        composeRule.mainClock.advanceTimeBy(400L)
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(600L)
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1000L)
        composeRule.waitForIdle()

        org.junit.Assert.assertTrue(viewModel.uiState.value.lastMoveId > 0L)
    }

    @Test
    fun tableScreen_renders_via_koin() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val testDb = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            MaestroDatabase::class.java,
        ).allowMainThreadQueries().build()

        val koinApp = org.koin.dsl.koinApplication {
            androidContext(context)
            modules(
                org.koin.dsl.module {
                    single<Clock> { FakeClock(LocalDate.of(2026, 10, 27)) }
                    single<MaestroDatabase> { testDb }
                    single { testDb.savedMatchDao() }
                    single<MatchRepository> {
                        RoomMatchRepository(dao = get())
                    }
                    single<SoundManager> {
                        NoOpSoundManager()
                    }
                    viewModel {
                        GameViewModel(
                            matchRepository = get(),
                            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
                        )
                    }
                },
            )
        }
        org.koin.core.context.stopKoin()
        org.koin.core.context.startKoin(koinApp)

        try {
            composeRule.setContent {
                MaestroTheme {
                    TableScreen(
                        level = DifficultyLevel.INTERMEDIO,
                        targetScore = 11,
                    )
                }
            }
            composeRule.waitForIdle()
        } finally {
            org.koin.core.context.stopKoin()
            testDb.close()
        }
    }

    @Test
    fun training_question_waits_for_the_scene_to_finish() {
        val question = TrainingQuestion(
            fundamental = TrainingFundamental.SEVENS_COUNT,
            options = listOf(0, 1, 2, 3).map { count -> TrainingOption.IntCount(count) },
            correctIndex = 1,
        )
        val state = midDeal.copy(isSparring = true, activeTrainingQuestion = question)
        val played = SceneStep.OpponentReadPause(Card(Rank.SETTE, Suit.DENARI))
        var scene by androidx.compose.runtime.mutableStateOf(sceneOf(state, played))

        composeRule.setContent { MaestroTheme { TableContent(uiState = state, scene = scene, onIntent = {}) } }

        // Mentre la regia mostra la giocata dell'avversario la domanda non copre il tavolo.
        composeRule.onNodeWithTag("training_question_dialog").assertDoesNotExist()

        scene = sceneOf(state)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("training_question_dialog").assertExists()
        composeRule.onNodeWithTag("training_question_text").assertExists()
    }

    @Test
    fun check_bubble_answers_and_continues_without_covering_the_table() {
        val question = TrainingQuestion(
            fundamental = TrainingFundamental.SEVENS_COUNT,
            options = listOf(0, 1, 2, 3).map { count -> TrainingOption.IntCount(count) },
            correctIndex = 1,
        )
        val intents = mutableListOf<it.maestro.app.feature.table.presentation.GameIntent>()
        var state by androidx.compose.runtime.mutableStateOf(midDeal.copy(isSparring = true, activeTrainingQuestion = question))

        composeRule.setContent {
            MaestroTheme { TableContent(uiState = state, scene = sceneOf(state), onIntent = { intents += it }) }
        }

        // Il Check è un fumetto: le carte della mano restano sul tavolo e giocabili a vista.
        composeRule.onNodeWithTag("training_option_2").performClick()
        state = state.copy(
            trainingQuestionFeedback = it.maestro.app.feature.table.presentation.TrainingAnswerFeedback(
                selectedIndex = 2,
                isCorrect = false,
                correctIndex = 1,
            ),
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("training_feedback_banner").assertExists()
        composeRule.onNodeWithTag("training_continue_button").performClick()

        org.junit.Assert.assertEquals(
            listOf(
                it.maestro.app.feature.table.presentation.GameIntent.AnswerTrainingQuestion(2),
                it.maestro.app.feature.table.presentation.GameIntent.DismissTrainingQuestion,
            ),
            intents,
        )
    }
}
