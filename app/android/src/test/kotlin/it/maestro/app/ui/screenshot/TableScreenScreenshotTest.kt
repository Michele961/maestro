package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.game.GameUiState
import it.maestro.app.ui.screens.TableContent
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.app.game.OpponentMoveSummary
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaMove
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.data.local.db.MaestroDatabase
import it.maestro.app.data.repository.MatchRepository
import it.maestro.app.data.repository.RoomMatchRepository
import it.maestro.app.audio.SoundManager
import it.maestro.app.audio.NoOpSoundManager
import it.maestro.app.game.GameViewModel
import it.maestro.app.ui.screens.TableScreen
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
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal, onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_mid_deal"))
    }

    @Test
    fun table_with_maestro_hint_screenshot() {
        val hint = ScopaMove(card = midDeal.humanHand[0], capture = setOf(Card(Rank.SETTE, Suit.COPPE)))
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal.copy(suggestedMove = hint), onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_hint"))
    }

    @Test
    fun table_with_bot_card_play_screenshot() {
        val botMove = ScopaMove(card = Card(Rank.SETTE, Suit.SPADE), capture = setOf(Card(Rank.SETTE, Suit.COPPE)))
        val botState = midDeal.copy(
            lastMove = botMove,
            lastMover = Seat.P2,
            lastMoveId = 1L,
            isHumanTurn = false,
            isBotThinking = false,
        )
        composeRule.setContent { MaestroTheme { TableContent(uiState = botState, onIntent = {}) } }
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
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, onIntent = {}) } }
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
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, onIntent = {}) } }
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
        composeRule.setContent { MaestroTheme { TableContent(uiState = state, onIntent = {}) } }
        composeRule.onRoot().captureRoboImage(screen.path("table_opponent_drop"))
    }

    @Test
    fun pause_opens_dialog_and_menu_goes_home() {
        var home = 0
        composeRule.setContent { MaestroTheme { TableContent(uiState = midDeal, onIntent = {}, onNavigateHome = { home++ }) } }
        composeRule.onNodeWithTag("pause_button").performClick()
        composeRule.onNodeWithTag("pause_home_button").performClick()
        org.junit.Assert.assertEquals(1, home)
    }

    @Test
    fun playing_card_in_game_does_not_crash() {
        val viewModel = it.maestro.app.game.GameViewModel(
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
        )
        viewModel.onIntent(
            it.maestro.app.game.GameIntent.StartMatch(
                config = it.maestro.game.scopa.ScopaConfig(targetScore = 11),
                seed = 42L,
                humanSeat = it.maestro.game.model.Seat.P1,
            ),
        )

        composeRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MaestroTheme {
                TableContent(
                    uiState = uiState,
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
    fun training_question_waits_for_move_animation_and_shows_opponent_move() {
        val question = TrainingQuestion(
            fundamental = TrainingFundamental.SEVENS_COUNT,
            options = listOf(0, 1, 2, 3).map { count -> TrainingOption.IntCount(count) },
            correctIndex = 1,
        )
        val opponentMove = OpponentMoveSummary(
            card = Card(Rank.SETTE, Suit.DENARI),
            capture = setOf(Card(Rank.SETTE, Suit.SPADE)),
            isSweep = false,
        )
        val state = midDeal.copy(
            lastMoveId = 10L,
            lastMove = it.maestro.game.scopa.ScopaMove(Card(Rank.SETTE, Suit.DENARI), setOf(Card(Rank.SETTE, Suit.SPADE))),
            lastMover = Seat.P2,
            opponentLastMove = opponentMove,
            activeTrainingQuestion = question,
        )

        composeRule.setContent {
            MaestroTheme {
                TableContent(
                    uiState = state,
                    onIntent = {},
                )
            }
        }

        // All'inizio dell'animazione della mossa, la domanda non deve ancora coprire il tavolo
        composeRule.onNodeWithTag("training_question_dialog").assertDoesNotExist()

        // Avanziamo l'orologio oltre l'animazione della mossa e il tempo di respiro (800ms)
        composeRule.mainClock.advanceTimeBy(3500L)
        composeRule.waitForIdle()

        // Ora il dialogo della domanda deve essere visibile e contenere il testo
        composeRule.onNodeWithTag("training_question_dialog").assertExists()
        composeRule.onNodeWithTag("training_question_text").assertExists()
    }
}
