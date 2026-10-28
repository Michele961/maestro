package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.ai.analysis.MatchAnalyzer
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.ai.bot.SearchBudget
import it.maestro.app.feature.review.ui.GameReviewScreen
import it.maestro.app.domain.retention.DailyMission
import it.maestro.app.domain.retention.DailyMissionType
import it.maestro.app.domain.rewards.MissionAdvance
import it.maestro.app.domain.opponents.LadderVictory
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.rewards.RewardSummary
import it.maestro.app.feature.review.presentation.PostMatchFixtures
import it.maestro.app.feature.review.presentation.PostMatchStep
import it.maestro.app.feature.review.presentation.PostMatchUiState
import it.maestro.app.feature.review.ui.PostMatchScreen
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.game.CoreGame
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.PlayResult
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.newMatch
import it.maestro.game.scopa.play
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Fine partita e Game Review (F1-22): screenshot da confrontare con `docs/art/reference_review_screen.png`.
 * La partita è una smazzata giocata in modo deterministico (seed fisso, prima mossa legale).
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class ReviewScreensScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)


    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val analyzer = MatchAnalyzer(budget = SearchBudget(samples = 2, maxNodes = 40))

    private val log: MatchLog by lazy {
        val config = ScopaConfig()
        var state = newMatch(config = config, seed = 7L)
        val moves = ArrayList<ScopaMove>()
        repeat(36) {
            val legal = legalMoves(state)
            if (legal.isEmpty()) return@repeat
            val move = legal.first()
            val result = play(state, move) as? PlayResult.Applied ?: return@repeat
            moves.add(move)
            state = result.state
        }
        MatchLog(rulesVersion = CoreGame.RULES_VERSION, config = config, seed = 7L, moves = moves)
    }

    private fun postMatchState(step: PostMatchStep, won: Boolean = true, fromHistory: Boolean = false, reward: RewardSummary? = null) =
        PostMatchUiState(
            step = step,
            won = won,
            humanTotal = if (won) 11 else 7,
            opponentTotal = if (won) 8 else 11,
            analysis = if (step == PostMatchStep.LOADING) null else PostMatchFixtures.analysis,
            reward = reward,
            fromHistory = fromHistory,
        )

    private fun shotPostMatch(name: String, state: PostMatchUiState) {
        composeRule.setContent { MaestroTheme { PostMatchScreen(state = state) } }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(screen.path(name))
    }

    @Test
    fun post_match_analysis_waiting_screenshot() = shotPostMatch("post_match_analysis", postMatchState(PostMatchStep.LOADING))

    @Test
    fun post_match_victory_screenshot() = shotPostMatch(
        "post_match",
        postMatchState(PostMatchStep.READY, reward = RewardSummary(masteryBefore = 1200, masteryAfter = 1218, streakAfter = 3)),
    )

    @Test
    fun post_match_defeat_screenshot() = shotPostMatch(
        "post_match_defeat",
        postMatchState(PostMatchStep.READY, won = false, reward = RewardSummary(masteryBefore = 1218, masteryAfter = 1209, streakAfter = 3)),
    )

    @Test
    fun post_match_from_history_screenshot() = shotPostMatch("post_match_history", postMatchState(PostMatchStep.READY, fromHistory = true))

    @Test
    fun post_match_shows_the_mastery_delta_even_after_a_defeat() {
        val state = postMatchState(PostMatchStep.READY, won = false, reward = RewardSummary(masteryBefore = 1218, masteryAfter = 1209))
        composeRule.setContent { MaestroTheme { PostMatchScreen(state = state) } }
        composeRule.onNodeWithTag("reward_mastery_delta", useUnmergedTree = true).assertTextContains("−9")
    }

    @Test
    fun post_match_shows_the_attention_score_of_the_checks() {
        composeRule.setContent { MaestroTheme { PostMatchScreen(state = postMatchState(PostMatchStep.READY), trainingScore = 2 to 3) } }
        composeRule.onNodeWithTag("match_over_attention_score", useUnmergedTree = true).assertTextContains("66", substring = true)
    }

    private val unlockReward = RewardSummary(
        masteryBefore = 1200,
        masteryAfter = 1218,
        ladderVictory = LadderVictory(Opponent.GIGI, firstTime = true, newlyUnlocked = Opponent.NONNA_ROSA),
    )

    @Test
    fun post_match_first_win_over_an_opponent_shows_title_and_unlock() {
        composeRule.setContent { MaestroTheme { PostMatchScreen(state = postMatchState(PostMatchStep.READY, reward = unlockReward)) } }
        composeRule.onNodeWithTag("reward_ladder_title", useUnmergedTree = true).assertTextContains("Promosso al bar", substring = true)
        composeRule.onNodeWithTag("reward_ladder_unlocked", useUnmergedTree = true).assertTextContains("Nonna Rosa", substring = true)
        composeRule.onNodeWithTag("post_match_maestro_line", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun post_match_a_repeated_win_over_an_opponent_shows_no_new_title() {
        val repeat = unlockReward.copy(ladderVictory = LadderVictory(Opponent.GIGI, firstTime = false, newlyUnlocked = null))
        composeRule.setContent { MaestroTheme { PostMatchScreen(state = postMatchState(PostMatchStep.READY, reward = repeat)) } }
        composeRule.onNodeWithTag("reward_ladder_title", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun post_match_ladder_unlock_screenshot() = shotPostMatch("post_match_unlock", postMatchState(PostMatchStep.READY, reward = unlockReward))

    private val completedMission = DailyMission(id = "m1", type = DailyMissionType.MAKE_SWEEPS, progress = 3, target = 3)
    private val missionReward = RewardSummary(
        missions = listOf(MissionAdvance(DailyMissionType.MAKE_SWEEPS, 1, 3, 3, completedMission)),
        masteryBefore = 1200,
        masteryAfter = 1218,
        streakAfter = 3,
    )

    @Test
    fun post_match_mission_ready_to_claim_screenshot() = shotPostMatch("post_match_mission", postMatchState(PostMatchStep.READY, reward = missionReward))

    @Test
    fun post_match_a_completed_mission_is_claimed_with_one_tap() {
        var claimed: DailyMission? = null
        val state = postMatchState(PostMatchStep.READY, reward = missionReward)
        composeRule.setContent { MaestroTheme { PostMatchScreen(state = state, onClaimMission = { claimed = it }) } }
        composeRule.onNodeWithTag("reward_claim_make_sweeps", useUnmergedTree = true).performClick()
        org.junit.Assert.assertEquals(completedMission, claimed)
    }

    @Test
    fun post_match_a_claimed_mission_shows_claimed_and_does_not_claim_twice() {
        var claims = 0
        val state = postMatchState(PostMatchStep.READY, reward = missionReward).copy(claimedMissions = setOf("m1"))
        composeRule.setContent { MaestroTheme { PostMatchScreen(state = state, onClaimMission = { claims++ }) } }
        composeRule.onNodeWithText("Riscattata", ignoreCase = true, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("reward_claim_make_sweeps", useUnmergedTree = true).performClick()
        org.junit.Assert.assertEquals(0, claims)
    }

    @Test
    fun post_match_shows_only_the_loader_until_everything_is_ready() {
        composeRule.setContent { MaestroTheme { PostMatchScreen(state = postMatchState(PostMatchStep.LOADING)) } }
        composeRule.onNodeWithTag("analysis_loading_indicator").assertExists()
        composeRule.onNodeWithTag("post_match_review_button").assertDoesNotExist()
    }

    @Test
    fun post_match_shows_the_actions_once_ready() {
        composeRule.setContent { MaestroTheme { PostMatchScreen(state = postMatchState(PostMatchStep.READY)) } }
        composeRule.onNodeWithTag("post_match_review_button").assertExists()
        composeRule.onNodeWithTag("analysis_loading_indicator").assertDoesNotExist()
    }

    @Test
    fun the_explanation_opens_on_request_and_does_not_squash_the_board() {
        val analysis = analyzer.analyze(log)
        composeRule.setContent { MaestroTheme { GameReviewScreen(matchLog = log, analysis = analysis) } }
        composeRule.onNodeWithTag("review_next_mistake").performClick()
        composeRule.onNodeWithTag("review_show_solution_button").performClick()
        composeRule.onNodeWithTag("review_explanation_card").assertDoesNotExist()
        composeRule.onNodeWithTag("review_board").assertIsDisplayed()
        composeRule.onNodeWithTag("review_why_button").performClick()
        composeRule.onNodeWithTag("review_explanation_card").assertExists()
    }

    @Test
    fun game_review_screenshot() {
        val analysis = analyzer.analyze(log)
        composeRule.setContent { MaestroTheme { GameReviewScreen(matchLog = log, analysis = analysis) } }
        // Salta al primo errore, come la mano evidenziata nel mockup
        composeRule.onNodeWithTag("review_next_mistake").performClick()
        composeRule.onRoot().captureRoboImage(screen.path("game_review"))
    }

    @Test
    fun game_review_retry_mode_screenshot() {
        val analysis = analyzer.analyze(log)
        composeRule.setContent { MaestroTheme { GameReviewScreen(matchLog = log, analysis = analysis) } }
        composeRule.onNodeWithTag("review_next_mistake").performClick()
        composeRule.onNodeWithTag("review_retry_button").performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(screen.path("game_review_retry"))
    }

    @Test
    fun game_review_retry_solved_screenshot() {
        val analysis = analyzer.analyze(log)
        val viewModel = it.maestro.app.feature.review.presentation.GameReviewViewModel()
        composeRule.setContent {
            MaestroTheme {
                GameReviewScreen(
                    matchLog = log,
                    analysis = analysis,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.onNodeWithTag("review_next_mistake").performClick()
        composeRule.onNodeWithTag("review_retry_button").performClick()
        val currentStep = viewModel.uiState.value.currentStep!!
        val bestCard = currentStep.analysis.bestMove.card
        val tag = "card_${bestCard.suit.name}_${bestCard.rank.name}"
        composeRule.onNodeWithTag(tag).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(tag).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(screen.path("game_review_retry_solved"))
    }
}
